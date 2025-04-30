package org.hmeadow.fittonia.utility

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.first
import org.hmeadow.fittonia.MainActivity.CreateDumpDirectory
import org.hmeadow.fittonia.dataStore

fun Context.getFileSizeBytes(uri: Uri): Long {
    return contentResolver
        .openAssetFileDescriptor(uri, "r")
        .use { file -> file?.length?.takeIf { it > 0 } ?: 0 }
}

suspend fun Context.createJobDirectory(jobName: String?, print: (String) -> Unit = {}): CreateDumpDirectory {
    val data = dataStore.data.first()
    try {
        var limit = 100
        val dumpUri = Uri.parse(data.dumpPath.dumpPathForReal)
        val dumpObject = DocumentFile.fromTreeUri(this, dumpUri)
        var nextAutoJobName = data.nextAutoJobName
        var attemptJobName: String = jobName ?: "Job$nextAutoJobName"
        while (true) {
            if (dumpObject?.findFile(attemptJobName) == null) {
                val directoryObject = dumpObject?.createDirectory(attemptJobName)
                val directoryUri = directoryObject?.uri
                return if (directoryUri != null) {
                    dataStore.updateData { it.copy(nextAutoJobName = ++nextAutoJobName) }
                    CreateDumpDirectory.Success(uri = directoryUri, name = attemptJobName)
                } else {
                    CreateDumpDirectory.Error.PermissionDenied
                }
            }
            attemptJobName = "${jobName ?: "Job"}$nextAutoJobName"
            nextAutoJobName++
            limit--
            if (limit == 0) {
                throw RuntimeException("Could not create directory after 100 tries.")
            }
        }
    } catch (e: Exception) {
        return if (e.message?.contains(other = "requires that you obtain access") == true) {
            debug { e.printStackTrace() }
            CreateDumpDirectory.Error.PermissionDenied
        } else {
            CreateDumpDirectory.Error.Other
        }
    }
}

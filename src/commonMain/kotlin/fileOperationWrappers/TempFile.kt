package fileOperationWrappers

import commandHandler.FileTransfer
import fileOperationWrappers.FittoniaTempFileBase.FittoniaBufferedWriterBase.BufferedWriterFileLines.bufferedFileLines
import fileOperationWrappers.FittoniaTempFileBase.FittoniaTempFileMock.TempFileLines.fileLines
import java.io.File

internal sealed class FittoniaTempFileBase {

    abstract fun lineStream(block: (String) -> Unit)
    abstract fun bufferedWrite(block: (FittoniaBufferedWriterBase) -> Unit)

    class FittoniaTempFile : FittoniaTempFileBase() {
        private val file: File = File.createTempFile(FileTransfer.tempPrefix, FileTransfer.tempSuffix)

        override fun lineStream(block: (String) -> Unit) = file.lineStream(block)

        override fun bufferedWrite(block: (FittoniaBufferedWriterBase) -> Unit) {
            val bufferedWriter = FittoniaBufferedWriterBase.FittoniaBufferedWriter(file = file)
            block(bufferedWriter)
            bufferedWriter.close()
        }
    }

    data object FittoniaTempFileMock : FittoniaTempFileBase() {
        object TempFileLines {
            val fileLines = mutableListOf("")
        }

        override fun lineStream(block: (String) -> Unit) = fileLines.forEach(block)

        override fun bufferedWrite(block: (FittoniaBufferedWriterBase) -> Unit) {
            val bufferedWriter = FittoniaBufferedWriterBase.FittoniaBufferedWriterMock
            block(bufferedWriter)
            bufferedWriter.close()
        }
    }

    sealed class FittoniaBufferedWriterBase {
        abstract fun write(text: String)
        abstract fun newLine()
        abstract fun close()

        class FittoniaBufferedWriter(file: File) : FittoniaBufferedWriterBase() {
            private val bufferedWriter = file.bufferedWriter()

            override fun write(text: String) = bufferedWriter.write(text)
            override fun newLine() = bufferedWriter.newLine()
            override fun close() = bufferedWriter.close()
        }

        data object BufferedWriterFileLines {
            val bufferedFileLines = mutableListOf<String>()
        }

        data object FittoniaBufferedWriterMock : FittoniaBufferedWriterBase() {

            override fun write(text: String) {
                bufferedFileLines.add(text)
            }

            override fun newLine() {
                bufferedFileLines[bufferedFileLines.lastIndex] = bufferedFileLines.last() + "\n"
            }

            override fun close() {}
        }
    }
}

private fun File.lineStream(block: (String) -> Unit) {
    this.bufferedReader().use { reader ->
        reader.lines().forEach { line ->
            block(line)
        }
    }
}

package org.hmeadow.fittonia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.focus.FocusRequester
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.dataStore
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.hmeadow.fittonia.utility.debug

val Context.dataStore by dataStore("fittonia.json", SettingsDataAndroidSerializer)

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var fileFolderPickerIntent: ActivityResultLauncher<Intent>
    private var onUriPicked: (Uri) -> Unit = {}

    // TODO - After release
    private var testBind = 0
    private var testUnbind = 0

    private var lastServerConnection: ServiceConnection? = null
    private var serverConnection: ServiceConnection? = null

    fun openFilePicker(onSelectItem: (Uri) -> Unit) {
        onUriPicked = onSelectItem
        fileFolderPickerIntent.launch(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                type = "*/*"
            },
        )
    }

    fun openFolderPicker(onSelectItem: (Uri) -> Unit) {
        onUriPicked = onSelectItem
        fileFolderPickerIntent.launch(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addCategory(Intent.CATEGORY_DEFAULT)
            },
        )
    }

    fun getDeviceIpAddress(): String? {
        return (getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { conman ->
            conman.getLinkProperties(conman.activeNetwork)
                ?.linkAddresses
                ?.find { it.toString().contains('.') }
                ?.toString()
                ?.substringBefore('/')
        }
    }

    private fun initFileFolderPickerIntent() {
        fileFolderPickerIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    onUriPicked(uri)
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                }
            }
        }
    }

    private val gestureDetector by lazy {
        GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(ev: MotionEvent): Boolean {
                    // TODO - After release.
                    /*
                    println("currentFocus: $currentFocus")
                    currentFocus?.let { focus ->
                        println("focus.clipBounds: ${focus.clipBounds}}")
                        println("x: ${focus.x}")
                        println("y: ${focus.y}")
                        println("width: ${focus.width}")
                        println("height: ${focus.height}")
                        focus.clipBounds?.let { aaa ->
                            println(aaa)
                            if (!(ev.x >= aaa.left && ev.x <= aaa.right && ev.y >= aaa.top && ev.y <= aaa.bottom)) {
                                val inputMethodManager = (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                                inputMethodManager?.hideSoftInputFromWindow(focus.windowToken, 0)
                                focus.clearFocus()
                            }
                        }
                    }*/
                    return false
                }
            },
        )
    }

    fun attemptStartServer() = viewModel.attemptAndroidServerWithPort(::initAndroidServer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        mainActivityForServer = this
        viewModel = getViewModel()
        val navigator = Navigator(mainViewModel = viewModel)
        initWindowInsetsListener()
        initFileFolderPickerIntent()
        initNotificationChannels()
        attemptStartServer()
        setContent(
            content = {
                navigator.Render(
                    settingsDataAndroid = dataStore.data.collectAsState(initial = SettingsDataAndroid()).value,
                )
            },
        )
    }

    fun unbindFromServer() {
        serverConnection?.let {
            if (isConnected) {
                unbindService(it).also {
                    println("testUnbind = ${++testUnbind}")
                }
            }
        }
        serverConnection = null
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindFromServer()
        mainActivityForServer = null
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return event?.let {
            //gestureDetector.onTouchEvent(event) // TODO - After release
            super.dispatchTouchEvent(event)
        } ?: false
    }

    private fun initNotificationChannels() {
        val channel = NotificationChannel(
            getString(R.string.send_receive_channel_id),
            getString(R.string.send_receive_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.send_receive_channel_description)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun initWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { _, insets ->
            navBarHeight.value = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            imeHeight.value = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            statusBarsHeight.value = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            WindowInsetsCompat.CONSUMED
        }
    }

    var isConnected = false
    private fun initAndroidServer(port: Int, password: String) {
        val intent = Intent(mainActivity, AndroidServer::class.java).apply {
            this.putExtra("org.hmeadow.fittonia.port", port)
            this.putExtra("org.hmeadow.fittonia.password", password)
        }
        lastServerConnection = serverConnection
        if (serverConnection == null) {
            serverConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    isConnected = true
                    AndroidServer.server.value = (service as AndroidServer.AndroidServerBinder).getService()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isConnected = false
                }
            }.also {
                startForegroundService(intent)
                bindService(
                    intent,
                    it,
                    0,
                ).also {
                    println("testBind = ${++testBind}")
                }
            }
        }
    }

    private fun getViewModel() = ViewModelProvider(
        owner = this,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(dataStore = dataStore) as T
            }
        },
    )[MainViewModel::class.java]

    inline fun <reified T : UserAlert> alert(alert: T) {
        if (UserAlert.userAlerts.value.filterIsInstance<T>().size < alert.numAllowed) {
            UserAlert.userAlerts.value += alert
        }
    }

    inline fun <reified T : UserAlert> unAlert() {
        UserAlert.userAlerts.value = UserAlert.userAlerts.value.filter {
            it !is T
        }
    }

    interface CreateDumpDirectory {
        class Success(val uri: Uri) : CreateDumpDirectory
        interface Error : CreateDumpDirectory {
            object PermissionDenied : Error
            object Other : Error
        }
    }

    suspend fun createJobDirectory(jobName: String?, print: (String) -> Unit = {}): CreateDumpDirectory {
        val foo = dataStore.data.first()
        try {
            val dumpUri = Uri.parse(foo.dumpPath.dumpPathForReal)
            val dumpObject = DocumentFile.fromTreeUri(this, dumpUri)
            var nextAutoJobName = foo.nextAutoJobName
            var limit = 100
            var attemptJobName: String = jobName ?: "Job$nextAutoJobName"
            while (true) {
                if (dumpObject?.findFile(attemptJobName) == null) {
                    val directoryObject = dumpObject?.createDirectory(attemptJobName)
                    val directoryUri = directoryObject?.uri
                    return if (directoryUri != null) {
                        dataStore.updateData { it.copy(nextAutoJobName = ++nextAutoJobName) }
                        CreateDumpDirectory.Success(uri = directoryUri)
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
                if (BuildConfig.DEBUG) {
                    e.printStackTrace()
                }
                CreateDumpDirectory.Error.PermissionDenied
            } else {
                CreateDumpDirectory.Error.Other
            }
        }
    }

    fun deleteDumpDirectory(uri: Uri) = DocumentFile.fromTreeUri(this, uri)?.delete()

    companion object {
        lateinit var mainActivity: MainActivity

        var mainActivityForServer: MainActivity? = null

        val imeHeight: MutableStateFlow<Int> = MutableStateFlow(0)
        val navBarHeight: MutableStateFlow<Int> = MutableStateFlow(0)
        val statusBarsHeight: MutableStateFlow<Int> = MutableStateFlow(0)
    }
}

val LocalFocusRequester = compositionLocalOf {
    FocusRequester()
}

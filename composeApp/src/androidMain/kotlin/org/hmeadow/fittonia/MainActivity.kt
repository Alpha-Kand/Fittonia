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
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow

val Context.dataStore by dataStore("fittonia.json", SettingsDataAndroidSerializer)

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var fileFolderPickerIntent: ActivityResultLauncher<Intent>
    private var onUriPicked: (Uri) -> Unit = {}

    private val serverConnection = object : ServiceConnection {
        var isConnected = false

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isConnected = true
            AndroidServer.server.value = (service as AndroidServer.AndroidServerBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

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
            conman.getLinkProperties(conman.activeNetwork)?.let { addresses ->
                addresses
                    .linkAddresses
                    .find { it.toString().contains('.') }
                    .toString()
                    .substringBefore('/')
            }
        }
    }

    private fun initFileFolderPickerIntent() {
        fileFolderPickerIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    onUriPicked(it)
                }
            }
        }
    }

    private val gestureDetector by lazy {
        GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(ev: MotionEvent): Boolean {
                    currentFocus?.let { curFocus ->
                        val inputMethodManager = (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                        inputMethodManager?.hideSoftInputFromWindow(curFocus.windowToken, 0)
                        curFocus.clearFocus()
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        if (serverConnection.isConnected) {
            unbindService(serverConnection)
        }
        mainActivityForServer = null
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return event?.let {
            gestureDetector.onTouchEvent(event)
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

    private fun initAndroidServer(port: Int, password: String) {
        val intent = Intent(mainActivity, AndroidServer::class.java).apply {
            this.putExtra("org.hmeadow.fittonia.port", port)
            this.putExtra("org.hmeadow.fittonia.password", password)
        }
        bindService(
            intent,
            serverConnection,
            0,
        )
        startForegroundService(intent)
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

    fun alert(alert: UserAlert) {
        UserAlert.userAlerts.value += alert
    }

    fun unAlert(alert: Class<out UserAlert>) {
        UserAlert.userAlerts.value = UserAlert.userAlerts.value.filter {
            it::class.java != alert
        }
    }

    companion object {
        lateinit var mainActivity: MainActivity

        var mainActivityForServer: MainActivity? = null

        val imeHeight: MutableStateFlow<Int> = MutableStateFlow(0)
        val navBarHeight: MutableStateFlow<Int> = MutableStateFlow(0)
        val statusBarsHeight: MutableStateFlow<Int> = MutableStateFlow(0)
    }
}

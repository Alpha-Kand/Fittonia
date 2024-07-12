package org.hmeadow.fittonia

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
    private lateinit var fileFolderPickerIntent: ActivityResultLauncher<Intent>
    private var onUriPicked: (Uri) -> Unit = {}

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        val viewModel = getViewModel()
        val navigator = Navigator(mainViewModel = viewModel)
        initWindowInsetsListener()
        initFileFolderPickerIntent()
        setContent(
            content = {
                navigator.Render(
                    settingsDataAndroid = dataStore.data.collectAsState(initial = SettingsDataAndroid()).value,
                )
            },
        )
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return ev?.let {
            gestureDetector.onTouchEvent(ev)
            super.dispatchTouchEvent(ev)
        } ?: false
    }

    private fun initWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { _, insets ->
            navBarHeight.value = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            imeHeight.value = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            statusBarsHeight.value = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            WindowInsetsCompat.CONSUMED
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

    companion object {
        lateinit var mainActivity: MainActivity

        val imeHeight: MutableStateFlow<Int> = MutableStateFlow(0)
        val navBarHeight: MutableStateFlow<Int> = MutableStateFlow(0)
        val statusBarsHeight: MutableStateFlow<Int> = MutableStateFlow(0)
    }
}

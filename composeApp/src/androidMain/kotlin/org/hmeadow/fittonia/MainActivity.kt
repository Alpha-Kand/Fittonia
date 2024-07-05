package org.hmeadow.fittonia

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

val Context.dataStore by dataStore("fittonia.json", SettingsDataAndroidSerializer)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = getViewModel()
        setContent(
            content = {},
        )
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
}

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun desktopAppMain() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Fittonia",
        ) {
            App()
        }
    }
}

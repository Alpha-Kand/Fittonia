import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main(args: Array<String>) =
    singleWindowApplication(
        title = "Fittonia",
        state = WindowState(size = DpSize(500.dp, 800.dp))
    ) {
        FittoniaApp()
    }

@Preview
@Composable
fun ChatPreview() {
    FittoniaApp()
}

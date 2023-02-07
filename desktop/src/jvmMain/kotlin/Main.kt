import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.*
import com.programmersbox.common.UIShow
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Quotes",
        icon = rememberVectorPainter(Icons.Default.FormatQuote)
    ) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            val trayState = rememberTrayState()
            Tray(
                state = trayState,
                icon = rememberVectorPainter(Icons.Default.FormatQuote),
            )
            UIShow(
                onShareClick = {
                    val stringSelection = StringSelection(it.quote)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                    Toolkit.getDefaultToolkit().beep()
                    trayState.sendNotification(Notification("Copied!", it.quote))
                }
            )
        }
    }
}
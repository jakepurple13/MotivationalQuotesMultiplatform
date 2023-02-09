import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import com.programmersbox.common.UIShow
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

fun main() = application {
    val trayState = rememberTrayState()
    Tray(
        state = trayState,
        icon = rememberVectorPainter(Icons.Default.FormatQuote),
    )
    WindowWithBar(
        onCloseRequest = ::exitApplication,
        windowTitle = "Quotes",
    ) {
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
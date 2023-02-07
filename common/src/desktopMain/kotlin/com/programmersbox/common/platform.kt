package com.programmersbox.common

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

public actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
public fun UIShow() {
    App(
        onShareClick = {
            val stringSelection = StringSelection(it.quote)
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(stringSelection, null)
            Toolkit.getDefaultToolkit().beep()
        }
    )
}
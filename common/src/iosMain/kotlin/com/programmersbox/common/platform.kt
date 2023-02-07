package com.programmersbox.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Application
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController

public actual fun getPlatformName(): String {
    return "iOS"
}

@Composable
private fun UIShow(onShareClick: (SavedQuote) -> Unit) {
    App(onShareClick = onShareClick)
}

public fun MainViewController(): UIViewController {
    var onShareClick: (SavedQuote) -> Unit = {}
    val viewController = Application("Quotes") {
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Spacer(Modifier.height(30.dp))
                    UIShow(
                        onShareClick = onShareClick
                    )
                }
            }
        }
    }

    onShareClick = {
        viewController.presentViewController(
            UIActivityViewController(
                activityItems = listOf(it.quote),
                applicationActivities = null
            ),
            true,
            null
        )
    }

    return viewController
}
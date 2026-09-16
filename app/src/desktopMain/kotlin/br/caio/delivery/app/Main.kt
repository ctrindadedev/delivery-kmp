package br.caio.delivery.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

private const val MINIMUM_WINDOW_WIDTH = 420
private const val MINIMUM_WINDOW_HEIGHT = 640

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Delivery — Restaurantes",
        ) {
            window.minimumSize = java.awt.Dimension(MINIMUM_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT)
            DeliveryApp()
        }
    }

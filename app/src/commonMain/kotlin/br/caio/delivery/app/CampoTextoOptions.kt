package br.caio.delivery.app

import androidx.compose.foundation.text.KeyboardOptions

data class CampoTextoOptions(
    val error: String? = null,
    val singleLine: Boolean = true,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
)

package br.caio.delivery.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun rememberFormSubmission(): FormSubmission {
    val scope = rememberCoroutineScope()
    return remember(scope) { FormSubmission(scope) }
}

internal class FormSubmission(
    private val scope: CoroutineScope,
) {
    var saving by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun save(
        onSaved: () -> Unit,
        action: suspend () -> Unit,
    ) {
        if (saving) return
        saving = true
        error = null

        scope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                error = "Não foi possível salvar. Tente novamente."
                return@launch
            } finally {
                saving = false
            }
            onSaved()
        }
    }
}

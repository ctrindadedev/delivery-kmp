package br.caio.delivery.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.caio.delivery.app.theme.DeliveryTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class RestaurantFormState(
    val name: String = "",
    val nameError: String? = null,
    val submitEnabled: Boolean = true,
    val editing: Boolean = false,
)

data class RestaurantFormActions(
    val onNameChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onBack: () -> Unit,
)

@Composable
fun RestaurantFormScreen(
    state: RestaurantFormState,
    actions: RestaurantFormActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 640.dp)) {
            Text(
                text = if (state.editing) "Editar restaurante" else "Cadastrar restaurante",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Informe os dados que serão exibidos aos clientes.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            CampoTexto(
                value = state.name,
                onValueChange = actions.onNameChange,
                label = "Nome do restaurante",
                options =
                    CampoTextoOptions(
                        error = state.nameError,
                        keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done,
                            ),
                    ),
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                OutlinedButton(onClick = actions.onBack) {
                    Text("Cancelar")
                }
                Button(
                    onClick = actions.onSubmit,
                    enabled = state.submitEnabled,
                ) {
                    Text(if (state.editing) "Salvar" else "Cadastrar")
                }
            }
        }
    }
}

@Preview
@Composable
private fun EmptyRestaurantFormPreview() {
    DeliveryTheme(darkTheme = false) {
        Surface {
            RestaurantFormScreen(
                state = RestaurantFormState(submitEnabled = false),
                actions = RestaurantFormActions({}, {}, {}),
            )
        }
    }
}

@Preview
@Composable
private fun RestaurantFormWithErrorPreview() {
    DeliveryTheme(darkTheme = true) {
        Surface {
            RestaurantFormScreen(
                state =
                    RestaurantFormState(
                        name = "A",
                        nameError = "Informe pelo menos 3 caracteres.",
                        submitEnabled = false,
                        editing = true,
                    ),
                actions = RestaurantFormActions({}, {}, {}),
            )
        }
    }
}

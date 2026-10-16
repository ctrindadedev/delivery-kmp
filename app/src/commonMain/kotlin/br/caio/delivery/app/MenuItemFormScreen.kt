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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.caio.delivery.app.theme.DeliveryTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class MenuItemFormState(
    val draft: MenuItemDraft = MenuItemDraft(),
    val nameError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null,
    val submitEnabled: Boolean = true,
    val editing: Boolean = false,
)

data class MenuItemFormActions(
    val onNameChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onPriceChange: (String) -> Unit,
    val onAvailableChange: (Boolean) -> Unit,
    val onSubmit: () -> Unit,
    val onBack: () -> Unit,
)

@Composable
fun MenuItemFormScreen(
    state: MenuItemFormState,
    actions: MenuItemFormActions,
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
            MenuItemFormHeader(editing = state.editing)
            Spacer(Modifier.height(24.dp))
            MenuItemFields(state = state, actions = actions)
            Spacer(Modifier.height(12.dp))
            FormButtons(
                editing = state.editing,
                submitEnabled = state.submitEnabled,
                onSubmit = actions.onSubmit,
                onBack = actions.onBack,
            )
        }
    }
}

@Composable
private fun MenuItemFormHeader(editing: Boolean) {
    Text(
        text = if (editing) "Editar item" else "Adicionar item",
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Preencha as informações que aparecerão no cardápio.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MenuItemFields(
    state: MenuItemFormState,
    actions: MenuItemFormActions,
) {
    CampoTexto(
        value = state.draft.name,
        onValueChange = actions.onNameChange,
        label = "Nome do item",
        options =
            CampoTextoOptions(
                error = state.nameError,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
            ),
    )
    Spacer(Modifier.height(12.dp))
    CampoTexto(
        value = state.draft.description,
        onValueChange = actions.onDescriptionChange,
        label = "Descrição",
        options =
            CampoTextoOptions(
                error = state.descriptionError,
                singleLine = false,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
            ),
    )
    Spacer(Modifier.height(12.dp))
    CampoTexto(
        value = state.draft.price,
        onValueChange = actions.onPriceChange,
        label = "Preço",
        options =
            CampoTextoOptions(
                error = state.priceError,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
            ),
    )
    AvailabilityField(
        available = state.draft.available,
        onAvailableChange = actions.onAvailableChange,
    )
}

@Composable
private fun AvailabilityField(
    available: Boolean,
    onAvailableChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = available,
                    role = Role.Switch,
                    onValueChange = onAvailableChange,
                ).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Disponível para pedidos",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = available,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun FormButtons(
    editing: Boolean,
    submitEnabled: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Cancelar")
        }
        Button(
            onClick = onSubmit,
            enabled = submitEnabled,
        ) {
            Text(if (editing) "Salvar" else "Adicionar")
        }
    }
}

@Preview
@Composable
private fun EmptyMenuItemFormPreview() {
    DeliveryTheme(darkTheme = false) {
        Surface {
            MenuItemFormScreen(
                state = MenuItemFormState(submitEnabled = false),
                actions = MenuItemFormActions({}, {}, {}, {}, {}, {}),
            )
        }
    }
}

@Preview
@Composable
private fun MenuItemFormWithErrorPreview() {
    DeliveryTheme(darkTheme = true) {
        Surface {
            MenuItemFormScreen(
                state =
                    MenuItemFormState(
                        draft =
                            MenuItemDraft(
                                name = "Suco",
                                price = "zero",
                                available = false,
                            ),
                        descriptionError = "Informe uma descrição.",
                        priceError = "Informe um preço válido.",
                        submitEnabled = false,
                        editing = true,
                    ),
                actions = MenuItemFormActions({}, {}, {}, {}, {}, {}),
            )
        }
    }
}

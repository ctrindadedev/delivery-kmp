package br.caio.delivery.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.caio.delivery.dominio.MenuItem

fun MenuItem.toDraft(): MenuItemDraft =
    MenuItemDraft(
        name = nome,
        description = descricao,
        price = preco.toInputValue(),
        available = disponivel,
    )

private fun br.caio.delivery.dominio.Dinheiro.toInputValue(): String {
    val reais = centavos / CENTS_PER_REAL
    val cents = (centavos % CENTS_PER_REAL).toString().padStart(2, '0')
    return "$reais,$cents"
}

@Composable
fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    options: CampoTextoOptions = CampoTextoOptions(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        supportingText = options.error?.let { { Text(it) } },
        isError = options.error != null,
        singleLine = options.singleLine,
        keyboardOptions = options.keyboardOptions,
    )
}

private const val CENTS_PER_REAL = 100L

@Composable
fun ItemLinha(
    item: MenuItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val interactionModifier =
        if (onClick == null) {
            modifier
        } else {
            modifier.clickable(
                onClickLabel = "Editar ${item.nome}",
                role = Role.Button,
                onClick = onClick,
            )
        }

    Card(
        modifier =
            interactionModifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = item.preco.exibir(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!item.disponivel) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Indisponível",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

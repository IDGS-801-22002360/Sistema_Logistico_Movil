package com.example.crm_logistico_movil.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    maxLines: Int = 1,
    placeholder: String = ""
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    val isPasswordField = visualTransformation is PasswordVisualTransformation

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = when {
            isPasswordField -> {
                {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            }
            trailingIcon != null -> { { Icon(trailingIcon, contentDescription = null) } }
            else -> null
        },
        isError = isError,
        supportingText = if (isError && errorMessage?.isNotEmpty() == true) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = if (isPasswordField && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
        modifier = modifier
    )
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClear: () -> Unit = {}
) {
    CustomOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Buscar",
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        trailingIcon = if (value.isNotEmpty()) Icons.Default.Clear else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomOutlinedTextField() {
    CustomOutlinedTextField(
        value = "ejemplo@dominio.com",
        onValueChange = {},
        label = "Correo Electrónico",
        leadingIcon = Icons.Default.Email
    )
}
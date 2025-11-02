package com.example.crm_logistico_movil.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.crm_logistico_movil.navigation.Screen
import com.example.crm_logistico_movil.components.CustomOutlinedTextField
import com.example.crm_logistico_movil.components.GradientButton
import com.example.crm_logistico_movil.ui.theme.PrimaryBlue
import com.example.crm_logistico_movil.ui.theme.PrimaryDarkBlue
import com.example.crm_logistico_movil.viewmodels.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }
    
    val uiState by authViewModel.uiState.collectAsState()
    
    // Observar mensajes de éxito
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && !emailSent) {
            emailSent = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Recuperar contraseña",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (!emailSent) {
                    Text(
                        text = "Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecer tu contraseña.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    CustomOutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (email.isNotBlank()) {
                                    authViewModel.forgotPassword(email.trim())
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    // Mostrar mensaje de error si existe
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GradientButton(
                        text = if (uiState.isLoading) "ENVIANDO..." else "ENVIAR INSTRUCCIONES",
                        onClick = {
                            if (email.isNotBlank()) {
                                authViewModel.forgotPassword(email.trim())
                            }
                        },
                        gradientColors = listOf(PrimaryBlue, PrimaryDarkBlue),
                        enabled = !uiState.isLoading && email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Mostrar indicador de carga
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Mensaje de confirmación
                    Text(
                        text = "✓ Instrucciones enviadas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Hemos enviado las instrucciones para restablecer tu contraseña a:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Por favor, revisa tu bandeja de entrada y sigue las instrucciones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            emailSent = false
                            email = ""
                            authViewModel.clearMessages()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enviar a otro correo")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { 
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Regresar al inicio de sesión")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewForgotPasswordScreen() {
    ForgotPasswordScreen(navController = rememberNavController())
}

package com.example.crm_logistico_movil.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.crm_logistico_movil.R
import com.example.crm_logistico_movil.navigation.Screen
import com.example.crm_logistico_movil.components.CustomOutlinedTextField
import com.example.crm_logistico_movil.components.GradientButton
import com.example.crm_logistico_movil.ui.theme.CRM_Logistico_MovilTheme
import com.example.crm_logistico_movil.ui.theme.PrimaryBlue
import com.example.crm_logistico_movil.ui.theme.PrimaryDarkBlue
import com.example.crm_logistico_movil.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()

    // Observar cambios en el estado de autenticación y navegar
    LaunchedEffect(uiState.isLoggedIn) {
        uiState.currentUser?.let { user ->
            if (uiState.isLoggedIn) {
                val route = when (user.rol) {
                    "cliente" -> Screen.ClientDashboard.route
                    "admin" -> Screen.AdminDashboard.route
                    "ventas" -> Screen.SalesDashboard.route
                    "operaciones" -> Screen.OperativeDashboard.route
                    else -> Screen.ClientDashboard.route // Ruta por defecto si el rol no coincide
                }
                navController.navigate(route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }

    // Mostrar mensajes de error o éxito
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) { // Clave para relanzar cuando el mensaje cambia
            // Aquí puedes mostrar un snackbar o toast si quieres
            // Por ahora solo limpiamos el mensaje después de un tiempo
            kotlinx.coroutines.delay(3000)
            authViewModel.clearMessages()
        }
    }

    // Fondo con degradado sutil
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center), // Alinea la columna al centro del Box
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_utl_logo),
                contentDescription = "UTL Logo",
                modifier = Modifier.size(120.dp).padding(bottom = 32.dp)
            )

            Text(
                text = "Bienvenido a",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "CRM Logístico Móvil",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Inicia sesión en tu cuenta",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Tarjeta de inicio de sesión
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
                    CustomOutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Usuario/Email",
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    CustomOutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                enabled = !uiState.isLoading
                            )
                            Text("Recordar sesión", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(
                            text = "¿Olvidé mi contraseña?",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.clickable {
                                if (!uiState.isLoading) {
                                    navController.navigate(Screen.ForgotPassword.route)
                                }
                            }
                        )
                    }

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
                        text = if (uiState.isLoading) "INICIANDO SESIÓN..." else "INICIAR SESIÓN",
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(email.trim(), password)
                            }
                        },
                        gradientColors = listOf(PrimaryBlue, PrimaryDarkBlue),
                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                    )

                    // Mostrar indicador de carga
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿No tienes cuenta? Regístrate aquí",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable {
                    if (!uiState.isLoading) {
                        navController.navigate(Screen.Register.route)
                    }
                }
            )
        }
    }
}
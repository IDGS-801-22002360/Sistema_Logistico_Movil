package com.example.crm_logistico_movil.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.crm_logistico_movil.models.RegisterRequest
import com.example.crm_logistico_movil.navigation.Screen
import com.example.crm_logistico_movil.components.CustomOutlinedTextField
import com.example.crm_logistico_movil.components.GradientButton
import com.example.crm_logistico_movil.ui.theme.PrimaryBlue
import com.example.crm_logistico_movil.ui.theme.PrimaryDarkBlue
import com.example.crm_logistico_movil.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombreEmpresa by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("México") }
    var telefono by remember { mutableStateOf("") }
    
    var passwordError by remember { mutableStateOf("") }
    
    val uiState by authViewModel.uiState.collectAsState()
    
    // Observar cambios en el estado de registro
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn && uiState.currentUser != null) {
            navController.navigate(Screen.ClientDashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    // Validar contraseñas
    LaunchedEffect(password, confirmPassword) {
        passwordError = when {
            password.isNotEmpty() && password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            confirmPassword.isNotEmpty() && password != confirmPassword -> "Las contraseñas no coinciden"
            else -> ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Crear nueva cuenta",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Completa los siguientes datos para registrarte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos personales",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                CustomOutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre(s)",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = "Apellidos",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    isError = passwordError.isNotEmpty()
                )
                
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos de la empresa",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                CustomOutlinedTextField(
                    value = nombreEmpresa,
                    onValueChange = { nombreEmpresa = it },
                    label = "Nombre de la empresa",
                    leadingIcon = Icons.Default.Business,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = rfc,
                    onValueChange = { rfc = it.uppercase() },
                    label = "RFC",
                    leadingIcon = Icons.Default.Badge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = "Teléfono",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                CustomOutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = "Dirección",
                    leadingIcon = Icons.Default.LocationOn,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomOutlinedTextField(
                        value = ciudad,
                        onValueChange = { ciudad = it },
                        label = "Ciudad",
                        leadingIcon = Icons.Default.LocationCity,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    )

                    CustomOutlinedTextField(
                        value = pais,
                        onValueChange = { pais = it },
                        label = "País",
                        leadingIcon = Icons.Default.Public,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    )
                }
            }
        }

        // Mostrar mensaje de error si existe
        uiState.errorMessage?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        val isFormValid = nombre.isNotBlank() && apellido.isNotBlank() && 
                          email.isNotBlank() && password.isNotBlank() && 
                          confirmPassword.isNotBlank() && nombreEmpresa.isNotBlank() && 
                          rfc.isNotBlank() && telefono.isNotBlank() && 
                          direccion.isNotBlank() && ciudad.isNotBlank() && 
                          pais.isNotBlank() && passwordError.isEmpty()

        GradientButton(
            text = if (uiState.isLoading) "REGISTRANDO..." else "CREAR CUENTA",
            onClick = {
                if (isFormValid) {
                    val registerRequest = RegisterRequest(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        email = email.trim(),
                        password = password,
                        nombreEmpresa = nombreEmpresa.trim(),
                        rfc = rfc.trim(),
                        direccion = direccion.trim(),
                        ciudad = ciudad.trim(),
                        pais = pais.trim(),
                        telefono = telefono.trim()
                    )
                    authViewModel.register(registerRequest)
                }
            },
            gradientColors = listOf(PrimaryBlue, PrimaryDarkBlue),
            enabled = !uiState.isLoading && isFormValid,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Mostrar indicador de carga
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        TextButton(
            onClick = { 
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            enabled = !uiState.isLoading
        ) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    RegisterScreen(navController = rememberNavController())
}

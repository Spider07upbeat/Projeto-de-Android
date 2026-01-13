package ipca.example.JogosAndroid.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun RegisterView(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit
) {
    LaunchedEffect(Unit) {
        authViewModel.clearState()
    }

    val uiState by authViewModel.uiState

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Criar Conta",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TextField(
            modifier = Modifier.padding(8.dp),
            value = uiState.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("Email") }
        )

        TextField(
            modifier = Modifier.padding(8.dp),
            value = uiState.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        TextField(
            modifier = Modifier.padding(8.dp),
            value = uiState.confirmPassword,
            onValueChange = { authViewModel.onConfirmPasswordChange(it) },
            label = { Text("Confirmar Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (uiState.loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    authViewModel.register(onSuccess = onRegisterSuccess)
                }
            ) {
                Text("Registar")
            }
        }

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Já tem conta? Fazer Login.")
        }
    }
}

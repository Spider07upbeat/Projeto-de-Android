package ipca.example.JogosAndroid.ui.login

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

data class AuthState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val loading: Boolean = false,
)

class AuthViewModel : ViewModel() {
    var uiState = mutableStateOf(AuthState())
        private set

    fun onEmailChange(newEmail: String) {
        uiState.value = uiState.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        uiState.value = uiState.value.copy(password = newPassword)
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        uiState.value = uiState.value.copy(confirmPassword = newConfirmPassword)
    }

    fun login(onSuccess: () -> Unit) {
        uiState.value = uiState.value.copy(loading = true, error = null)
        if (uiState.value.email.isEmpty() || uiState.value.password.isEmpty()) {
            uiState.value = uiState.value.copy(error = "Email e password não podem estar vazios.", loading = false)
            return
        }

        Firebase.auth.signInWithEmailAndPassword(uiState.value.email, uiState.value.password)
            .addOnCompleteListener { task ->
                uiState.value = uiState.value.copy(loading = false)
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login realizado com sucesso!")
                    onSuccess() // Chama o callback de sucesso
                } else {
                    Log.w("AuthViewModel", "Falha no login.", task.exception)
                    uiState.value = uiState.value.copy(error = task.exception?.message)
                }
            }
    }

    fun register(onSuccess: () -> Unit) {
        uiState.value = uiState.value.copy(loading = true, error = null)

        if (uiState.value.email.isEmpty() || uiState.value.password.isEmpty()) {
            uiState.value = uiState.value.copy(error = "Email e password não podem estar vazios.", loading = false)
            return
        }
        if (uiState.value.password != uiState.value.confirmPassword) {
            uiState.value = uiState.value.copy(error = "As passwords não coincidem.", loading = false)
            return
        }

        Firebase.auth.createUserWithEmailAndPassword(uiState.value.email, uiState.value.password)
            .addOnCompleteListener { task ->
                uiState.value = uiState.value.copy(loading = false)
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Utilizador registado com sucesso!")
                    val user = Firebase.auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = uiState.value.email.split('@')[0]
                    }
                    user?.updateProfile(profileUpdates)

                    onSuccess()
                } else {
                    Log.w("AuthViewModel", "Falha no registo.", task.exception)
                    uiState.value = uiState.value.copy(error = task.exception?.message)
                }
            }
    }
    fun clearState() {
        uiState.value = AuthState()
    }
}

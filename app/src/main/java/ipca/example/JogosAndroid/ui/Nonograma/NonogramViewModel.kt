package ipca.example.JogosAndroid.ui.Nonograma

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
data class CompletedPuzzle(
    val userId: String = "",
    val puzzleSize: Int = 0,
    val completedAt: Long = System.currentTimeMillis(),
    val gridAsString: String = ""
) {
    constructor() : this("", 0, 0, "")
}

class NonogramViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _completedPuzzles = MutableStateFlow<List<CompletedPuzzle>>(emptyList())
    val completedPuzzles: StateFlow<List<CompletedPuzzle>> = _completedPuzzles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    fun resetForNewUser() {
        _completedPuzzles.value = emptyList()
        Log.d("NonogramViewModel", "RESET COMPLETO. Estado do Nonogram limpo para novo utilizador.")
    }
    fun saveCompletedPuzzle(puzzleSize: Int, gridAsString: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val puzzle = CompletedPuzzle(
                userId = userId,
                puzzleSize = puzzleSize,
                completedAt = System.currentTimeMillis(),
                gridAsString = gridAsString
            )
            try {
                db.collection("users").document(userId)
                    .collection("Jogos").document("Nonogram")
                    .collection("PuzzlesCompletos").add(puzzle)
                    .await()

                Log.d("Firestore", "Puzzle de Nonogram guardado na pasta do utilizador com sucesso!")
            } catch (e: Exception) {
                Log.e("Firestore", "Erro ao guardar Nonogram na pasta do user: ", e)
            }
        }
    }

    fun fetchCompletedPuzzles() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            _isLoading.value = true
            try {
                val snapshot = db.collectionGroup("PuzzlesCompletos")
                    .whereEqualTo("userId", userId)
                    .orderBy("completedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                _completedPuzzles.value = snapshot.documents.mapNotNull { it.toObject<CompletedPuzzle>() }
                Log.d("Firestore", "Encontrados ${_completedPuzzles.value.size} puzzles completos para o utilizador.")
            } catch (e: Exception) {
                Log.e("Firestore", "Erro ao carregar puzzles completos do user: ", e)
                _completedPuzzles.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}

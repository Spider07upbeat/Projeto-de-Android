package ipca.example.JogosAndroid.ui.PokeCliker

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HighScoreEntry(
    val score: Int = 0,
    val playerName: String = "Unknown",
    val timestamp: Timestamp = Timestamp.now()
)

class GameManager : ViewModel() {

    private val _currentEnemy = MutableStateFlow<Enemy?>(null)
    val currentEnemy = _currentEnemy.asStateFlow()

    private val _nextEnemy = MutableStateFlow<Enemy?>(null)

    private val _highScores = MutableStateFlow<List<HighScoreEntry>>(emptyList())
    val highScores = _highScores.asStateFlow()

    private var score = 0
    private var damagePerClick = 10
    private var isInitialized = false

    init {
        if (Firebase.auth.currentUser != null) {
            loadLastScoreFromFirestore()
        }
    }
    fun resetForNewUser() {
        score = 0
        isInitialized = false
        _currentEnemy.value = null
        _nextEnemy.value = null
        _highScores.value = emptyList()
        if (Firebase.auth.currentUser != null) {
            loadLastScoreFromFirestore()
        }
        Log.d("GameManager", "RESET COMPLETO. A carregar dados para o novo utilizador.")
    }

    private fun loadLastScoreFromFirestore() {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser ?: return@launch
            val db = Firebase.firestore
            try {
                val lastSession = db.collection("users")
                    .document(user.uid)
                    .collection("Jogos")
                    .document("PokeClicker")
                    .collection("Sessoes")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()
                if (!lastSession.isEmpty) {
                    val lastScore = lastSession.documents[0].getLong("score")?.toInt() ?: 0
                    score = lastScore
                } else {
                    score = 0
                }
                Log.d("GameManager", "Score carregado da Firestore para ${user.email}: $score")
            } catch (e: Exception) {
                Log.e("GameManager", "Erro ao carregar o score da Firestore.", e)
                score = 0
            }
        }
    }

    fun initializeAndPreload(context: Context) {
        if (isInitialized) return
        viewModelScope.launch {
            delay(500)
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            startPreloading(context, screenWidth, screenHeight)
            isInitialized = true
        }
    }

    fun processAttack(context: Context, screenWidth: Int, screenHeight: Int) {
        _currentEnemy.value?.let { enemy ->
            if (enemy.life > 0) {
                val newLife = enemy.life - damagePerClick
                _currentEnemy.value = enemy.copy(life = newLife)
                if (newLife <= 0) {
                    advanceToNextEnemy(context, screenWidth, screenHeight)
                }
            }
        }
    }

    fun startPreloading(context: Context, screenWidth: Int, screenHeight: Int) {
        val initialLife = 100 + (score / 5) * 10
        if (_currentEnemy.value == null) {
            viewModelScope.launch {
                Enemy.create(context, screenWidth, screenHeight) { enemy ->
                    _currentEnemy.value = enemy.copy(life = initialLife)
                }
            }
        }
        if (_nextEnemy.value == null) {
            viewModelScope.launch {
                Enemy.create(context, screenWidth, screenHeight) { enemy ->
                    _nextEnemy.value = enemy
                }
            }
        }
    }

    private fun advanceToNextEnemy(context: Context, screenWidth: Int, screenHeight: Int) {
        score++
        val newLife = 100 + (score / 5) * 10
        _currentEnemy.value = _nextEnemy.value?.copy(life = newLife)
        _nextEnemy.value = null
        viewModelScope.launch {
            Enemy.create(context, screenWidth, screenHeight) { newNextEnemy ->
                _nextEnemy.value = newNextEnemy
            }
        }
    }

    fun saveGameDataToFirestore() {
        val user = Firebase.auth.currentUser ?: return
        if (score == 0) return
        val db = Firebase.firestore
        val gameSessionData = hashMapOf(
            "score" to score,
            "timestamp" to Timestamp.now(),
            "playerEmail" to (user.email ?: "Anónimo")
        )
        db.collection("users")
            .document(user.uid)
            .collection("Jogos")
            .document("PokeClicker")
            .collection("Sessoes")
            .add(gameSessionData)
            .addOnSuccessListener { Log.d("GameManager", "Score guardado: $score") }
            .addOnFailureListener { e -> Log.e("GameManager", "Erro ao guardar score", e) }
    }
    fun fetchHighScores() {
        viewModelScope.launch {
            val db = Firebase.firestore
            try {
                val sessionsSnapshot = db.collectionGroup("Sessoes")
                    .orderBy("score", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()

                val allScores = sessionsSnapshot.documents.mapNotNull { doc ->
                    val sessionScore = doc.getLong("score")?.toInt() ?: 0
                    val playerName = doc.getString("playerEmail") ?: "Anónimo"
                    val timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    HighScoreEntry(sessionScore, playerName, timestamp)
                }

                val bestScoresMap = mutableMapOf<String, HighScoreEntry>()

                for (entry in allScores) {
                    val existingEntry = bestScoresMap[entry.playerName]
                    if (existingEntry == null) {
                        bestScoresMap[entry.playerName] = entry
                    }
                }

                val uniqueHighScores = bestScoresMap.values.sortedByDescending { it.score }

                _highScores.value = uniqueHighScores

            } catch (e: Exception) {
                Log.e("GameManager", "Erro ao carregar ranking global", e)
            }
        }
    }


    fun getScore(): Int = score
}

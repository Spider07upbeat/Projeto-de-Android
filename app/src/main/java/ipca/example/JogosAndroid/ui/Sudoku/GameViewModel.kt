package ipca.example.JogosAndroid.ui.Sudoku

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class SudokuRecord(
    val userId: String = "",
    val difficulty: String = "",
    val timeInSeconds: Long = 0L,
    val completedAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
) {
    constructor() : this("", "", 0L, com.google.firebase.Timestamp.now())
}

data class GameState(
    val initialBoardGenerated: Array<IntArray> = emptyArray(),
    val board: Array<IntArray> = emptyArray(),
    val solvedBoard: Array<IntArray> = emptyArray(),
    val selectedCell: Pair<Int, Int>? = null,
    val bestTimes: List<SudokuRecord> = emptyList(),
    val timer: Long = 0L,
    val isCompleted: Boolean = false,
    val transientMessage: String? = null,
    val isLoading: Boolean = true,
    val currentDifficulty: SudokuGenerator.Difficulty = SudokuGenerator.Difficulty.EASY
)

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()
    private var timerJob: Job? = null
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    fun fetchBestTimesFromFirestore() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(isLoading = true)
            try {
                val snapshot = db.collectionGroup("Tempos")
                    .whereEqualTo("userId", user.uid)
                    .orderBy("difficulty")
                    .orderBy("timeInSeconds", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val records = snapshot.documents.mapNotNull { it.toObject<SudokuRecord>() }

                val bestOfEachDifficulty = records
                    .groupBy { it.difficulty }
                    .map { (_, group) -> group.first() }

                _gameState.value = _gameState.value.copy(
                    bestTimes = bestOfEachDifficulty,
                    isLoading = false
                )
                Log.d("SudokuViewModel", "Carregados ${bestOfEachDifficulty.size} recordes de Sudoku do Firestore.")

            } catch (e: Exception) {
                Log.e("SudokuViewModel", "Erro ao carregar recordes de Sudoku", e)
                _gameState.value = _gameState.value.copy(isLoading = false, bestTimes = emptyList())
            }
        }
    }

    fun loadRandomLevel(difficulty: SudokuGenerator.Difficulty) {
        stopTimer()
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(
                isLoading = true,
                isCompleted = false,
                currentDifficulty = difficulty
            )
            val (initialBoard, solvedBoard) = withContext(Dispatchers.Default) {
                SudokuGenerator.generate(difficulty)
            }
            _gameState.value = _gameState.value.copy(
                initialBoardGenerated = initialBoard,
                board = initialBoard.map { it.clone() }.toTypedArray(),
                solvedBoard = solvedBoard,
                isLoading = false
            )
            startTimer()
        }
    }

    fun verifySolution() {
        val currentBoard = _gameState.value.board
        val solvedBoard = _gameState.value.solvedBoard
        if (currentBoard.any { row -> row.contains(0) }) {
            showTransientMessage("Grelha incompleta")
            return
        }
        if (currentBoard.contentDeepEquals(solvedBoard)) {
            stopTimer()
            val finalTimeInSeconds = _gameState.value.timer
            val difficulty = _gameState.value.currentDifficulty
            saveTimeAndScoreToFirestore(finalTimeInSeconds, difficulty)
            _gameState.value = _gameState.value.copy(isCompleted = true)
        } else {
            showTransientMessage("Solução incorreta")
        }
    }

    private fun saveTimeAndScoreToFirestore(timeInSeconds: Long, difficulty: SudokuGenerator.Difficulty) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val record = SudokuRecord(
                userId = user.uid,
                difficulty = difficulty.name,
                timeInSeconds = timeInSeconds
            )
            try {
                db.collection("users").document(user.uid)
                    .collection("Jogos").document("Sudoku")
                    .collection("Tempos").add(record).await()
                Log.d("SudokuViewModel", "Tempo de Sudoku guardado com sucesso.")
            } catch (e: Exception) {
                Log.e("SudokuViewModel", "Erro ao guardar o tempo de Sudoku", e)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _gameState.value = _gameState.value.copy(timer = 0L)
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _gameState.value = _gameState.value.copy(timer = _gameState.value.timer + 1)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onCellClick(row: Int, col: Int) {
        val isInitial = _gameState.value.initialBoardGenerated.getOrNull(row)?.getOrNull(col) != 0
        if (!isInitial) {
            _gameState.value = _gameState.value.copy(
                selectedCell = if (_gameState.value.selectedCell == Pair(row, col)) null else Pair(row, col)
            )
        }
    }

    fun onNumberClick(number: Int) {
        _gameState.value.selectedCell?.let { (row, col) ->
            val newBoard = _gameState.value.board.map { it.clone() }.toTypedArray()
            newBoard[row][col] = number
            _gameState.value = _gameState.value.copy(board = newBoard)
        }
    }

    private fun showTransientMessage(message: String) {
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(transientMessage = message)
            delay(2000)
            _gameState.value = _gameState.value.copy(transientMessage = null)
        }
    }
}

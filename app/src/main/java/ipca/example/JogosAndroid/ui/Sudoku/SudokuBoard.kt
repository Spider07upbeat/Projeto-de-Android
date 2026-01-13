package ipca.example.JogosAndroid.ui.Sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SudokuBoardScreen(navController: NavController, gameViewModel: GameViewModel) {
    val gameState by gameViewModel.gameState.collectAsState()
    if (gameState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("A gerar novo puzzle...", fontSize = 18.sp)
            }
        }
    } else {
        SudokuGameContent(navController, gameViewModel, gameState)
    }
}

@Composable
private fun SudokuGameContent(navController: NavController, gameViewModel: GameViewModel, gameState: GameState) {
    val formattedTime = remember(gameState.timer) {
        val totalSeconds = gameState.timer
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Tempo: $formattedTime", fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))

        SudokuBoardLayout(gameState, gameViewModel)
        Spacer(Modifier.height(24.dp))

        if (!gameState.isCompleted) {
            NumberPad(boardSize = gameState.board.size) { number -> gameViewModel.onNumberClick(number) }
        }

        Spacer(Modifier.height(16.dp))
        gameState.transientMessage?.let {
            Text(it, color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        if (gameState.isCompleted) {
            Text("Puzzle Completo!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Continuar") }
        } else {
            Button(onClick = { gameViewModel.verifySolution() }) { Text("Verificar") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Voltar") }
        }
    }
}

@Composable
private fun SudokuBoardLayout(gameState: GameState, gameViewModel: GameViewModel) {
    val initialBoard = gameState.initialBoardGenerated
    Box(modifier = Modifier.aspectRatio(1f).border(2.dp, Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            val boardSize = gameState.board.size
            if (boardSize == 0) return
            for (row in 0 until boardSize) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0 until boardSize) {
                        val number = gameState.board[row][col]
                        val isInitial = initialBoard.getOrNull(row)?.getOrNull(col) != 0
                        SudokuCell(
                            number,
                            gameState.selectedCell == Pair(row, col),
                            isInitial,
                            Modifier.weight(1f).clickable(!isInitial) { gameViewModel.onCellClick(row, col) },
                            boardSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuCell(number: Int, isSelected: Boolean, isInitial: Boolean, modifier: Modifier, boardSize: Int) {
    Box(
        modifier = modifier
            .border(0.5.dp, Color.Gray)
            .background(if (isSelected) Color.LightGray else Color.White)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (number == 0) "" else number.toString(),
            fontSize = if (boardSize > 9) 14.sp else 20.sp,
            fontWeight = if (isInitial) FontWeight.Bold else FontWeight.Normal,
            color = if (isInitial) Color.Black else Color.Blue
        )
    }
}

@Composable
fun NumberPad(boardSize: Int, onNumberClick: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (boardSize == 0) return
        val numbers = (1..boardSize).toList()
        val chunkSize = kotlin.math.ceil(boardSize / 2.0).toInt().coerceAtLeast(1)
        numbers.chunked(chunkSize).forEach { rowNumbers ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowNumbers.forEach { NumberButton(it, onClick = onNumberClick) }
            }
            Spacer(Modifier.height(4.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { NumberButton(0, "X", onNumberClick) }
    }
}

@Composable
fun NumberButton(number: Int, text: String = number.toString(), onClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color.DarkGray, CircleShape)
            .clickable { onClick(number) },
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = TextStyle(color = Color.White, fontSize = if (text.length > 1) 16.sp else 20.sp))
    }
}

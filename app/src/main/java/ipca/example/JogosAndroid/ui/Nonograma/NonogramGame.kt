package ipca.example.JogosAndroid.ui.Nonograma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CellState { EMPTY, FILLED, MARKED }

@Composable
fun NonogramGame(
    solutionGrid: List<List<Boolean>>,
    onBackToSelection: () -> Unit,
    onLevelCompleted: () -> Unit
) {
    if (solutionGrid.isEmpty() || solutionGrid[0].isEmpty()) {
        return
    }
    val numRows = solutionGrid.size
    val numCols = solutionGrid[0].size

    var playerGrid by remember {
        mutableStateOf(
            List(numRows) { List(numCols) { CellState.EMPTY } }
        )
    }

    var isMarkMode by remember { mutableStateOf(false) }

    val isSolved = remember(playerGrid, solutionGrid) {
        derivedStateOf {
            playerGrid.indices.all { r ->
                playerGrid[r].indices.all { c ->
                    (playerGrid[r][c] == CellState.FILLED) == solutionGrid[r][c]
                }
            }
        }
    }

    LaunchedEffect(isSolved.value) {
        if (isSolved.value) {
            onLevelCompleted()
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val rowClueWidth = 40.dp
    val cellSize = ((screenWidth - rowClueWidth - 32.dp) / numCols).coerceAtMost(48.dp)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nonogram", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row {
            Button(
                onClick = { isMarkMode = false },
                colors = if (!isMarkMode) ButtonDefaults.buttonColors(containerColor = Color.DarkGray) else ButtonDefaults.buttonColors()
            ) { Text("Fill") }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { isMarkMode = true },
                colors = if (isMarkMode) ButtonDefaults.buttonColors(containerColor = Color.DarkGray) else ButtonDefaults.buttonColors()
            ) { Text("X") }
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.wrapContentSize()) {
            Column {
                Row {
                    Spacer(Modifier.width(rowClueWidth))
                    repeat(numCols) { col ->
                        Text(
                            text = getColumnClue(solutionGrid, col),
                            modifier = Modifier.width(cellSize),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                repeat(numRows) { row ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getRowClue(solutionGrid[row]),
                            modifier = Modifier.width(rowClueWidth),
                            textAlign = TextAlign.End
                        )
                        repeat(numCols) { col ->
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .padding(1.dp)
                                    .background(
                                        when (playerGrid[row][col]) {
                                            CellState.FILLED -> Color.Black
                                            else -> Color.LightGray
                                        }
                                    )
                                    .border(1.dp, Color.DarkGray)
                                    .clickable {
                                        val newRow = playerGrid[row].toMutableList()
                                        newRow[col] = when {
                                            isMarkMode && newRow[col] != CellState.MARKED -> CellState.MARKED
                                            !isMarkMode && newRow[col] != CellState.FILLED -> CellState.FILLED
                                            else -> CellState.EMPTY
                                        }
                                        playerGrid = playerGrid.toMutableList().also { it[row] = newRow }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (playerGrid[row][col] == CellState.MARKED) {
                                    Text("X", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { playerGrid = List(numRows) { List(numCols) { CellState.EMPTY } } }) { Text("Reset Puzzle") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBackToSelection) { Text("Back") }
        if (isSolved.value) {
            Spacer(Modifier.height(12.dp))
            Text("Puzzle Solved!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        }
    }
}

fun getRowClue(row: List<Boolean>): String {
    val clues = mutableListOf<Int>()
    var count = 0
    for (cell in row) {
        if (cell) count++ else if (count > 0) { clues.add(count); count = 0 }
    }
    if (count > 0) clues.add(count)
    return if (clues.isEmpty()) "0" else clues.joinToString(" ")
}

fun getColumnClue(grid: List<List<Boolean>>, col: Int): String {
    val clues = mutableListOf<Int>()
    var count = 0
    for (row in grid) {
        if (row[col]) count++ else if (count > 0) { clues.add(count); count = 0 }
    }
    if (count > 0) clues.add(count)
    return if (clues.isEmpty()) "0" else clues.joinToString("\n")
}

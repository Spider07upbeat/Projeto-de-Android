package ipca.example.JogosAndroid.ui.Nonograma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedNonogramsScreen(
    navController: NavController,
    nonogramViewModel: NonogramViewModel
) {
    val completedPuzzles by nonogramViewModel.completedPuzzles.collectAsState()
    val isLoading by nonogramViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        nonogramViewModel.fetchCompletedPuzzles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nonogramas Completos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (completedPuzzles.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Ainda não completaste nenhum puzzle!")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(completedPuzzles) { puzzle ->
                        CompletedPuzzleItem(puzzle = puzzle)
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Voltar")
            }
        }
    }
}

@Composable
fun CompletedPuzzleItem(puzzle: CompletedPuzzle) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(puzzle.completedAt))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .padding(12.dp)
    ) {
        Text("Tamanho: ${puzzle.puzzleSize}x${puzzle.puzzleSize}", fontWeight = FontWeight.Bold)
        Text("Completo em: $date")
        Spacer(Modifier.height(8.dp))
        // Desenha a pequena grelha do puzzle
        MiniGrid(gridAsString = puzzle.gridAsString, size = puzzle.puzzleSize)
    }
}

@Composable
fun MiniGrid(gridAsString: String, size: Int) {
    if (gridAsString.length != size * size) return

    Column {
        val rows = gridAsString.chunked(size)
        rows.forEach { rowString ->
            Row {
                rowString.forEach { char ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(if (char == '1') Color.Black else Color.White)
                            .border(0.5.dp, Color.DarkGray)
                    )
                }
            }
        }
    }
}


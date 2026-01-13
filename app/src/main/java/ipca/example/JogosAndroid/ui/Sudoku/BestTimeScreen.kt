package ipca.example.JogosAndroid.ui.Sudoku

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun BestTimesScreen(navController: NavController, gameViewModel: GameViewModel) {
    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        gameViewModel.fetchBestTimesFromFirestore()
    }

    fun formatSeconds(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Melhores Tempos", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))

        if (gameState.isLoading) {
            CircularProgressIndicator()
        }
        else if (gameState.bestTimes.isEmpty()) {
            Text("Ainda não tens recordes guardados.")
        }
        else {
            Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                gameState.bestTimes.sortedBy { it.difficulty }.forEach { record ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = record.difficulty, fontWeight = FontWeight.Bold)
                        Text(text = formatSeconds(record.timeInSeconds))
                    }
                    Divider()
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para o fundo

        Button(onClick = { navController.popBackStack() }) {
            Text("Voltar")
        }
    }
}

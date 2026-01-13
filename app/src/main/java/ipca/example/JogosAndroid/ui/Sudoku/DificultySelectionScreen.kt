package ipca.example.JogosAndroid.ui.Sudoku

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DificultySelectionScreen(navController: NavController, gameViewModel: GameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Escolhe a Dificuldade", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 48.dp))
        DifficultyButton("Fácil", Color(0xFF4CAF50)) {
            gameViewModel.loadRandomLevel(SudokuGenerator.Difficulty.EASY)
            navController.navigate("sudoku_board")
        }
        Spacer(modifier = Modifier.height(24.dp))
        DifficultyButton("Médio", Color(0xFFFFC107)) {
            gameViewModel.loadRandomLevel(SudokuGenerator.Difficulty.MEDIUM)
            navController.navigate("sudoku_board")
        }
        Spacer(modifier = Modifier.height(24.dp))
        DifficultyButton("Difícil", Color(0xFFF44336)) {
            gameViewModel.loadRandomLevel(SudokuGenerator.Difficulty.HARD)
            navController.navigate("sudoku_board")
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Voltar")
        }
    }
}

@Composable
private fun DifficultyButton(text: String, backgroundColor: Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = backgroundColor), modifier = Modifier.fillMaxWidth(0.8f).height(65.dp)) {
        Text(text, fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

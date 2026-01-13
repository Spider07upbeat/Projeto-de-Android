package ipca.example.JogosAndroid.ui.Sudoku

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun InitialScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sudoku", fontSize = 80.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 60.dp))
        MenuButton("Jogar", onClick = { navController.navigate("sudoku_difficulty_selection") })
        Spacer(modifier = Modifier.height(24.dp))
        MenuButton("Melhores Tempos", onClick = { navController.navigate("sudoku_best_times") })
        Spacer(modifier = Modifier.height(24.dp))
        MenuButton("Voltar ao Hub", onClick = { navController.popBackStack() })
    }
}

@Composable
private fun MenuButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(0.8f).height(65.dp)) {
        Text(text, fontSize = 22.sp)
    }
}

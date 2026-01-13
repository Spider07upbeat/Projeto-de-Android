package ipca.example.JogosAndroid.nonogram

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SizeSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select Puzzle Size", modifier = Modifier.padding(bottom = 24.dp))

        val onSizeSelected = { size: Int ->
            navController.navigate("nonogram_game/$size")
        }

        Button(onClick = { onSizeSelected(5) }) { Text("5 × 5") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onSizeSelected(10) }) { Text("10 × 10") }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onSizeSelected(15) }) { Text("15 × 15") }

        Spacer(Modifier.height(32.dp))
        Button(onClick = { navController.popBackStack() }) { Text("Back") }
    }
}

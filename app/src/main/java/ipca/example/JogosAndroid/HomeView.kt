package ipca.example.JogosAndroid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun HomeView(navController: NavController, onLogout: () -> Unit) {
    val user = Firebase.auth.currentUser
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2C2C2C)).padding(horizontal = 32.dp, vertical = 24.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Game Hub", fontSize = 70.sp, color = Color.White)
                Text(user?.email ?: "Utilizador", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA500), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                GameButton("PokeClicker") { navController.navigate("poke_clicker_hub") }
                GameButton("Nonogram") { navController.navigate("nonogram_hub") }
                GameButton("Sudoku") { navController.navigate("sudoku_hub") }
            }
            Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White), modifier = Modifier.border(1.dp, Color.Gray, CircleShape).padding(horizontal = 16.dp)) {
                Text("Logout", fontWeight = FontWeight.Normal, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun GameButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(70.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500), contentColor = Color.Black, disabledContainerColor = Color.Gray, disabledContentColor = Color.DarkGray)) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

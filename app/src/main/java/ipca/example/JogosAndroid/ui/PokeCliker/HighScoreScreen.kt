package ipca.example.JogosAndroid.ui.PokeCliker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ipca.example.JogosAndroid.ui.PokeCliker.themePokeclicker.ThaleahFat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HighScoreScreen(
    navController: NavController,
    gameManager: GameManager
) {
    val highScores by gameManager.highScores.collectAsState()

    LaunchedEffect(Unit) {
        gameManager.fetchHighScores()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "High Scores",
            fontFamily = ThaleahFat,
            fontSize = 60.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (highScores.isEmpty()) {
            CircularProgressIndicator()
            Text("A carregar ranking...", modifier = Modifier.padding(top = 16.dp))
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jogador", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Score", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Divider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(highScores) { index, entry ->
                    HighScoreRow(rank = index + 1, entry = entry)
                    Divider()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Voltar", fontSize = 18.sp)
        }
    }
}

@Composable
fun HighScoreRow(rank: Int, entry: HighScoreEntry) {
    val date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(entry.timestamp.toDate())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("$rank. ${entry.playerName}", fontSize = 18.sp)
            Text(date, fontSize = 12.sp, color = Color.Gray)
        }
        Text(entry.score.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

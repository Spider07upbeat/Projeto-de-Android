package ipca.example.JogosAndroid

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ipca.example.JogosAndroid.ui.PokeCliker.GameManager
import ipca.example.JogosAndroid.ui.PokeCliker.themePokeclicker.ThaleahFat

@Composable
fun PokeClickerView(
    navController: NavController,
    gameManager: GameManager
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = gameManager) {
        gameManager.initializeAndPreload(context)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.first_back),
            contentDescription = "background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Poke",
                fontFamily = ThaleahFat,
                fontSize = 90.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Clicker",
                fontFamily = ThaleahFat,
                fontSize = 70.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 80.dp)
            )
            HubTextButton(
                text = "Play Now",
                onClick = { navController.navigate("poke_clicker_game") }
            )

            HubTextButton(
                text = "High-Score",
                onClick = { navController.navigate("poke_clicker_highscore") }
            )

            HubTextButton(
                text = "Voltar ao Hub",
                onClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun HubTextButton(
    text: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val alpha = if (isPressed) 0.5f else 1.0f

    Text(
        text = text,
        fontFamily = ThaleahFat,
        fontSize = 50.sp,
        textAlign = TextAlign.Center,
        color = Color.White,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    )
}

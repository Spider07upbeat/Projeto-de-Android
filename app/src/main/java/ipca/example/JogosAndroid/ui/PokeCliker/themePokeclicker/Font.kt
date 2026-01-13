package ipca.example.JogosAndroid.ui.PokeCliker.themePokeclicker

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ipca.example.JogosAndroid.R

// Define a nossa família de fontes personalizada
val ThaleahFat = FontFamily(
    // Diz ao Compose para encontrar o ficheiro 'thaleah_fat.ttf' na pasta res/font
    // e associá-lo ao peso 'Normal'
    Font(R.font.thaleahfat, FontWeight.Normal)

    // Se tivesse uma versão 'Bold' da mesma fonte, adicionaria aqui:
    // Font(R.font.thaleah_fat_bold, FontWeight.Bold)
)

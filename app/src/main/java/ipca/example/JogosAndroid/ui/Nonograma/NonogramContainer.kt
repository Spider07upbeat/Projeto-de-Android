package ipca.example.JogosAndroid.ui.Nonograma

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.example.JogosAndroid.ui.theme.JogosAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.min

@Composable
fun NonogramContainer(
    puzzleSize: Int,
    onNavigateBack: () -> Unit,
    nonogramViewModel: NonogramViewModel = viewModel()
) {
    Log.d("NonogramContainer", "Composable iniciado com puzzleSize: $puzzleSize")

    JogosAndroidTheme {
        var solutionGrid by remember { mutableStateOf<List<List<Boolean>>?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorOccurred by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(key1 = puzzleSize) {
            isLoading = true
            errorOccurred = false
            coroutineScope.launch {
                val grid = fetchAndGeneratePuzzle(puzzleSize)
                if (grid.isNotEmpty()) {
                    solutionGrid = grid
                } else {
                    errorOccurred = true
                }
                isLoading = false
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorOccurred || solutionGrid == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Erro: Não foi possível gerar um puzzle válido.")
            }
        } else {
            NonogramGame(
                solutionGrid = solutionGrid!!,
                onBackToSelection = onNavigateBack,
                onLevelCompleted = {
                    val gridAsString = solutionGrid!!.joinToString(separator = "") { row ->
                        row.joinToString(separator = "") { if (it) "1" else "0" }
                    }
                    nonogramViewModel.saveCompletedPuzzle(puzzleSize, gridAsString)
                    onNavigateBack()
                }
            )
        }
    }
}

private suspend fun fetchAndGeneratePuzzle(size: Int): List<List<Boolean>> {
    if (size <= 0) {
        Log.e("NonogramGenerator", "ERRO FATAL: fetchAndGeneratePuzzle chamado com tamanho inválido ($size). A abortar.")
        return emptyList()
    }
    Log.d("NonogramGenerator", "A iniciar geração de puzzle $size x $size.")
    repeat(5) { attempt ->
        Log.d("NonogramGenerator", "Tentativa ${attempt + 1} de 5...")
        val bitmap = fetchRandomGrayscaleImage(500, 500)
        if (bitmap == null) {
            Log.w("NonogramGenerator", "Falha ao obter imagem da internet na tentativa ${attempt + 1}.")
            return@repeat
        }

        val bwBitmap = bitmapToBlackAndWhite(bitmap, 128)
        val grid = pixelateBitmapToGrid(bwBitmap, size, size)

        if (grid.isNotEmpty() && !isPuzzleTooUniform(grid)) {
            Log.d("NonogramGenerator", "Puzzle válido gerado com sucesso!")
            return grid
        } else {
            Log.w("NonogramGenerator", "Puzzle gerado na tentativa ${attempt + 1} foi descartado (vazio ou uniforme).")
        }
    }
    Log.e("NonogramGenerator", "Falha ao gerar um puzzle válido após 5 tentativas.")
    return emptyList()
}


private suspend fun fetchRandomGrayscaleImage(width: Int, height: Int): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://picsum.photos/$width/$height?grayscale"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            Log.e("NonogramGenerator", "Falha na ligação à net: ${e.message}")
            null
        }
    }
}


private fun bitmapToBlackAndWhite(bitmap: Bitmap, threshold: Int = 128): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixel = bitmap.getPixel(x, y)
            val gray = ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
            bwBitmap.setPixel(x, y, if (gray < threshold) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bwBitmap
}


private fun pixelateBitmapToGrid(bitmap: Bitmap, rows: Int, cols: Int): List<List<Boolean>> {
    if (rows <= 0 || cols <= 0) {
        Log.e("NonogramGenerator", "ERRO CRÍTICO: pixelateBitmapToGrid chamada com rows/cols a zero. Rows: $rows, Cols: $cols. A abortar para evitar crash.")
        return emptyList()
    }
    val grid = mutableListOf<List<Boolean>>()
    val cellW = bitmap.width / cols
    val cellH = bitmap.height / rows
    if (cellW <= 0 || cellH <= 0) {
        Log.e("NonogramGenerator", "ERRO: Imagem demasiado pequena para o tamanho do puzzle. CellW: $cellW, CellH: $cellH.")
        return emptyList()
    }
    for (r in 0 until rows) {
        val rowList = mutableListOf<Boolean>()
        for (c in 0 until cols) {
            var blackCount = 0
            var totalPixels = 0
            val startX = c * cellW
            val startY = r * cellH
            for (x in startX until min(startX + cellW, bitmap.width)) {
                for (y in startY until min(startY + cellH, bitmap.height)) {
                    if (bitmap.getPixel(x, y) == android.graphics.Color.BLACK) blackCount++
                    totalPixels++
                }
            }
            rowList.add(if (totalPixels > 0) blackCount > totalPixels / 2 else false)
        }
        grid.add(rowList)
    }
    return grid
}


private fun isPuzzleTooUniform(grid: List<List<Boolean>>): Boolean {
    if (grid.isEmpty() || grid[0].isEmpty()) return true
    val totalCells = grid.size * grid[0].size
    val filledCells = grid.flatten().count { it }
    val fillRatio = if (totalCells > 0) filledCells.toFloat() / totalCells else 0f
    return fillRatio < 0.25f || fillRatio > 0.75f
}

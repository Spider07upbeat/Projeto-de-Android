package ipca.example.JogosAndroid.ui.PokeCliker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.Coil
import coil.request.ImageRequest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.random.Random
data class Enemy(
    val context: Context,
    val screenWidth: Int,
    val screenHeight: Int,
    var name: String = "Loading...",
    var life: Int = 100,
    var bitmap: Bitmap? = null,
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var isReady: Boolean = false
) {
    companion object {
        private val okHttpClient = OkHttpClient()

        fun create(context: Context, screenWidth: Int, screenHeight: Int, onReady: (Enemy) -> Unit) {
            thread {
                try {
                    val pokemonId = Random.Default.nextInt(1, 1026)
                    val apiUrl = "https://pokeapi.co/api/v2/pokemon/$pokemonId"
                    val request = Request.Builder().url(apiUrl).build()

                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) return@thread

                        val responseBody = response.body?.string() ?: return@thread
                        val json = JSONObject(responseBody)
                        val sprites = json.getJSONObject("sprites")
                        val imageUrl = sprites.getString("front_default")
                        val pokemonName = json.getString("name").replace('-', ' ')
                            .replaceFirstChar { it.titlecase(Locale.getDefault()) }

                        if (imageUrl.isNotBlank() && imageUrl != "null") {
                            loadBitmapFromUrl(
                                context,
                                screenWidth,
                                screenHeight,
                                imageUrl,
                                pokemonName,
                                onReady
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Enemy", "Erro ao carregar dados do Pokémon", e)
                }
            }
        }

        private fun loadBitmapFromUrl(
            context: Context,
            screenWidth: Int,
            screenHeight: Int,
            url: String,
            name: String,
            onReady: (Enemy) -> Unit
        ) {
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .target { drawable ->
                    val originalBitmap = (drawable as BitmapDrawable).bitmap
                    val enemyWidth = screenWidth / 2
                    val enemyHeight = (originalBitmap.height.toFloat() / originalBitmap.width.toFloat() * enemyWidth).toInt()
                    val enemyX = (screenWidth - enemyWidth) / 2
                    val enemyY = (screenHeight / 2) - enemyHeight + 150
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, enemyWidth, enemyHeight, true)
                    val newEnemy = Enemy(
                        context = context,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        name = name,
                        life = 100,
                        bitmap = scaledBitmap,
                        x = enemyX,
                        y = enemyY,
                        width = enemyWidth,
                        height = enemyHeight,
                        isReady = true
                    )
                    onReady(newEnemy)
                }
                .build()
            imageLoader.enqueue(request)
        }
    }
}
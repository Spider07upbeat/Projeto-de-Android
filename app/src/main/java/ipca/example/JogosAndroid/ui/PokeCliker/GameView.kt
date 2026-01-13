package ipca.example.JogosAndroid.ui.PokeCliker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat
import ipca.example.JogosAndroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameView(
    context: Context,
    private val gameManager: GameManager
) : SurfaceView(context), Runnable {

    private var gameThread: Thread? = null
    @Volatile
    private var isPlaying = false
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var paint: Paint
    private lateinit var player: Player
    private var currentEnemy: Enemy? = null
    private var score = 0
    private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val backgrounds = mutableListOf<Bitmap>()
    private lateinit var backgroundRect: Rect
    private var currentBackgroundIndex = 0
    private val scoreToChangeBackground = 30
    private var customTypeface: Typeface? = null
    private lateinit var soundPool: SoundPool
    private var soundIdAttack: Int = 0
    private var canAttack = true

    init {
        surfaceHolder = holder
        paint = Paint()
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                initializeGameObjects(width, height)
                resumeGame()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pause()
            }
        })

        customTypeface = ResourcesCompat.getFont(context, R.font.thaleahfat)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build()
        soundIdAttack = soundPool.load(context, R.raw.shoot, 1)
    }

    private fun initializeGameObjects(width: Int, height: Int) {
        backgroundRect = Rect(0, 0, width, height)
        backgrounds.clear()
        backgrounds.add(loadAndScaleBackground(R.drawable.first_back, width, height))
        backgrounds.add(loadAndScaleBackground(R.drawable.second_back, width, height))
        backgrounds.add(loadAndScaleBackground(R.drawable.third_back, width, height))
        player = Player(context, width, height)
        observeGameState()
        Log.d("GameView", "Objetos do jogo inicializados.")
    }

    private fun observeGameState() {
        viewScope.launch {
            gameManager.currentEnemy.collectLatest { enemy ->
                currentEnemy = enemy
                score = gameManager.getScore()
                if (enemy != null) {
                    Log.d("GameView", "Observador recebeu: ${enemy.name} com vida ${enemy.life}")
                    val backgroundLevel = score / scoreToChangeBackground
                    val newIndex = backgroundLevel % backgrounds.size
                    if (newIndex != currentBackgroundIndex) {
                        currentBackgroundIndex = newIndex
                    }
                }
            }
        }
    }

    override fun run() {
        while (isPlaying) {
            update()
            draw()
            control()
        }
    }

    private fun update() {
        player.update()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!::player.isInitialized) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            currentEnemy?.let { enemy ->
                if (enemy.life > 0 && enemy.bitmap != null) {
                    val enemyRect =
                        Rect(enemy.x, enemy.y, enemy.x + enemy.width, enemy.y + enemy.height)
                    if (enemyRect.contains(event.x.toInt(), event.y.toInt())) {
                        if (canAttack) {
                            player.attack()
                            soundPool.play(soundIdAttack, 1f, 1f, 0, 0, 1f)
                            gameManager.processAttack(context, width, height)
                            canAttack = false
                        }
                    }
                }
            }
        } else if (event.action == MotionEvent.ACTION_UP) {
            canAttack = true
        }
        return true
    }

    private fun draw() {
        if (!surfaceHolder.surface.isValid || !::player.isInitialized) {
            return
        }

        val canvas = surfaceHolder.lockCanvas() ?: return
        try {
            canvas.drawBitmap(backgrounds[currentBackgroundIndex], null, backgroundRect, paint)
            player.draw(canvas)

            paint.typeface = customTypeface

            currentEnemy?.let { enemy ->
                if (enemy.isReady && enemy.bitmap != null) {
                    canvas.drawBitmap(enemy.bitmap!!, enemy.x.toFloat(), enemy.y.toFloat(), paint)
                    paint.textSize = 70f
                    paint.textAlign = Paint.Align.CENTER
                    paint.color = Color.BLACK
                    canvas.drawText("Life: ${enemy.life}", enemy.x + (enemy.width / 2f), enemy.y.toFloat() - 30, paint)
                    canvas.drawText(enemy.name, enemy.x + (enemy.width / 2f), enemy.y.toFloat() - 100, paint)
                } else {
                    paint.textSize = 80f
                    paint.textAlign = Paint.Align.CENTER
                    paint.color = Color.BLACK
                    canvas.drawText("Waiting for Pokémon...", width / 2f, height / 2f, paint)
                }
            } ?: run {
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.BLACK
                canvas.drawText("Waiting for Pokémon...", width / 2f, height / 2f, paint)
            }

            paint.textSize = 120f
            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.BLACK
            canvas.drawText("Score: $score", width / 2f, 120f, paint)
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun loadAndScaleBackground(drawableId: Int, width: Int, height: Int): Bitmap {
        val original = BitmapFactory.decodeResource(resources, drawableId)
        return Bitmap.createScaledBitmap(original, width, height, true)
    }

    private fun control() {
        try { Thread.sleep(17) } catch (e: InterruptedException) { e.printStackTrace() }
    }

    private fun resumeGame() {
        if (!isPlaying) {
            isPlaying = true
            gameThread = Thread(this)
            gameThread?.start()
            Log.d("GameView", "Thread do jogo iniciada.")
        }
    }

    fun pause() {
        if (isPlaying) {
            gameManager.saveGameDataToFirestore()

            isPlaying = false
            try {
                gameThread?.join(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            gameThread = null
            Log.d("GameView", "Jogo pausado e dados guardados.")
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
    }
}
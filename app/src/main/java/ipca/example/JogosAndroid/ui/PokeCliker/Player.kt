package ipca.example.JogosAndroid.ui.PokeCliker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import ipca.example.JogosAndroid.R

class Player(context: Context, screenWidth: Int, screenHeight: Int) {

    private val idleSheet: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.idle_up)
    private val attackSheet: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.attack1_up)
    private var currentSheet: Bitmap = idleSheet
    private val frameCount = 8
    private var currentFrame = 0
    private var lastFrameChangeTime = 0L
    private val frameLengthInMilliseconds = 50L
    private val frameWidth: Int = idleSheet.width / frameCount
    private val frameHeight: Int = idleSheet.height
    private val scaleFactor = 3.0f
    private val scaledWidth = (frameWidth * scaleFactor).toInt()
    private val scaledHeight = (frameHeight * scaleFactor).toInt()
    val x: Int = (screenWidth / 2) - (scaledWidth / 2)
    val y: Int = screenHeight - scaledHeight - 100

    private var isAttacking = false

    fun attack() {
        if (!isAttacking) {
            isAttacking = true
            currentSheet = attackSheet
            currentFrame = 0
        }
    }
    fun update() {
        val time = System.currentTimeMillis()

        if (time > lastFrameChangeTime + frameLengthInMilliseconds) {
            lastFrameChangeTime = time
            currentFrame++

            if (currentFrame >= frameCount) {
                currentFrame = 0

                if (isAttacking) {
                    isAttacking = false
                    currentSheet = idleSheet
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        val srcX = currentFrame * frameWidth
        val srcRect = Rect(srcX, 0, srcX + frameWidth, frameHeight)

        val destRect = Rect(x, y, x + scaledWidth, y + scaledHeight)

        canvas.drawBitmap(currentSheet, srcRect, destRect, null)
    }
}
package com.example.flappyx_with_ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class GameView(context: Context) : View(context) {

    private var bgImage: Bitmap? = null
    private var birdImage: Bitmap? = null
    private var bottomTube: Bitmap? = null
    private var topTube: Bitmap? = null

    // Physics
    private var birdX = 200f
    private var birdY = 500f
    private var velocity = 0f
    private val gravity = 2.5f
    private val jumpStrength = -30f

    // Tube & Gap System
    private var tubeX = 1000f
    private val tubeSpeed = 12f
    private var gapTopY = 0f
    private var gapSize = 400f

    // Game States
    private var isGameOver = false
    private var score = 0
    private var isScored = false
    private var isSurfaceReady = false

    // AI Mode
    var isAiEnabled = true

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(5f, 5f, 5f, Color.BLACK)
    }

    private val birdPaint = Paint().apply { color = Color.YELLOW }
    private val tubePaint = Paint().apply { color = Color.GREEN }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (!isGameOver && isSurfaceReady) {
                if (isAiEnabled) {
                    runAiLogic()
                }
                updatePhysics()
            }
            invalidate()
            handler.postDelayed(this, 30)
        }
    }

    init {
        try {
            bgImage = BitmapFactory.decodeResource(resources, R.drawable.background)
            birdImage = BitmapFactory.decodeResource(resources, R.drawable.icon)
            bottomTube = BitmapFactory.decodeResource(resources, R.drawable.tube)
        } catch (e: Exception) {
            // Resources might not exist yet
        }
        handler.post(updateRunnable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        bgImage = bgImage?.let { Bitmap.createScaledBitmap(it, w, h, false) }
        birdImage = birdImage?.let { Bitmap.createScaledBitmap(it, 120, 120, false) } 
        bottomTube = bottomTube?.let { Bitmap.createScaledBitmap(it, 250, h, false) }

        bottomTube?.let {
            val matrix = Matrix().apply { postScale(1f, -1f) }
            topTube = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
        }

        gapSize = 120 * 4f
        resetTubes()
        isSurfaceReady = true
    }

    private fun resetTubes() {
        tubeX = width.toFloat()
        isScored = false
        val minTopY = height * 0.1f
        val maxTopY = height - gapSize - (height * 0.1f)
        gapTopY = minTopY + Random.nextFloat() * (maxTopY - minTopY)
    }

    private fun updatePhysics() {
        velocity += gravity
        birdY += velocity
        tubeX -= tubeSpeed

        if (tubeX < -250) { // tube width
            resetTubes()
        }

        if (tubeX + 250 < birdX && !isScored) {
            score++
            isScored = true
        }

        checkCollisions()
    }

    private fun runAiLogic() {
        // AI Logic: Jump if bird is below the center of the gap
        val gapCenterY = gapTopY + (gapSize / 2)
        // Add a bit of look-ahead or buffer
        if (birdY > gapCenterY - 20) {
            velocity = jumpStrength
        }
    }

    private fun checkCollisions() {
        if (birdY > height - 120 || birdY < 0) {
            isGameOver = true
        }

        val birdRect = Rect(birdX.toInt(), birdY.toInt(), (birdX + 120).toInt(), (birdY + 120).toInt())
        val topTubeRect = Rect(tubeX.toInt(), (gapTopY - height).toInt(), (tubeX + 250).toInt(), gapTopY.toInt())
        val bottomTubeRect = Rect(tubeX.toInt(), (gapTopY + gapSize).toInt(), (tubeX + 250).toInt(), (gapTopY + gapSize + height).toInt())

        if (Rect.intersects(birdRect, topTubeRect) || Rect.intersects(birdRect, bottomTubeRect)) {
            isGameOver = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw Background
        bgImage?.let { canvas.drawBitmap(it, 0f, 0f, null) } ?: canvas.drawColor(Color.BLUE)

        if (isSurfaceReady) {
            // Draw Tubes
            topTube?.let { canvas.drawBitmap(it, tubeX, gapTopY - it.height, null) }
                ?: canvas.drawRect(tubeX, 0f, tubeX + 250, gapTopY, tubePaint)
            
            bottomTube?.let { canvas.drawBitmap(it, tubeX, gapTopY + gapSize, null) }
                ?: canvas.drawRect(tubeX, gapTopY + gapSize, tubeX + 250, height.toFloat(), tubePaint)
        }

        // Draw Bird
        birdImage?.let { canvas.drawBitmap(it, birdX, birdY, null) }
            ?: canvas.drawCircle(birdX + 60, birdY + 60, 60f, birdPaint)

        // Draw Score
        canvas.drawText(score.toString(), width / 2f, 200f, textPaint)

        if (isGameOver) {
            canvas.drawText("GAME OVER", width / 2f, height / 2f, textPaint)
            textPaint.textSize = 60f
            canvas.drawText("Tap to Restart", width / 2f, height / 2f + 120f, textPaint)
            if (isAiEnabled) canvas.drawText("AI is ON", width / 2f, height / 2f + 200f, textPaint)
            textPaint.textSize = 100f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                restartGame()
            } else {
                if (!isAiEnabled) {
                    velocity = jumpStrength
                } else {
                    // Toggle AI off if user taps? Or just let AI play.
                    // Let's make long press or something toggle AI? 
                    // For now, simple tap to restart works.
                }
            }
        }
        return true
    }

    private fun restartGame() {
        birdY = height / 2f
        velocity = 0f
        score = 0
        resetTubes()
        isGameOver = false
    }
}

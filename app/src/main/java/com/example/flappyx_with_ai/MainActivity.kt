package com.example.flappyx_with_ai

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

class MainActivity : Activity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Hide action bar
        actionBar?.hide()

        gameView = GameView(this)
        setContentView(gameView)
    }
}

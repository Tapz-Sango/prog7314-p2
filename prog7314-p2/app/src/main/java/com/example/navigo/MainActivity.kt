package com.example.navigo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val splashHandler = Handler(Looper.getMainLooper())

    private val openLogin = Runnable {
        startActivity(
            Intent(this, LoginActivity::class.java)
        )
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_main)

        splashHandler.postDelayed(openLogin, 2000)
    }

    override fun onDestroy() {
        splashHandler.removeCallbacks(openLogin)
        super.onDestroy()
    }
}
package com.example.wisechoice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

class SplashActivity : AppCompatActivity() {
    var logo: ImageView? = null
    var top: Animation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        top = AnimationUtils.loadAnimation(this, R.anim.top_animation)

        logo = findViewById<ImageView>(R.id.logo)
        logo?.startAnimation(top)


        val handler = Handler()
        handler.postDelayed({
            startActivity(Intent(this@SplashActivity, SignInActivity::class.java))
            finish()
        }, 3000)
    }
}
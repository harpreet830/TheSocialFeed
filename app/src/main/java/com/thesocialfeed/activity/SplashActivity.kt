package com.thesocialfeed.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.thesocialfeed.R

class SplashActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }, 1000)
    }

}
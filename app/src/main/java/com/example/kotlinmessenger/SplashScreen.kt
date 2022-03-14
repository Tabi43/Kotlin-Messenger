package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.kotlinmessenger.Activity.DashBoard
import com.example.kotlinmessenger.Activity.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private var firebaseAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            if (firebaseAuth!!.currentUser == null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, DashBoard::class.java))
                finish()
            }
        }, 3000)

    }

}

package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.auth.GoogleAuthUtil.getToken
import com.google.android.gms.auth.zzd.getToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class SplashScreen : AppCompatActivity() {

    private var firebaseAuth : FirebaseAuth? = null
    private lateinit var appUtil: AppUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()
        appUtil = AppUtil()

        Handler().postDelayed({
            if (firebaseAuth!!.currentUser == null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {

                FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
                    if (result != null) {
                        val token=result
                        val databaseReference =
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(appUtil.getUID()!!)

                        val map: MutableMap<String, Any> = HashMap()
                        map["token"] = token!!
                        databaseReference.updateChildren(map)
                    }
                }

                startActivity(Intent(this, DashBoard::class.java))
                finish()
            }
        }, 3000)

    }

}

package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.kotlinmessenger.Constants.AppConstants
import com.google.android.gms.auth.GoogleAuthUtil.getToken
import com.google.android.gms.auth.zzd.getToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

class SplashScreen : AppCompatActivity() {

    private var firebaseAuth : FirebaseAuth? = null
    private lateinit var appUtil: AppUtil
    private val TAG = "SLASHSCREEN ACTIVITY"

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
                bindUrlToUSer(firebaseAuth!!.uid!!)
                startActivity(Intent(this, DashBoard::class.java))
                finish()
            }
        }, 1000)

    }

    private fun bindUrlToUSer(uid: String) {
        FirebaseStorage.getInstance().reference.child(AppConstants.PATH + uid).downloadUrl
            .addOnSuccessListener {
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/users").child(uid).child("image").setValue(it.toString())
                    .addOnSuccessListener {
                        Log.d(TAG,"Url image correctly bind to userDB")
                    }
                    .addOnFailureListener {
                        Log.d(TAG,"Error to bind url image -> $it")
                    }
            }
            .addOnFailureListener {
                Log.d(TAG,"Error to bind url image -> $it")
            }
    }

}

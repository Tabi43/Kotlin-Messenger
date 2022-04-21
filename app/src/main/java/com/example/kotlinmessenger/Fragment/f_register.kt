package com.example.kotlinmessenger.Fragment

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.AppUtil
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.DashBoard
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import com.example.kotlinmessenger.UserModel
import com.google.firebase.messaging.FirebaseMessaging


class f_register : Fragment(R.layout.f_register) {

    lateinit var binding: FRegisterBinding
    private lateinit var appUtil: AppUtil
    private val TAG = "REGISTRATION FRAGMENT"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FRegisterBinding.inflate(inflater, container, false)

        binding.confirmButton.setOnClickListener {

        }
        appUtil = AppUtil()
        binding.loginreturnButton.setOnClickListener {
            val fragment = f_login()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer, fragment)?.commit()
        }
        binding.confirmButton.setOnClickListener {
            performRegistration()
        }

        return binding.root
    }

    private fun performRegistration() {
        var email = binding.mailET.text.toString()
        var password = binding.passwordET.text.toString()
        var confirm_password = binding.confirmPasswordET.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Please fill out the fields", Toast.LENGTH_LONG).show()
            return
        }
        Log.d("Main Activity", "$password :check $confirm_password")
        if (password != confirm_password) {
            Toast.makeText(activity, "Check your password", Toast.LENGTH_LONG).show()
            return
        }

        //info visibili su logcat
        Log.d("Main Activity", "Email is $email")
        Log.d("Main Activity", "Password is $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }
                Log.d("Main Activity", "Utente creato con successo ${it.result?.user?.uid}")
                saveUserToFirebaseDatabase()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to register -> ${it.message}", Toast.LENGTH_LONG)
                    .show()
                //info visibili su logcat
            }
    }//fine performRegistration

    private fun saveUserToFirebaseDatabase() {
        Log.d("Register Activity", "Chiamata salvataggio dati utente")
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val userRef =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users")
        Log.d("Register Activity", "Reference : $userRef")
        //val user = User(uid, binding.username.text.toString(), "Hello")
        val user = UserModel(binding.username.text.toString(), "Hello", "", uid, "offline", "false")
        userRef.child(uid).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(activity, "Successfully registration", Toast.LENGTH_LONG).show()
                setDefaultAvatar(uid)
                //Token Handler
                FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
                    if (result != null) {
                        val token = result
                        val databaseReference =
                            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("users").child(appUtil.getUID()!!).child("token")
                                .setValue(token)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Token successfully assigned -> $token")
                                    startActivity(Intent(activity, DashBoard::class.java))
                                }
                                .addOnFailureListener {
                                    Log.d(TAG, "Error token not assigned -> $it")
                                }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Is impossible to update your data", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Utente NON salvato nel db")
            }
    }

    private fun setDefaultAvatar(uid: String) {
        val storageReference = FirebaseStorage.getInstance().reference
        val storageRef =
            FirebaseStorage.getInstance().reference.child(AppConstants.PATH + "default-avatar")
        val localFile = File.createTempFile("tempImage", "jpeg")
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                val url = localFile.toUri()
                storageReference!!.child(AppConstants.PATH + uid).putFile(url!!)
                    .addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            "Default avatar loaded and saved",
                            Toast.LENGTH_SHORT
                        ).show()
                        bindUrlToUSer(uid)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Default avatar image loading failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bindUrlToUSer(uid: String) {
        FirebaseStorage.getInstance().reference.child(AppConstants.PATH + uid).downloadUrl
            .addOnSuccessListener {
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/users").child(uid).child("image").setValue(it.toString())
                    .addOnSuccessListener {
                        Log.d(TAG, "Url image correctly bind to userDB")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Error to bind url image -> $it")
                    }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error to bind url image -> $it")
            }
    }


}
package com.example.kotlinmessenger.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.AppUtil
import com.example.kotlinmessenger.DashBoard
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class f_login : Fragment(R.layout.f_login) {

    lateinit var binding : FLoginBinding
    private lateinit var appUtil: AppUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FLoginBinding.inflate(inflater, container, false)
        binding.loginButton.setOnClickListener {
            performLogin()
        }
        appUtil=AppUtil()
        binding.signUpButton.setOnClickListener {
            val fragment = f_register()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
        }
        return binding.root
    }

    private fun performLogin(){
        var email = binding.mailET.text.toString()
        var password= binding.passwordET.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(activity,"Email or Password fields are required", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("Login_activity","Login with email $email and password $password")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(!it.isSuccessful){
                    return@addOnCompleteListener
                }
                FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
                    if (result != null) {
                        val token = result
                        val databaseReference =
                            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("users")
                                .child(appUtil.getUID()!!)
                        Log.d("token", "IL TOKEN è $token")
                        val map: MutableMap<String, Any> = HashMap()
                        map["token"] = token!!
                        databaseReference.updateChildren(map)
                    }
                }
                Toast.makeText(activity,"Loggato con successo $email",Toast.LENGTH_SHORT).show()

                    var intent = Intent(activity, DashBoard::class.java)
                    startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this.activity,"Failed to Login: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
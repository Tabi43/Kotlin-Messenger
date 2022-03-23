package com.example.kotlinmessenger.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.DashBoard
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FLoginBinding
import com.google.firebase.auth.FirebaseAuth

class f_login : Fragment(R.layout.f_login) {

    lateinit var binding : FLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FLoginBinding.inflate(inflater, container, false)

        binding.loginButton.setOnClickListener {
            performLogin()
        }

        binding.signUpButton.setOnClickListener {
            val fragment = f_register()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
        }
        //autologin()
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
                //ha funzionato
                Toast.makeText(activity,"Loggato con successo $email",Toast.LENGTH_SHORT).show()
                //info visibili su logcat
                Log.d("Main Activity", "Login effettuato con successo ${it.result?.user?.uid}")
                var intent = Intent(activity, DashBoard::class.java)
                startActivity(intent)
                //start activity dopo aver effettuato il login
                //TODO:"Inserire la schermata home"
                //val intent= Intent(this, ::class.java)
                //intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                //startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this.activity,"Failed to Login: ${it.message}", Toast.LENGTH_LONG).show()
                //info visibili su logcat
                Log.d("Main Activity", "sei un coglione")
            }
    }

    /*private fun autologin(){
        val email= "prova@gmail.com"
        val password= "123456"
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                var intent = Intent(activity, DashBoard::class.java)
                startActivity(intent)
            }
    }*/

}
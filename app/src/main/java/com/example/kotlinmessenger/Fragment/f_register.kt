package com.example.kotlinmessenger.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.Activity.DashBoard
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase



class f_register : Fragment(R.layout.f_register) {

    lateinit var binding: FRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FRegisterBinding.inflate(inflater, container, false)

        binding.confirmButton.setOnClickListener {
            //TODO: Funzione per il register fragment
        }

        binding.loginreturnButton.setOnClickListener {
            val fragment = f_login()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(R.id.fragmentContainer,fragment)?.commit()
        }
        binding.confirmButton.setOnClickListener {
            performRegistration()
        }

        return binding.root
    }

    private fun performRegistration() {
        var email = binding.mailET.text.toString()
        var password = binding.passwordET.text.toString()
        var confirm_password=binding.confirmPasswordET.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Please fill out the fields", Toast.LENGTH_LONG).show()
            return
        }
        Log.d("Main Activity","$password :check $confirm_password" )
        if ( password!=confirm_password) {
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
                //ha funzionato
                //info visibili su logcat
                Log.d("Main Activity", "Utente creato con successo ${it.result?.user?.uid}")
                //Dati dell'utente
                saveUserToFirebaseDatabase()

            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to create user: ${it.message}", Toast.LENGTH_LONG)
                    .show()
                //info visibili su logcat
                Log.d("Main Activity", "sei un coglione")
            }
    }//fine performRegistration

    private fun saveUserToFirebaseDatabase() {
        Log.d("Register Activity", "Chiamata salvataggio dati utente")
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")
        Log.d("Register Activity", "Reference : $ref")
        //val user = User(uid, binding.username.text.toString(), "Hello")
        val user = mapOf(
            "uid" to uid,
            "username" to binding.username.text.toString(),
            "status" to "Hello"
        )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register Activity", "Utente salvato nel db")
                Toast.makeText(activity, "Successfully registration into db ", Toast.LENGTH_LONG)
                    .show()
                /*//start activity dopo aver creato l utente
                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)*/
                var intent = Intent(activity, DashBoard::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(activity, "impossibile salvare nel db", Toast.LENGTH_LONG).show()
                Log.d("Register Activity", "Utente NON salvato nel db")
            }
    }
}
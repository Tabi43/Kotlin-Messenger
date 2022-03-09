package com.example.kotlinmessenger

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

        binding.confirmButton.setOnClickListener {
            performRegistration()
        }

        return binding.root
    }

    private fun performRegistration() {
        var email = binding.mailET.text.toString()
        var password = binding.passwordET.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(activity, "Inserisci sto testo del cazzo", Toast.LENGTH_LONG).show()
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

            }
            .addOnFailureListener {
                Toast.makeText(activity, "Failed to create user: ${it.message}", Toast.LENGTH_LONG)
                    .show()
                //info visibili su logcat
                Log.d("Main Activity", "sei un coglione")
            }
    }//fine performRegistration

    private fun saveUserToFirebaseDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, binding.username.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register Activity", "Utente salvato nel db")

                /*//start activity dopo aver creato l utente
                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)*/
            }
            .addOnFailureListener {
                Toast.makeText(activity, "impossibile salvare nel db", Toast.LENGTH_LONG).show()
                Log.d("Register Activity", "Utente NON salvato nel db")
            }
    }
}
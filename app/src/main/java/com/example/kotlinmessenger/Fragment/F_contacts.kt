package com.example.kotlinmessenger.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.UserModel
import com.example.kotlinmessenger.adapter.ContactAdapter
import com.example.kotlinmessenger.databinding.FContactsBinding
import com.example.kotlinmessenger.permissions.AppPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


//https://youtu.be/TCA9R2LsOcQ?t=319

class f_contacts : Fragment(R.layout.f_contacts) {

    //var collection = ArrayList<UserModel>()
    //var dimension : Int = 0

    private lateinit var appPermission: AppPermission
    private var contactAdapter: ContactAdapter? = null
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var binding: FContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FContactsBinding.inflate(inflater, container, false)
        appPermission = AppPermission()
        firebaseAuth = FirebaseAuth.getInstance()
        binding.contactSearchView.setOnQueryTextListener(object :

            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("Ricerca", "${searchUser(newText!!)}")
                searchUser(newText)
                return true
            }
        })
        showAllUsers()
        return binding.root
    }

    private fun searchUser(key: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        var result = ArrayList<UserModel>()
        val ref =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users")
        ref.get()
            .addOnFailureListener() {
                Toast.makeText(activity, "Failed to search users", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                Log.d("Search", "GET: ${it}")
                it.children.forEach {
                    val username = it.child("name").value.toString()
                    val status = it.child("status").value.toString()
                    val id = it.child("uid").value.toString()
                    val image = it.child("image").value.toString()
                    if (username.contains(key,true) && uid != id) {
                        //Log.d("url ","aggiunto:${it} id:${id}" )
                        Log.d("url ", "aggiunto url foto: per id:${id}")
                        result.add(UserModel(username, status, image,id, "",""))
                        Log.d("Contatti","(S)Dimension $result")
                    }
                }
                //Adapter
                Log.d("Search", "Utenti trovati: ${result}")
                updateRecyclerView(result)
            }
    }

    private fun showAllUsers(){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        var result = ArrayList<UserModel>()
        val ref =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/users")
        ref.get()
            .addOnFailureListener() {
                Toast.makeText(activity, "Failed to search users", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                //dimension = it.childrenCount.toInt() - 1
                Log.d("Search", "GET: ${it}")
                it.children.forEach {
                    val username = it.child("name").value.toString()
                    val status = it.child("status").value.toString()
                    val id = it.child("uid").value.toString()
                    val image = it.child("image").value.toString()
                    if (uid != id) {
                        //Log.d("url ","aggiunto:${it} id:${id}" )
                        Log.d("url ", "aggiunto url foto: per id:${id}")
                        result.add(UserModel(username, status, image,id, "",""))
                        Log.d("Contatti","(S)Dimension $result")
                    }
                }
                //Adapter
                Log.d("Search", "Utenti trovati: ${result}")
                updateRecyclerView(result)
            }
    }

    private fun updateRecyclerView(collection: ArrayList<UserModel>) {
        if(collection.size == 0){
            binding.noUsersTV.visibility = View.VISIBLE
        }else{
            binding.noUsersTV.visibility = View.INVISIBLE
        }
        binding.recyclerViewContact.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            contactAdapter = ContactAdapter(collection)
            adapter = contactAdapter
        }
    }

}
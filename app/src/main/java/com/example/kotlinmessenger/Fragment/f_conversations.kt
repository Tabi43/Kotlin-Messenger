package com.example.kotlinmessenger.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.*
import com.example.kotlinmessenger.databinding.ChatItemLayoutBinding
import com.example.kotlinmessenger.databinding.FConversationsBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.adapter.ChatAdapter
import com.example.kotlinmessenger.adapter.ContactAdapter
import com.google.firebase.auth.FirebaseAuth

//https://youtu.be/TCA9R2LsOcQ?t=319

class f_conversations : Fragment(R.layout.f_conversations) {

    private lateinit var binding: FConversationsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var chatAdapter: ChatAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FConversationsBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        getAllActiveChat()

        return binding.root
    }

    private fun getAllActiveChat() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        var chats = ArrayList<ChatModel>()

        val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/chatlist")
        ref.get()
            .addOnSuccessListener {
                it.children.forEach {
                    if(it.key.toString().equals(uid)){
                        it.children.forEach {
                            val lastMessage = it.child("Last Message").value.toString()
                            val date = it.child("date").value.toString()
                            val chatID = it.child("chatId").value.toString()
                            val hisId = it.child("member").value.toString()
                            chats.add(ChatModel(chatID,"",lastMessage,"",date,hisId,""))
                        }
                    }
                }
                updateRecycleView(chats)
            }
    }

    private fun updateRecycleView(collection: ArrayList<ChatModel>) {
        binding.recyclerViewChat.apply{
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            chatAdapter = ChatAdapter(collection)
            adapter = chatAdapter
        }

    }
}
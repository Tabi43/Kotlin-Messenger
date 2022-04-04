package com.example.kotlinmessenger.Fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinmessenger.*
import com.example.kotlinmessenger.databinding.FConversationsBinding
import com.google.firebase.database.FirebaseDatabase
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.adapter.ChatAdapter
import com.google.firebase.auth.FirebaseAuth


class f_conversations : Fragment(R.layout.f_conversations) {

    private lateinit var binding: FConversationsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var chatAdapter: ChatAdapter? = null

    private val TAG = "CONVERSATION FRAGMENT"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FConversationsBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        getAllActiveChat()

        return binding.root
    }

    private fun getAllActiveChat() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val chats = ArrayList<ChatModel>()

        val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/chatlist")
        ref.child(uid).get()
            .addOnSuccessListener {
                it.children.forEach {
                    val lastMessage = it.child("lastMessage").value.toString()
                    val date = it.child("date").value.toString()
                    val chatID = it.child("chatId").value.toString()
                    val hisId = it.child("member").value.toString()
                    chats.add(ChatModel(chatID,"",lastMessage,"",date,hisId,""))
                }
                updateRecycleView(chats)
            }
            .addOnFailureListener {
                Toast.makeText(activity,"Query for active chats failed!",Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateRecycleView(collection: ArrayList<ChatModel>) {
        if(collection.size == 0){
            binding.noChatTV.visibility = View.VISIBLE
        }else{
            binding.noChatTV.visibility = View.INVISIBLE
        }
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            chatAdapter = ChatAdapter(collection,binding.noChatTV)
            adapter = chatAdapter
        }

    }
}
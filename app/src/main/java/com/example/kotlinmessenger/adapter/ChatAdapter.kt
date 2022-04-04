package com.example.kotlinmessenger.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.AppUtil
import com.example.kotlinmessenger.ChatModel
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.Fragment.f_conversations
import com.example.kotlinmessenger.MessageActivity
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ChatItemLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ChatAdapter(private val chatList: ArrayList<ChatModel>,private val noChatTV: TextView) :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val TAG = "CHAT ADAPTER"

    class ViewHolder(val chatItemLayoutBinding: ChatItemLayoutBinding):
            RecyclerView.ViewHolder(chatItemLayoutBinding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val chatItemLayoutBinding = ChatItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(chatItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val util = AppUtil()
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val chatModel = chatList[position]
        val rawDate = chatModel.date
        chatModel.date = util.getTimeAgo(rawDate!!.toLong())
        val databaseReference = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("users")
            databaseReference.child(chatModel.hisID.toString()).addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(!snapshot.exists()) databaseReference.removeEventListener(this)
                        else {
                            Log.d("CHAT ADAPETR", "Load data user or changed")
                            chatModel.name = snapshot.child("name").getValue().toString()
                            chatModel.online = snapshot.child("online").getValue().toString()
                            chatModel.image = snapshot.child("image").getValue().toString()
                            if (chatModel.image!!.isEmpty()) {
                                FirebaseStorage.getInstance().reference.child(AppConstants.PATH + chatModel.hisID.toString()).downloadUrl
                                    .addOnSuccessListener {
                                        chatModel.image = it.toString()
                                        Log.d(TAG, "Url image loaded successfully")
                                        holder.chatItemLayoutBinding.chatModel = chatModel
                                    }
                                    .addOnFailureListener {
                                        Log.d(TAG, "Failed to load url image -> $it")
                                    }
                            }
                            holder.chatItemLayoutBinding.chatModel = chatModel
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG,"error: $error")
                    }
                })
        val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("/chatlist")
        ref.child(uid).child(chatModel.chatID.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) ref.removeEventListener(this)
                else {
                    chatModel.lastMessage = snapshot.child("lastMessage").value.toString()
                    //chatModel.date = util.getTimeAgo(snapshot.child("date").value.toString().toLong())
                    var raw = snapshot.child("date").value.toString()
                    Log.d("CHAT ADAPTER", "Load data user or changed $raw")
                    chatModel.date = util.getTimeAgo(raw.toLong())
                    holder.chatItemLayoutBinding.chatModel = chatModel
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG,"error: $error")
            }
        })

        holder.chatItemLayoutBinding.container.setOnClickListener{
            val intent = Intent(it.context, MessageActivity::class.java)
            intent.putExtra("hisId", chatModel.hisID)
            intent.putExtra("hisImage", chatModel.image)
            it.context.startActivity(intent)
        }

        holder.chatItemLayoutBinding.deleteChat.setOnClickListener {
            chatList.removeAt(position)
            this.notifyItemRemoved(position)
            val databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("chatlist").child(uid).child(chatModel.chatID!!).removeValue()
            val databaseref =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("chat").child(chatModel.chatID!!).removeValue()
            if(chatList.size == 0) {
                noChatTV.visibility = View.VISIBLE
            }
        }
        holder.chatItemLayoutBinding.chatModel = chatModel
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

}
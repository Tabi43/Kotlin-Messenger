package com.example.kotlinmessenger.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.AppUtil
import com.example.kotlinmessenger.ChatModel
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.MessageActivity
import com.example.kotlinmessenger.databinding.ChatItemLayoutBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ChatAdapter(private val chatList: ArrayList<ChatModel>) :
        RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(val chatItemLayoutBinding: ChatItemLayoutBinding):
            RecyclerView.ViewHolder(chatItemLayoutBinding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val chatItemLayoutBinding = ChatItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(chatItemLayoutBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var util = AppUtil()
        var chatModel = chatList[position]
        var rawDate = chatModel.date
        chatModel.date = util.getTimeAgo(rawDate!!.toLong())
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(chatModel.hisID.toString()).child("name").addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            chatModel.name = snapshot.getValue().toString()
                            Log.d("ADAPTER CHAT","Nome cambiato!")
                            holder.chatItemLayoutBinding.chatModel = chatModel
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        FirebaseStorage.getInstance().reference.child(AppConstants.PATH + chatModel.hisID).downloadUrl
            .addOnSuccessListener {
                chatModel.image = it.toString()
                holder.chatItemLayoutBinding.chatModel = chatModel
            }
        holder.chatItemLayoutBinding.container.setOnClickListener{
            val intent = Intent(it.context, MessageActivity::class.java)
            intent.putExtra("hisId", chatModel.hisID)
            intent.putExtra("hisImage", chatModel.image)
            it.context.startActivity(intent)
        }
        holder.chatItemLayoutBinding.chatModel = chatModel
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

}
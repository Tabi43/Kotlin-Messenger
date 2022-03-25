package com.example.kotlinmessenger.adapter

import com.example.kotlinmessenger.ChatModel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.databinding.ChatItemLayoutBinding
import kotlin.collections.ArrayList

class ChatAdapter(private var appChat: ArrayList<ChatModel>):  {
    RecyclerView.Adapter<ChatAdapter.ViewHolder>(), Filterable {

        private var chats: ArrayList<ChatModel> = appChat

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactAdapter.ViewHolder {
            val chatItemLayoutBinding = ChatItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return ViewHolder(chatItemLayoutBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var chatModel = appChat[position]
            /*Query adapter*/

            holder.chatItemLayoutBinding.imgContactUserInfo.setOnClickListener {
                /**/
            }

            holder.itemView.setOnClickListener {
                /*bla*/
            }

        }

    }

    class ViewHolder(val chatItemLayoutBinding: ChatItemLayoutBinding) :
        RecyclerView.ViewHolder(chatItemLayoutBinding.root) {
    }

}
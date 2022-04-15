package com.example.kotlinmessenger.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.BR
import com.example.kotlinmessenger.MessageModel
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.IsWritingLayoutBinding
import com.example.kotlinmessenger.databinding.LeftItemLayoutBinding
import com.example.kotlinmessenger.databinding.RightItemLayoutBinding
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val messageList: ArrayList<MessageModel>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val TAG = "MESSAGE ADAPTER"

    class ViewHolder(var viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {}

    class LeftViewHolder(var leftItemLayoutBinding: LeftItemLayoutBinding):
        RecyclerView.ViewHolder(leftItemLayoutBinding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var viewDataBinding: ViewDataBinding? = null
        if (viewType == 0)
            viewDataBinding = RightItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        if (viewType == 1) {
            val binding: LeftItemLayoutBinding =
                LeftItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            viewDataBinding = binding
        }
        if (viewType == 2) {
            viewDataBinding = IsWritingLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }
        return ViewHolder(viewDataBinding!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageModel = messageList[position]
        val myId = FirebaseAuth.getInstance().uid ?: ""
        val viewType = if (messageModel.senderId == myId) 0
        else 1
        if (viewType == 0) {
            holder.viewDataBinding.setVariable(BR.message, messageModel)
        }
        if (viewType == 1) {
            holder.viewDataBinding.setVariable(BR.message, messageModel)
            holder.itemView.findViewById<ImageView>(R.id.translateButton).setOnClickListener {
                Log.i(TAG,"hey")
                test(messageModel,holder)
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val myId = FirebaseAuth.getInstance().uid ?: ""
        val messageModel = messageList[position]
        if (messageModel.type == "IS_WRITING") return 2
        else if ((messageModel.senderId == myId)) return 0
        else return 1
    }

    fun test(model: MessageModel, holder: ViewHolder){
        model.message = "BELANDI FUNZIONA !!!!"
        holder.viewDataBinding.setVariable(BR.message,model)
    }
}
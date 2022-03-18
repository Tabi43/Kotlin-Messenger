package com.example. kotlinmessenger

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.adapter.ContactAdapter
import com.example.kotlinmessenger.databinding.ActivityMessageBinding
import com.example.kotlinmessenger.databinding.LeftItemLayoutBinding
import com.example.kotlinmessenger.databinding.RightItemLayoutBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage

class MessageActivity : AppCompatActivity() {

    private lateinit var activityMessageBinding: ActivityMessageBinding
    private var hisId: String? = null
    private var hisImageUrl: String? = null
    private var chatId: String? = null
    private lateinit var myId: String
    private lateinit var appUtil: AppUtil
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<MessageModel, ViewHolder>? = null
    private lateinit var myImage: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        activityMessageBinding = ActivityMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(activityMessageBinding.root)

        appUtil = AppUtil()
        myId = appUtil.getUID()!!
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        myImage = sharedPreferences.getString("myImage", "").toString()

        activityMessageBinding.activity = this

        hisId = intent.getStringExtra("hisId")
        hisImageUrl = intent.getStringExtra("hisImage")

        activityMessageBinding.btnSend.setOnClickListener {
            val message: String = activityMessageBinding.msgText.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(message)
            }
        }

        if (chatId == null) CheckChat(hisId!!)

        FirebaseStorage.getInstance().reference.child(AppConstants.PATH + hisId).downloadUrl
            .addOnSuccessListener {
                Log.d("Chat", "HIS URL: ${it.toString()}")
                activityMessageBinding.hisImage = it.toString()
            }
        FirebaseStorage.getInstance().reference.child(AppConstants.PATH + myId).downloadUrl
            .addOnSuccessListener {
                Log.d("Chat", "Our URL: ${it.toString()}")
                myImage = it.toString()
            }
        checkOnlineStatus()
    }

    private fun CheckChat(hisId: String) {
        Log.d("Chat", "Check chat id: $hisId")
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/chatlist").child(myId)
        databaseReference.get().addOnSuccessListener {
            it.children.forEach {
                if (it.child("member").value == hisId) {
                    Log.d("Chat", "Chat ID rilevato: ${it.key}")
                    chatId = it.key
                    readMessages(chatId!!)
                }
            }
        }
    }

    private fun CreateChat(message: String) {
        Log.d("Message", "Chat created: $message")
        var databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/chatlist").child(myId)
        chatId = databaseReference.push().key
        val chatListMod =
            ChatListModel(chatId!!, "Say Hi!", System.currentTimeMillis().toString(), hisId!!)
        databaseReference.child(chatId!!).setValue(chatListMod)
        databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/chatlist").child(hisId!!)
        val chatList =
            ChatListModel(chatId!!, "Say Hi!", System.currentTimeMillis().toString(), myId!!)
        databaseReference.child(chatId!!).setValue(chatList)
        readMessages(chatId!!)
        sendMessage(message)

    }

    private fun sendMessage(message: String) {
        activityMessageBinding.msgText.text = null
        Log.d("Message", "Send : $message")
        if (chatId == null) {
            Log.d("Message", "ID NULL")
            CreateChat(message)
        } else {
            Log.d("Message", "SEND OK")
            var databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/chat").child(chatId!!)
            val messageModel = MessageModel(myId, hisId!!, message, type = "text")
            databaseReference.push().setValue(messageModel)
            val Map: MutableMap<String, Any> = HashMap()
            Map["Last Message"] = message
            Map["Date"] = System.currentTimeMillis().toShort().toInt()
            databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/chatlist").child(myId).child(chatId!!)
            databaseReference.updateChildren(Map)
            databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/chatlist").child(hisId!!).child(chatId!!)
            databaseReference.updateChildren(Map)
        }//end else
    }

    private fun readMessages(chatId: String) {
        Log.d("Message", "Read  chat id: $chatId")
        val query = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("chat").child(chatId)

        val firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<MessageModel>()
            .setLifecycleOwner(this)
            .setQuery(query, MessageModel::class.java)
            .build()
        query.keepSynced(true)

        firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<MessageModel, ViewHolder>(firebaseRecyclerOptions) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

                    var viewDataBinding: ViewDataBinding? = null

                    if (viewType == 0)
                        viewDataBinding = RightItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    if (viewType == 1)
                        viewDataBinding = LeftItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    return ViewHolder(viewDataBinding!!)
                }

                override fun onBindViewHolder(
                    holder: ViewHolder,
                    position: Int,
                    messageModel: MessageModel
                ) {
                    if (getItemViewType(position) == 0) {
                        holder.viewDataBinding.setVariable(BR.message, messageModel)
                        //holder.viewDataBinding.setVariable(BR.messageImage,myImage)
                    }
                    if (getItemViewType(position) == 1) {
                        holder.viewDataBinding.setVariable(BR.message, messageModel)
                       // holder.viewDataBinding.setVariable(BR.messageImage, hisImageUrl)
                    }
                }

                override fun getItemViewType(position: Int): Int {
                    val messageModel = getItem(position)
                    return if (messageModel.senderId == myId) 0
                    else 1
                }
            }

        activityMessageBinding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        activityMessageBinding.messageRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter!!.startListening()

    }

    class ViewHolder(var viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root)

    override fun onPause(){
        super.onPause()
        if(firebaseRecyclerAdapter!=null){
            firebaseRecyclerAdapter!!.stopListening()
        }
    }
    override fun onResume() {
        super.onResume()
        appUtil.updateOnlineStatus("online")
    }

   private fun checkOnlineStatus() {
       Log.d("string","ho chiamato la funzione checkOnlineStatus()")

        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(hisId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    activityMessageBinding.online = userModel!!.online
                    Log.d("chat","stato interlocutore: ${activityMessageBinding.online}")


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
   }




}




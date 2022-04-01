package com.example. kotlinmessenger

import android.content.Intent
import android.content.SharedPreferences
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.android.volley.Response
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.example.kotlinmessenger.Constants.AppConstants
import com.android.volley.toolbox.Volley
import com.android.volley.DefaultRetryPolicy
import com.example.kotlinmessenger.AppUtil
import com.example.kotlinmessenger.ChatListModel
import com.example.kotlinmessenger.MessageModel
import com.example.kotlinmessenger.UserModel
import com.example.kotlinmessenger.databinding.ActivityMessageBinding
import com.example.kotlinmessenger.databinding.LeftItemLayoutBinding
import com.example.kotlinmessenger.databinding.RightItemLayoutBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.firebase.ui.database.FirebaseRecyclerOptions
import org.json.JSONObject

class MessageActivity : AppCompatActivity() {

    private lateinit var activityMessageBinding: ActivityMessageBinding
    private var hisId: String? = null
    private var hisImageUrl: String? = null
    private var chatId: String? = null
    private lateinit var myId: String
    private lateinit var appUtil: AppUtil
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<MessageModel, ViewHolder>? = null
    private var myName: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "Message Activity"
    private var existChat:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        activityMessageBinding = ActivityMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(activityMessageBinding.root)

        appUtil = AppUtil()
        myId = appUtil.getUID()!!
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        activityMessageBinding.activity=this

        hisId = intent.getStringExtra("hisId")
        hisImageUrl = intent.getStringExtra("hisImage")
        activityMessageBinding.hisImage = hisImageUrl

        activityMessageBinding.btnSend.setOnClickListener {
            val message: String = activityMessageBinding.msgText.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(message)
                gettoken(message)
            }
        }

        activityMessageBinding.messageToolbar.msgBack.setOnClickListener {
            startActivity(Intent(this, DashBoard::class.java))
            finish()
        }

        activityMessageBinding.msgText.addOnLayoutChangeListener(object : View.OnLayoutChangeListener{
            override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                activityMessageBinding.messageRecyclerView.adapter?.itemCount?.let{activityMessageBinding.messageRecyclerView.smoothScrollToPosition(it)}
            }
        })
        activityMessageBinding.messageToolbar.trashChat.setOnClickListener {
            if (existChat) {
                startActivity(Intent(this, DashBoard::class.java))
                val databaseReference =
                    FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                        .getReference("chatlist").child(myId!!).child(chatId!!).removeValue()
                val databaseref =
                    FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                        .getReference("chat").child(chatId!!).removeValue()
                finish()
            }

        }
        if (chatId == null) CheckChat(hisId!!)

        checkOnlineStatus()
        getMyname()

    }

     private fun getMyname(){
         val databaseReference =
             FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                 .getReference("users").child(myId).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                     override fun onDataChange(snapshot: DataSnapshot) {
                         if (snapshot.exists()) {
                             myName = snapshot.getValue().toString()
                             Log.d(TAG, "nome caricato: $myName")
                         }
                     }
                     override fun onCancelled(error: DatabaseError) {

                     }
                 })

     }

    private fun CheckChat(hisId: String) {
        Log.d(TAG, "Check chat id: $hisId")
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("/chatlist").child(myId)
        databaseReference.get().addOnSuccessListener {
            it.children.forEach {
                if (it.child("member").value == hisId) {
                    Log.d(TAG, "Chat ID rilevato: ${it.key}")
                    chatId = it.key
                    readMessages(chatId!!)
                }
            }
        }
    }

    private fun CreateChat(message: String) {
        Log.d(TAG, "Chat created: $message")
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
        Log.d(TAG, "Send : $message")
        if (chatId == null) {
            Log.d(TAG, "ID CHAT NULL")
            CreateChat(message)
        } else {
            Log.d(TAG, "SEND OK")
            var databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/chat").child(chatId!!)
            val messageModel = MessageModel(myId, hisId!!, message, type = "text", date = System.currentTimeMillis().toString())
            databaseReference.push().setValue(messageModel)
            val Map: MutableMap<String, Any> = HashMap()
            Map["lastMessage"] = message
            Map["date"] = System.currentTimeMillis()
            databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/chatlist").child(myId).child(chatId!!)
            databaseReference.updateChildren(Map)
            databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("/chatlist").child(hisId!!).child(chatId!!)
            databaseReference.updateChildren(Map)
        } //end else
    }

    private fun readMessages(chatId: String) {
        Log.d(TAG, "Read  chat id: $chatId")
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
                    if(itemCount>0) existChat=true
                    else existChat=false
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

                override fun onDataChanged() {
                    activityMessageBinding.messageRecyclerView.smoothScrollToPosition(itemCount)
                    super.onDataChanged()
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
        appUtil.updateOnlineStatus("offline")
    }
    override fun onResume() {
        super.onResume()
        appUtil.updateOnlineStatus("online")
    }

   private fun checkOnlineStatus() {
        val databaseReference = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(hisId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    activityMessageBinding.online = userModel!!.online
                    Log.d(TAG,"stato interlocutore: ${activityMessageBinding.online}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
   }

    private fun gettoken(message: String) {
        val databaseReference = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(hisId!!).child("token")
        .addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val token =snapshot.getValue().toString()

                    val to = JSONObject()
                    val data = JSONObject()
                    data.put("hisId", myId)
                    data.put("title", myName)
                    data.put("message", message)
                    data.put("chatId", chatId)

                    to.put("to", token)
                    to.put("data", data)
                    sendNotification(to)

                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun sendNotification(to: JSONObject) {
        Log.d(TAG,"token notifica: $to")
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            AppConstants.NOTIFICATION_URL,
            to,
            Response.Listener { response: JSONObject ->
                Log.d("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.d("TAG", "onError: $it")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val map: MutableMap<String, String> = HashMap()

                map["Authorization"] = "key=" + AppConstants.SERVER_KEY
                map["Content-type"] = "application/json"
                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(request)

    }

}




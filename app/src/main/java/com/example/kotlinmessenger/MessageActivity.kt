package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.adapter.MessageAdapter
import com.example.kotlinmessenger.databinding.ActivityMessageBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File


class MessageActivity : AppCompatActivity() {

    private lateinit var activityMessageBinding: ActivityMessageBinding
    private var hisId: String? = null
    private var hisUsername: String? = null
    private var hisImageUrl: String? = null
    private var chatId: String? = null
    private lateinit var myId: String
    private lateinit var appUtil: AppUtil
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<MessageModel, ViewHolder>? = null
    private var myName: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "MESSAGE ACTIVITY"
    private var existChat: Boolean = false
    private var messageList = ArrayList<MessageModel>()
    private var messageAdapter: MessageAdapter? = null
    private var isTyping = false
    private var t: Long = 0
    private var deadLine: Long = 500
    private var debugTime: Long = 0
    private var box = 0
    private var isWriting = false
    private var isOnline = false
    private var READ_EXTERNAL_STORAGE_REQUEST_CODE=1001
    private var imageUris = ArrayList<String>()


    //Local variable for handling images
    private val PICK_IMAGES_CODE = 0
    private val position = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        activityMessageBinding = ActivityMessageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(activityMessageBinding.root)

        appUtil = AppUtil()
        myId = appUtil.getUID()!!
        sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        activityMessageBinding.activity = this

        hisId = intent.getStringExtra("hisId")
        hisImageUrl = intent.getStringExtra("hisImage")
        hisUsername = intent.getStringExtra("hisUsername")
        activityMessageBinding.hisImage = hisImageUrl
        activityMessageBinding.messageToolbar.username = hisUsername

        activityMessageBinding.btnSend.setOnClickListener {
            val message: String = activityMessageBinding.msgText.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show()
            } else {
                setNoWriting()
                sendMessage(message)
                gettoken(message)
            }
        }

        activityMessageBinding.btnDataSend.setOnClickListener {
            pickImageIntent()
        }

        activityMessageBinding.messageToolbar.msgBack.setOnClickListener {
            startActivity(Intent(this, DashBoard::class.java))
            finish()
        }

        activityMessageBinding.msgText.doOnTextChanged { text, start, before, count ->
            val TIME_FACTOR: Long = 350
            if (!isTyping) {
                Log.d(TAG, "Is typing")
                t = System.currentTimeMillis()
                debugTime = t
                isTyping = true
                setWriting()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        // This method will be executed once the timer is over
                        if (System.currentTimeMillis() > t) {
                            Log.d(TAG, "No more typing (2)")
                            setNoWriting()
                            isTyping = false
                        } else {
                            Log.d(TAG, "difference from stop: ${t - System.currentTimeMillis()}")
                        }
                    },
                    TIME_FACTOR // value in milliseconds
                )
            } else {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        // This method will be executed once the timer is over
                        if (System.currentTimeMillis() > t) {
                            if (isTyping) {
                                Log.d(
                                    TAG,
                                    "No more typing (1) difference ${t - debugTime - deadLine}"
                                )
                                setNoWriting()
                                isTyping = false
                                deadLine = 500
                            }
                        } else {
                            //Log.d(TAG,"difference: ${t-System.currentTimeMillis()}")
                        }
                    },
                    deadLine // value in milliseconds
                )
            }
            t += TIME_FACTOR
            deadLine += (TIME_FACTOR)
            Log.d(TAG, "difference: ${t - System.currentTimeMillis()}")
        }

        activityMessageBinding.msgText.addOnLayoutChangeListener(object :
            View.OnLayoutChangeListener {
            override fun onLayoutChange(
                p0: View?,
                p1: Int,
                p2: Int,
                p3: Int,
                p4: Int,
                p5: Int,
                p6: Int,
                p7: Int,
                p8: Int
            ) {
                activityMessageBinding.messageRecyclerView.adapter?.itemCount?.let {
                    activityMessageBinding.messageRecyclerView.smoothScrollToPosition(
                        it
                    )
                }
            }
        })


        if (chatId == null) CheckChat(hisId!!)

        checkOnlineStatusAndUsername()
        getMyname()
    }

    private fun deleteChat(){
        startActivity(Intent(this, DashBoard::class.java))
        if (messageList.size > 0) {
            val databaseReference =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("chatlist").child(myId!!).child(chatId!!).removeValue()
            val databaseref =
                FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("chat").child(chatId!!).removeValue()
        }
    }

    private fun getMyname() {
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(myId).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    sperimentalReadMessages(chatId!!)
                }
            }
        }
        FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/chatlist").child(hisId).get()
            .addOnFailureListener {
                //Va ricreata una nuova chat
            }
            .addOnSuccessListener {

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
        sperimentalReadMessages(chatId!!)
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
            val messageModel = MessageModel(
                myId,
                hisId!!,
                message,
                type = "text",
                date = System.currentTimeMillis().toString()
            )
            databaseReference.push().setValue(messageModel)
            val Map: MutableMap<String, Any> = HashMap()
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("chatlist").child(hisId!!).child(chatId!!).child("chatid").get()
                .addOnSuccessListener {
                    if (it.exists()) {
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
                    } else {
                        Map["lastMessage"] = message
                        Map["date"] = System.currentTimeMillis()
                        Map["chatId"] = chatId!!
                        Map["member"] = hisId!!
                        databaseReference =
                            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("/chatlist").child(myId).child(chatId!!)
                        databaseReference.updateChildren(Map)
                        Map["member"] = myId
                        databaseReference =
                            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                                .getReference("/chatlist").child(hisId!!).child(chatId!!)
                        databaseReference.updateChildren(Map)
                    }
                }
                .addOnFailureListener {
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
                }
        } //end else
    }

    private fun sperimentalReadMessages(chatId: String) {
        //Prima fase caricamento chats
        FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("chat").child(chatId).get()
            .addOnSuccessListener {
                it.children.forEach {
                    val senderId = it.child("senderId").value.toString()
                    val reciverId = it.child("reciverId").value.toString()
                    val message = it.child("message").value.toString()
                    val date = it.child("date").value.toString()
                    val type = it.child("type").value.toString()
                    messageList.add(MessageModel(senderId, reciverId, message, date, type))
                }
                activityMessageBinding.messageRecyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                    setHasFixedSize(true)
                    messageAdapter = MessageAdapter(messageList)
                    adapter = messageAdapter
                    activityMessageBinding.messageRecyclerView.smoothScrollToPosition(messageList.size)
                    syncNewMessages(chatId)
                    writingListener()
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error loading existing messages -> $it")
            }
    }

    private fun syncNewMessages(chatId: String) {
        val query =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("chat").child(chatId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > messageList.size) {
                    Log.d(TAG, "New message found")
                    if(isWriting) removeIsWritingBox()
                    val lastMessageChildren = snapshot.children.last()
                    val senderId = lastMessageChildren.child("senderId").value.toString()
                    val reciverId = lastMessageChildren.child("reciverId").value.toString()
                    val message = lastMessageChildren.child("message").value.toString()
                    val date = lastMessageChildren.child("date").value.toString()
                    val type = lastMessageChildren.child("type").value.toString()
                    messageList.add(MessageModel(senderId, reciverId, message, date, type))
                    activityMessageBinding.messageRecyclerView.adapter!!.notifyDataSetChanged()
                    activityMessageBinding.messageRecyclerView.smoothScrollToPosition(messageList.size)
                } else {
                    Log.d(
                        TAG,
                        "No new message found children: ${snapshot.childrenCount} array: ${messageList.size}"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    class ViewHolder(var viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root)

    override fun onPause() {
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter!!.stopListening()
        }
        appUtil.updateOnlineStatus("offline")
        super.onPause()
    }

    override fun onResume() {
        appUtil.updateOnlineStatus("online")
        super.onResume()
    }

    private fun checkOnlineStatusAndUsername() {
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(hisId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (!isOnline && snapshot.child("online").value == "online") {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        activityMessageBinding.online = userModel!!.online
                        Log.d(TAG, "stato interlocutore: ${activityMessageBinding.online}")
                        isOnline = true
                    }
                    if (isOnline && snapshot.child("online").value == "offline") {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        activityMessageBinding.online = userModel!!.online
                        Log.d(TAG, "stato interlocutore: ${activityMessageBinding.online}")
                        isOnline = false
                    }
                    if(snapshot.child("name").value != hisUsername){
                        hisUsername = snapshot.child("name").value.toString()
                        activityMessageBinding.messageToolbar.username = hisUsername
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun writingListener() {
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(hisId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Him typing: ${snapshot.child("typing").value}")
                if (snapshot.child("typing").value == "true" && !isWriting) {
                    messageList.add(
                        MessageModel(
                            hisId!!,
                            myId,
                            "...",
                            System.currentTimeMillis().toString(),
                            "IS_WRITING"
                        )
                    )
                    box = messageList.size - 1
                    isWriting = true
                    activityMessageBinding.messageRecyclerView.adapter!!.notifyDataSetChanged()
                    activityMessageBinding.messageRecyclerView.smoothScrollToPosition(messageList.size)
                }
                if (snapshot.child("typing").value == "false" && isWriting) {
                    messageList.removeAt(box)
                    isWriting = false
                    activityMessageBinding.messageRecyclerView.adapter!!.notifyDataSetChanged()
                    activityMessageBinding.messageRecyclerView.smoothScrollToPosition(messageList.size)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun removeIsWritingBox() {
        if (isWriting) {
            messageList.removeAt(box)
            isWriting = false
            activityMessageBinding.messageRecyclerView.adapter!!.notifyDataSetChanged()
            activityMessageBinding.messageRecyclerView.smoothScrollToPosition(messageList.size)
        }
    }

    private fun setWriting() {
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(myId).child("typing").setValue("true")
    }

    private fun setNoWriting() {
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(myId).child("typing").setValue("false")
    }

    private fun gettoken(message: String) {
        val databaseReference =
            FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users").child(hisId!!).child("token")
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val token = snapshot.getValue().toString()

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
        Log.d(TAG, "token notifica: $to")
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

    private val getAction =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                Log.d(TAG, "Photo selected: ${result.data}")
                if(result.data!!.clipData != null){
                    val count = result.data!!.clipData!!.itemCount-1
                    Log.d(TAG,"Selezionata più di un immagine: ${result.data!!.clipData!!.itemCount} immagini -> ${result.data!!.clipData} ")
                    for (i in 0..count) {
                        imageUris.add(result.data!!.clipData!!.getItemAt(i).uri.toString())
                    }
                    Log.d(TAG,"Gli uri dio bestia sono: ${imageUris}")
                }else{
                    Log.d(TAG,"Selezionata una sola immagine: ${result.data!!.data}")
                    imageUris.add(result.data!!.data!!.toString())
                    val file = Uri.fromFile(File(imageUris[0]))
                    activityMessageBinding.messageToolbar.hisImage = file.toString()
                }

                val intent = Intent(this, SendmediaService::class.java)
                intent.putExtra("hisID", hisId)
                intent.putExtra("chatID", chatId)
                intent.putStringArrayListExtra("media",imageUris)

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    startForegroundService(intent)
                else
                    startService(intent)
            }
        }

    private fun pickImageIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setAction(Intent.ACTION_GET_CONTENT)
        getAction.launch(intent)
    }

    private fun compressImage(fileName: String): String? {
        var newPath = ""
        val imageFile = File(fileName)
        lifecycleScope.launch {
            // Default compression
            var compressedImage = Compressor.compress(this@MessageActivity, imageFile)
            newPath = compressedImage.absolutePath.toUri().toString()
        }
        return newPath
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG,"items selected: ${data}")
        if (resultCode == Activity.RESULT_OK ) {
            imagesUri= data?.getData() as Nothing?

            if (chatId == null)
                Toast.makeText(this, "Please send text message first", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show()

                //val intent = Intent(this, SendmediaService::class.java)
                /*intent.putExtra("hisID", hisId)
                intent.putExtra("chatID", chatId)
                intent.putStringArrayListExtra("media", returnValue)*/

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    startForegroundService(intent)
                else
                    startService(intent)
            }

        }
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            READ_EXTERNAL_STORAGE_REQUEST_CODE-> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageIntent()
                } else {
                    Toast.makeText(
                        this@MessageActivity,
                        "Approve permissions to open Pix ImagePicker",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }


    override fun onDestroy() {
        setNoWriting()
        appUtil.updateOnlineStatus("offline")
        super.onDestroy()
    }
}









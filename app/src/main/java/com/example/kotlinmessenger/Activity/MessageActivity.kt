package com.example.kotlinmessenger.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlinmessenger.AppUtil
import com.example.kotlinmessenger.ChatListModel
import com.example.kotlinmessenger.MessageModel
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityMessageBinding
import com.google.firebase.database.*

class MessageActivity : AppCompatActivity() {
    private lateinit var activityMessageBinding: ActivityMessageBinding
    private var hisId: String?= null
    private var hisImage: String?=null
    private var chatId:String?=null
    private lateinit var myId:String
    private lateinit var appUtil: AppUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        appUtil= AppUtil()
        myId=appUtil.getUID()!!
        hisId= intent.getStringExtra("hisId")
        hisImage= intent.getStringExtra("hisImage")

        activityMessageBinding.btnSend.setOnClickListener({
            val message:String=activityMessageBinding.msgText.text.toString()
            if(message.isEmpty()){
                Toast.makeText(this,"Enter Message",Toast.LENGTH_SHORT).show()
            }else{
                sendMessage(message)
            }
        })

        if(chatId!=null) CheckChat(hisId!!)


    }

    private fun CheckChat(hisId:String) {
        val databaseReference= FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/messages").child(myId)
        val query=databaseReference.orderByChild("members").equalTo(hisId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (ds in snapshot.children){
                        val member:String = ds.child("member").value.toString()
                        if(hisId==member){
                            chatId=ds.key
                            break
                        }
                    }//end for

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    private fun CreateChat(message:String){
        var databaseReference= FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/chatlist").child(myId)
        chatId=databaseReference.push().key
        val chatListMod= ChatListModel(chatId!!,message,System.currentTimeMillis().toString(),hisId!!)
        databaseReference.child(chatId!!).setValue(chatListMod)
        databaseReference= FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/chatlist").child(hisId!!)
        val chatList=ChatListModel(chatId!!,message,System.currentTimeMillis().toString(),myId!!)
        databaseReference.child(chatId!!).setValue(chatList)
        databaseReference=FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("/chat").child(chatId!!)
        val messageModel=MessageModel(myId,hisId!!,message, type = "text")
        databaseReference.push().setValue(messageModel)
    }
    private fun sendMessage(message:String){
       if(chatId==null){
           CreateChat(message)
       }else{
           var databaseReference= FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app")
               .getReference("/chat").child(chatId!!)
           val messageModel=MessageModel(myId,hisId!!,message, type = "text")
           databaseReference.push().setValue(messageModel)
       }
        val Map:MutableMap<String,Any> = HashMap()
    }
}
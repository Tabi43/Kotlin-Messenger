package com.example.kotlinmessenger

import android.content.Context
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinmessenger.Constants.AppConstants
import com.example.kotlinmessenger.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var username: String
    private lateinit var status: String
    private lateinit var imageUrl: String

    private var selectedPhotoUri: Uri? = null

    private var databaseReference: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var storageReference: StorageReference? = null

    private lateinit var binding: ActivityProfileBinding
    //private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference
        //sharedPreferences = this.getSharedPreferences("userData", Context.MODE_PRIVATE)

        binding.btnDataDone.setOnClickListener {
            if(checkData()){
                //uploadData(username,status,selectedPhotoUri!!)
                if(selectedPhotoUri == null){
                    uploadData(username,status)
                }else{
                    uploadData(username,status,selectedPhotoUri!!)
                    //uploadImageToFireDataBase(selectedPhotoUri!!)
                }
            }
        }

        binding.imgPickImage.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")
            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            getAction.launch(intent)
        }
        loadData()
    }

    private val getAction= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode==Activity.RESULT_OK && result.data!=null){
            Log.d("RegisterActivity","Photo was selected")
            selectedPhotoUri= result.data?.data//location di dove l immagine è stata memorizzata
            //caricamento immagine sulla pagina di login
            val bitmap= MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            //val bitmapDrawable=BitmapDrawable(bitmap)
            //buttonSelectPhoto.setBackgroundDrawable(bitmapDrawable)
            binding.imgUser.setImageBitmap(bitmap)
        }
    }

    /*
    * buttonSelectPhoto.setOnClickListener {
    * */

    private fun checkData(): Boolean {
        username = binding.usernameET.text.toString().trim()
        status = binding.userStatusET.text.toString().trim()
        Log.d("Profile","Check data")
        if (username.isEmpty()) {
            binding.usernameET.error = "Filed is required"
            return false
        } else if (status.isEmpty()) {
            binding.userStatusET.error = "Filed is required"
            return false
        } else return true
    }

    //Funzione che carica i dati dell'utente nell'interfaccia
    private fun loadData(){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        if(uid != ""){
            val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")
            val data = ref.get()
                .addOnFailureListener {
                    Toast.makeText(this,"Fetching error: $it",Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener {
                    Toast.makeText(this,"Data Fetched",Toast.LENGTH_SHORT).show()
                    val status = it.child("status").value
                    val username = it.child("username").value
                    binding.usernameET.setText(username.toString())
                    binding.userStatusET.setText(status.toString())
                    //Caricamento e visualizzazione dell'immagine da Firebase
                    val storageRef = FirebaseStorage.getInstance().reference.child(AppConstants.PATH + uid)
                    val localFile = File.createTempFile("tempImage","jpeg")
                    storageRef.getFile(localFile)
                        .addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            binding.imgUser.setImageBitmap(bitmap)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"Image loading failed", Toast.LENGTH_SHORT).show()
                        }
                }
        }else{
            Toast.makeText(this,"You are not logged",Toast.LENGTH_LONG).show()
        }


    }

    //Aggiorna solo stato e username
    private fun uploadData(name: String, status: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")
        val map = mapOf(
            "status" to status,
            "uid" to firebaseAuth!!.uid,
            "username" to name,
        )
        ref.setValue(map)
            .addOnSuccessListener {
                Log.d("Register Activity", "Utente salvato nel db")
                /*//start activity dopo aver creato l utente
                val intent = Intent(this, LatestMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)*/
            }
            .addOnFailureListener {
                Toast.makeText(this, "impossibile salvare nel db", Toast.LENGTH_LONG).show()
                Log.d("Register Activity", "Utente NON salvato nel db")
            }
    }

    //Modifica nome, stato e foto
    private fun uploadData(name: String, status: String, image: Uri) = kotlin.run {
        storageReference!!.child(AppConstants.PATH + firebaseAuth!!.uid).putFile(image)
            .addOnSuccessListener {
                Log.d("Profile Activity","Aggiornamento dei date dell'account")
                val task = it.storage.downloadUrl
                task.addOnCompleteListener { uri ->
                    imageUrl = uri.result.toString()
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("/users/$uid")
                    val map = mapOf(
                        "status" to status,
                        "uid" to firebaseAuth!!.uid,
                        "username" to name,
                    )
                    ref.setValue(map)
                        .addOnSuccessListener {
                            Log.d("Register Activity", "Utente salvato nel db")
                            /*//start activity dopo aver creato l utente
                            val intent = Intent(this, LatestMessageActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)*/
                            /*val editor=sharedPreferences.edit()
                            editor.putString("myImage",imageUrl).apply()*/
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "impossibile salvare nel db", Toast.LENGTH_LONG).show()
                            Log.d("Register Activity", "Utente NON salvato nel db")
                        }
                    //startActivity(Intent(this, DashBoard::class.java))
                    //finish()
                }
            }
    }

    private fun uploadImageToFireDataBase( image: Uri){
        storageReference!!.child(AppConstants.PATH + "default-avatar").putFile(image)
            .addOnSuccessListener {
                Toast.makeText(this,"File uploaded",Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    //Verifica se è concesso il permesso per accedere alla memoria
    private fun storageRequestPermission() = ActivityCompat.requestPermissions(
        this,
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 1000
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                else Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

}
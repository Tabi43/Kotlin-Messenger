package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.Fragment.f_contacts
import com.example.kotlinmessenger.Fragment.f_conversations
import com.example.kotlinmessenger.Fragment.f_settings
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityDashBoardBinding

class DashBoard : AppCompatActivity() {

    lateinit var binding: ActivityDashBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val chatListF = f_conversations()
        val contactsF = f_contacts()
        val settingsF = f_settings()

        replaceFragment(chatListF)

        var intent = Intent(this, ProfileActivity::class.java)


        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.chat -> replaceFragment(chatListF)
                R.id.contacts -> replaceFragment(contactsF)
                R.id.settings -> startActivity(intent)
            }
            return@setOnItemSelectedListener true
        }


    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayoutWrapper, fragment).commit()
    }

}
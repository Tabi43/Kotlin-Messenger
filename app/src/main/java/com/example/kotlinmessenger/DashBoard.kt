package com.example.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
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

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.chat -> replaceFragment(chatListF)
                R.id.contacts -> replaceFragment(contactsF)
                R.id.settings -> replaceFragment(settingsF)
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayoutWrapper, fragment).commit()
    }

    /* Da aggiungere dentro onCreate



        * binding=DashBinging.inflate(LayoutInflater)
        setContetnView(binding.root)


        binding.contacs_button.setOnClickListener {
        replaceFragment(FragmentHome())
        }

        binding.chat_button.setOnClickListener {
        replaceFragment(FragmentChat())
        }

        binding.settings_buttonsetOnClickListener {
        replaceFragment(FragmentSettings())
        }

        Private fun replaceFragment(fragment: Fragment) {
        val fragmentManager=supportFragmentManager
        val fragmentTransaction=fragmentManager.beginTranscation()
        fragmentTransaction.replace( R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
        }
    *
    * */


}
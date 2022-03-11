package com.example.kotlinmessenger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.databinding.FConversationsBinding

class f_conversations : Fragment(R.layout.f_conversations) {

    lateinit var binding: FConversationsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FConversationsBinding.inflate(inflater, container, false)

        return binding.root
    }
}
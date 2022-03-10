package com.example.kotlinmessenger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.databinding.FConverationsBinding

class f_conversations : Fragment(R.layout.f_converations) {

    lateinit var binding: FConverationsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FConverationsBinding.inflate(inflater, container, false)

        return binding.root
    }
}
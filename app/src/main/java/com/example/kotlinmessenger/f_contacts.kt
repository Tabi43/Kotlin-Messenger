package com.example.kotlinmessenger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.databinding.FContactsBinding

class f_contacts : Fragment(R.layout.f_contacts) {

    lateinit var binding: FContactsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FContactsBinding.inflate(inflater, container, false)

        return binding.root
    }
}
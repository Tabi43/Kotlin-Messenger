package com.example.kotlinmessenger.Fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FSettingsBinding

class f_settings : Fragment(R.layout.f_settings) {

    lateinit var binding: FSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FSettingsBinding.inflate(inflater, container, false)

        getActivity()?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        return binding.root
    }
}
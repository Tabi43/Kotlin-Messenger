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
import com.example.kotlinmessenger.databinding.FUserdataBinding


class f_userdata:Fragment(R.layout.f_userdata) {

    lateinit var binding: FUserdataBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FUserdataBinding.inflate(inflater, container, false)

        getActivity()?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        return binding.root
    }
}
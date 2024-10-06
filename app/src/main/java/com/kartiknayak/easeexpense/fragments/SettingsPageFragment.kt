package com.kartiknayak.easeexpense.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kartiknayak.easeexpense.SharedFunctions
import com.kartiknayak.easeexpense.databinding.FragmentSettingsPageBinding


class SettingsPageFragment : Fragment() {
    private lateinit var binding: FragmentSettingsPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsPageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
    }

    private fun setListeners() {
        val (_, authEnabled) = SharedFunctions().getAppBootData(requireContext())

        binding.apply {
            authSwitch.isChecked = authEnabled
            authSwitch.setOnClickListener {
                val sharedPreferences =
                    requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("AuthEnabled", authSwitch.isChecked)
                editor.apply()
            }
        }
    }
}
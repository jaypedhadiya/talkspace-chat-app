package com.example.talkspace.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.talkspace.R
import com.example.talkspace.databinding.CallHistoryAppBarBinding
import com.example.talkspace.databinding.FragmentCallHistoryBinding

class CallHistoryFragment : Fragment() {

    private lateinit var binding : FragmentCallHistoryBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCallHistoryBinding.inflate(inflater,container,false)
        return binding.root
    }

}
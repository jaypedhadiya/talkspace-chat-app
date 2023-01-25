package com.example.talkspace.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.talkspace.R
import com.example.talkspace.databinding.ContactAppBarBinding
import com.example.talkspace.databinding.FragmentContactBinding

class ContactFragment : Fragment() {

    private lateinit var binding : FragmentContactBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactBinding.inflate(inflater,container,false)
        return binding.root
    }
}
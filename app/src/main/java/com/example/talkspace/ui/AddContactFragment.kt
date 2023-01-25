package com.example.talkspace.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.talkspace.R
import com.example.talkspace.databinding.FragmentAddContactBinding

class AddContactFragment : Fragment() {

    private lateinit var binding : FragmentAddContactBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddContactBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addChats.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_contactsOnAppFragment)
        }
    }

}
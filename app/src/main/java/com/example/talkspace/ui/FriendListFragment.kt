package com.example.talkspace.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.adapter.ChatListAdapter
import com.example.talkspace.adapter.StatusAdapter
import com.example.talkspace.databinding.FragmentFriendListBinding
import com.example.talkspace.model.SQLChat
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class FriendListFragment : Fragment() {

    private val chatViewModel : ChatViewModel by activityViewModels {
        ChatViewModelFactory((activity?.application as ApplicationClass).repository)
    }
    private lateinit var binding : FragmentFriendListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFriendListBinding.inflate(inflater,container,false)

//        For Status
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false)
        binding.recyclerView.adapter = StatusAdapter(requireContext())
//
//        For chat
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false)
        val adapter = ChatListAdapter(chatViewModel)
        binding.friendRecyclerView.adapter = adapter

//        showing chats in recycler view
        chatViewModel.chats.observe(viewLifecycleOwner){ chats ->
            chats?.let {
                adapter.submitList(chats)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuBtn = getView()?.findViewById<ImageView>(R.id.menu_btn)
        val searchBtn = getView()?.findViewById<ImageView>(R.id.search_btn)
        val profileIcon = getView()?.findViewById<ImageView>(R.id.profile_icon)
        //        For contacts permissions
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ){ isGranted ->
                if (isGranted){
                    Log.i("Permission" ,"Granted")
                }else{
                    Log.i("Permission","Denied")
                }
            }
        requestPermissionLauncher.launch(
            Manifest.permission.READ_CONTACTS
        )
        menuBtn?.setOnClickListener {
            Toast.makeText(context,"menu button is implement will be later", Toast.LENGTH_SHORT).show()
        }
        searchBtn?.setOnClickListener {
            Toast.makeText(context,"Search Button implement will be later", Toast.LENGTH_SHORT).show()
        }

        profileIcon?.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_userDetailFragment)
        }
    }

    override fun onStart() {
        super.onStart()
//        chatViewModel.startListeningForChats(requireContext())
    }

    override fun onStop() {
        super.onStop()
//        chatViewModel.stopListeningForChats()
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("FriendListFragment","Fragment Destroy")
    }
}
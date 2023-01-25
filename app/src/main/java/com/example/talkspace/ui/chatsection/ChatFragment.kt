package com.example.talkspace.ui.chatsection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.adapter.MessageAdapter
import com.example.talkspace.databinding.FragmentChatBinding
import com.example.talkspace.model.*
import com.example.talkspace.observers.MyScrollToBottomObserver
import com.example.talkspace.ui.currentUser
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var messages: LiveData<List<SQLiteMessage>>
    private lateinit var currentFriendName: String
    private lateinit var currentFriendId: String
    private lateinit var adapter: MessageAdapter
    private val chatViewModel: ChatViewModel by activityViewModels {
        ChatViewModelFactory((activity?.application as ApplicationClass).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        currentFriendName = chatViewModel.currentFriendName.value.toString()
        currentFriendId = chatViewModel.currentFriendId.value.toString()
        messages = chatViewModel.getMessages(currentFriendId)
//        for display massage in recycler view
        val layout = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            true
        )
        binding.messagesRecyclerview.layoutManager = layout
        adapter = MessageAdapter()
        binding.messagesRecyclerview.adapter = adapter

        messages.observe(viewLifecycleOwner) { messages ->
            messages.let {
                adapter.submitList(messages)
            }
        }

        chatViewModel.startListeningForMessages()

        adapter.registerAdapterDataObserver(
           MyScrollToBottomObserver(
               binding.messagesRecyclerview,
               adapter,
               layout
           )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        set friend profile
        val backChatButton = getView()?.findViewById<ImageView>(R.id.back_chat_button)
        val friendName = getView()?.findViewById<TextView>(R.id.friend_name)
        friendName?.text = chatViewModel.currentFriendName.value

//        back navigation
        backChatButton?.setOnClickListener {
            findNavController().navigateUp()
        }
//        send Massage
        binding.seadMassageButton.setOnClickListener {
            sendMessage()
        }
    }

    override fun onStart() {
        super.onStart()
        chatViewModel.startListeningForMessages()
    }

    override fun onStop() {
        super.onStop()
        chatViewModel.stopListeningForMessages()
    }

    private fun sendMessage() {
        val text = binding.massageInputText.text.toString().trim()
        val timeStamp = System.currentTimeMillis()
        val message = FirebaseMessage(
            timeStamp = timeStamp,
            text = text,
            senderId = currentUser?.phoneNumber.toString(),
            receiverId = chatViewModel.currentFriendId.value.toString(),
            imageUrl = "",
            state = MessageState.SENDING
        )
        binding.massageInputText.setText("")

        if (messages.value?.size == 0) {
            chatViewModel.addToChats(text, timeStamp.toString())
        }
        chatViewModel.sendMessage(message)

    }

}

package com.example.talkspace.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.talkspace.model.FirebaseMessage
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteMessage
import com.example.talkspace.model.User
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.ui.currentUser

class ChatViewModel(private val repository: ChatRepository): ViewModel() {
    val chats = repository.getChat()

    private val _currentFriendName = MutableLiveData<String>()
    val currentFriendName : LiveData<String> = _currentFriendName

    private val _currentFriendId = MutableLiveData<String>()
    val currentFriendId: LiveData<String> = _currentFriendId

    fun addChat(chat: SQLChat){
        repository.addChat(chat,viewModelScope)
    }

    fun addToChats(text: String,timeStamp:String){
        repository.addToChats(text,
        timeStamp,
        currentFriendId.value.toString(),
        currentFriendName.value.toString(),
        viewModelScope)
    }
    fun setFriendName(friendName: String){
        _currentFriendName.value = friendName
    }

    fun setFriendId(friendId: String){
        _currentFriendId.value = friendId
    }

    fun sendMessage(message: FirebaseMessage){
        repository.sendMessage(currentFriendId.value.toString(),message,viewModelScope)
    }

    fun stopListeningForMessages(){
        repository.stopListeningForMessages()
    }

    fun stopListeningForChats(){
        repository.stopListeningForChats()
    }

    fun getMessages(friendId: String ): LiveData<List<SQLiteMessage>>{
        return repository.getMessages(friendId)
    }

    fun startListeningForChats(context: Context){
        repository.startListeningForChats(context,viewModelScope)
    }
    fun startListeningForMessages(){
        repository.startListeningForMessages(
            currentUser?.phoneNumber.toString()
            ,currentFriendId.value.toString(),
            viewModelScope)
    }
}

class ChatViewModelFactory(private val repository: ChatRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChatViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}

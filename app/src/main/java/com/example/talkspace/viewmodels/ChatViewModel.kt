package com.example.talkspace.viewmodels

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.*
import com.example.talkspace.model.FirebaseMessage
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.model.SQLiteMessage
import com.example.talkspace.repositories.ChatRepository
import com.example.talkspace.repositories.ContactsRepository
import com.example.talkspace.ui.currentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository,
private val contactsRepository: ContactsRepository): ViewModel() {

    val chats : LiveData<List<SQLChat>> = chatRepository.getChat()
    val contactOnAppUser: LiveData<List<SQLiteContact>> = contactsRepository.getAppContacts()
    val contactNotOnAppUser: LiveData<List<SQLiteContact>> = contactsRepository.getInContact()

    private val _currentFriendName = MutableLiveData<String>()
    val currentFriendName : LiveData<String> = _currentFriendName

    private val _currentFriendId = MutableLiveData<String>()
    val currentFriendId: LiveData<String> = _currentFriendId

    fun setFriendName(friendName: String){
        _currentFriendName.value = friendName
    }

    fun setFriendId(friendId: String){
        _currentFriendId.value = friendId
    }

    fun addChat(chat: SQLChat){
        chatRepository.addChat(chat,viewModelScope)
    }

    fun addContacts(contact: SQLiteContact){
        contactsRepository.addContacts(contact,viewModelScope)
    }

    fun addToChats(text: String,timeStamp:String){
        chatRepository.addToChats(text,
        timeStamp,
        currentFriendId.value.toString(),
        currentFriendName.value.toString(),
        viewModelScope)
    }

    fun getMessages(friendId: String ): LiveData<List<SQLiteMessage>>{
        return chatRepository.getMessages(friendId)
    }

    fun sendMessage(message: FirebaseMessage){
        chatRepository.sendMessage(currentFriendId.value.toString(),message,viewModelScope)
    }

    fun startListeningForChats(){
        chatRepository.startListeningForChats(viewModelScope)
    }
    fun startListeningForMessages(){
        chatRepository.startListeningForMessages(
            currentUser?.phoneNumber.toString()
            ,currentFriendId.value.toString(),
            viewModelScope)
    }
    fun startListeningForContacts(){
        contactsRepository.startListeningForContacts(viewModelScope)
    }

    fun stopListeningForMessages(){
        chatRepository.stopListeningForMessages()
    }

    fun stopListeningForChats(){
        chatRepository.stopListeningForChats()
    }

    fun stopListeningForContacts(){
        contactsRepository.stopListeningForContacts()
    }

    fun syncContacts(contentResolver: ContentResolver) {
        contactsRepository.syncContacts(contentResolver)
    }
}

class ChatViewModelFactory(private val chatRepository: ChatRepository,
private val contactsRepository: ContactsRepository
):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChatViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository,contactsRepository) as T
        }
        throw IllegalArgumentException("Unknown viewModel class")
    }
}

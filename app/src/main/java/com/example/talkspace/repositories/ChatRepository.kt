package com.example.talkspace.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    private var messageRegistration: ListenerRegistration? = null
    private var chatRegistration: ListenerRegistration? = null
    fun getChat(): LiveData<List<SQLChat>> {
        return chatDao.getChats().asLiveData()
    }

    fun getMessages(friendId: String): LiveData<List<SQLiteMessage>> {
        return messageDao.getMessages(friendId).asLiveData()
    }

    fun addChat(chat: SQLChat, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            chatDao.insert(chat)
        }
    }

    fun generateChatKey(number1: String, number2: String): String {
        val number1_ = number1.replace("+91", "").toLong()
        val number2_ = number2.replace("+91", "").toLong()
        return if (number1_ > number2_) {
            "$number1_-$number2_"
        } else if (number1_ < number2_) {
            "$number2_-$number1_"
        } else {
            number1
        }
    }

    fun sendMessage(friendId: String, message: FirebaseMessage, coroutineScope: CoroutineScope) {
        if (currentUser == null) {
            return
        }
        coroutineScope.launch(Dispatchers.IO) {
            messageDao.insert(message.toSQLiteObject())
        }
        val currentUserId = currentUser.phoneNumber.toString()
        val key = generateChatKey(friendId, currentUserId)
        firestore.collection("chats").document(key)
            .collection("messages").document(message.timeStamp.toString())
            .set(message)
            .addOnSuccessListener {
                Log.d("ChatRepository", "Message sent successfully")

//                addInToFriendList(currentUserId,friendId)
            }.addOnFailureListener {
                Log.d("ChatRepository", "Message sent unsuccessful")
            }
    }

    fun addToChats(
        text: String,
        timeStamp: String,
        friendId: String,
        friendName: String,
        coroutineScope: CoroutineScope
    ) {
        Log.d("ChatFragment", friendId)
        val friend = FirebaseChat(
            friendId,
            friendName,
            "",
            "",
            text,
            timeStamp,
            0
        )
//                Add to chats table in sqlite database
        addChat(friend.toSQLObject(), coroutineScope)

        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("friends")
            .document(friendId)
            .set(friend)
            .addOnSuccessListener {
                Log.d("ChatFragment", "Add to the sender side")
                val user = FirebaseChat(
                    com.example.talkspace.ui.currentUser?.phoneNumber.toString(),
                    "",
                    "",
                    "",
                    text,
                    timeStamp,
                    0
                )
                firestore.collection("users")
                    .document(friendId)
                    .collection("friends")
                    .document(currentUser?.phoneNumber.toString())
                    .set(user)
                    .addOnSuccessListener {
                        Log.d("ChatsFragment", "Add to the receiver side")
                    }
            }
    }

    fun startListeningForChats(coroutineScope: CoroutineScope) {
        Log.d("ChatRepository", "Starting listener for chats...")
        chatRegistration = firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("friends")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    if (snapshot.metadata.isFromCache) {
                        Log.d("ChatRepository", "Chat listener data coming from cache")
                    } else {
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d("ChatRepository", "New chat added ")
                                    val userData = dc.document.data
                                    val chat = FirebaseChat(
                                        userData["phoneNumber"].toString(),
                                        userData["friendName"].toString(),
                                        userData["friendAbout"].toString(),
                                        userData["friendPhotoUrl"].toString(),
                                        userData["lastChat"].toString(),
                                        userData["lastTimeStamp"].toString(),
                                        0
                                    )

                                    coroutineScope.launch (Dispatchers.IO){
//                                        val contact = contactsOnAppUser.value?.find { it.contactPhoneNumber == chat.phoneNumber }

                                    val contact: SQLiteContact= chatDao.checkContact(userData["phoneNumber"].toString())
                                        if (contact != null){
                                            firestore.collection("users")
                                                .document(currentUser?.phoneNumber.toString())
                                                .collection("friends")
                                                .document(chat.phoneNumber)
                                                .update("friendName" ,contact.contactName)
                                                .addOnSuccessListener {
                                                    Log.d("ChatRepository","Chat is updated : ${contact.contactName}")
                                                }
                                                .addOnFailureListener {
                                                    Log.d("ChatRepository","Failed to update chat")
                                                }
                                        }else{
                                            chatDao.insert(chat.toSQLObject())
                                        }
                                    }

                                }
                                DocumentChange.Type.MODIFIED -> {
                                    Log.d("ChatRepository","Update Chat")
                                    val data = dc.document.data
                                    val chat = FirebaseChat(
                                        data["phoneNumber"].toString(),
                                        data["friendName"].toString(),
                                        data["friendAbout"].toString(),
                                        data["friendPhotoUrl"].toString(),
                                        data["lastChat"].toString(),
                                        data["lastTimeStamp"].toString(),
                                        data["remainingMessage"].toString().toInt()
                                    )
                                    coroutineScope.launch (Dispatchers.IO){
                                        Log.d("ChatRepository","Update Chat in SQLite database")
                                        chatDao.insert(chat.toSQLObject())
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    Log.d("ChatRepository","Delete Chat")
                                    val data = dc.document.data
                                    coroutineScope.launch (Dispatchers.IO){
                                        chatDao.delete(data["phoneNumber"].toString())
                                    }
                                }
                                else -> {
                                    Log.d("ChatRepository", "Other operation done")
                                }
                            }
                        }
                    }
                }
            }
    }

    fun startListeningForMessages(
        userId: String,
        currentUserId: String,
        coroutineScope: CoroutineScope
    ) {
        Log.d("ChatRepository", "Starting listening for messages...")
        val setting = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = setting
        val key = generateChatKey(userId, currentUserId)
        messageRegistration = firestore.collection("chats")
            .document(key)
            .collection("messages")
            .addSnapshotListener() { snapshot, e ->
                if (e != null) {
                    Log.d("ChatRepository", "Error listening for messages")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (snapshot.metadata.isFromCache) {
                        Log.d("ChatRepository", "message listener data coming from cache")
                    } else {
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d("Chas", "New Message added")
                                    val userData = dc.document.data
                                    val message = SQLiteMessage(
                                        userData["timeStamp"].toString().toLong(),
                                        userData["text"].toString(),
                                        userData["senderId"].toString(),
                                        userData["receiverId"].toString(),
                                        userData["imageUrl"].toString(),
                                        MessageState.valueOf("RECEIVED")
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        messageDao.insert(message)
                                    }
                                }
                                else -> {
                                    Log.d("ChatRepository", "Other Option")
                                }
                            }
                        }
                    }
                }
            }
    }

    fun stopListeningForChats() {
        Log.d("ChatRepository", "Stopping listening for chats...")
        chatRegistration?.remove()
        chatRegistration = null
    }

    fun stopListeningForMessages() {
        Log.d("ChatRepository", "Stopping listening for messages...")
        messageRegistration?.remove()
        messageRegistration = null
    }
}
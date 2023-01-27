package com.example.talkspace.repositories

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.*
import com.example.talkspace.ui.chatsection.ChatFragment
import com.example.talkspace.ui.currentUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
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

    fun startListeningForChats(context: Context, coroutineScope: CoroutineScope) {
        Log.d("ChatRepository", "Starting listener for chats...")
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        firestore.firestoreSettings = settings
        chatRegistration = firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("friends")
            .addSnapshotListener() { snapshot, e ->
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
                                    Log.d("FriendListFragment", "New chat added ")
                                    val userData = dc.document.data

                                    // Check if user is present in contacts or not
                                    val newFriendId = userData["phoneNumber"].toString()
                                    val newFriendName = returnNameIfExists(
                                        userData["phoneNumber"].toString(),
                                        context
                                    )
                                    val chat = SQLChat(
                                        newFriendId,
                                        newFriendName,
                                        userData["friendAbout"].toString(),
                                        userData["friendPhotoUri"].toString(),
                                        userData["lastChat"].toString(),
                                        userData["lastTimeStamp"].toString(),
                                        0
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        chatDao.insert(chat)
                                    }

                                    val contact = FirebaseContact(
                                        newFriendId,
                                        newFriendName,
                                        "",
                                        ""
                                    )
                                    firestore.collection("users")
                                        .document(currentUser?.phoneNumber.toString())
                                        .collection("contacts")
                                        .document(newFriendId)
                                        .set(contact)
                                        .addOnSuccessListener {
                                            Log.d("ChatRepository","Contact add successfully")
                                        }.addOnFailureListener {
                                            Log.d("ChatRepository","Failed to add contact",it)
                                        }
                                    //Friestore in update name

                                    if (newFriendName != newFriendId) {
                                        firestore.collection("users")
                                            .document(currentUser?.phoneNumber.toString())
                                            .collection("friends")
                                            .document(newFriendId)
                                            .update("friendName", newFriendName)
                                            .addOnSuccessListener {
                                                Log.d("ChatRepository", "New chat's name updated")
                                            }.addOnFailureListener {
                                                Log.d(
                                                    "ChatRepository",
                                                    "Error updating new chat name",
                                                    it
                                                )
                                            }
                                    }
                                }
                                else -> {
                                    Log.d("FriendListFragment", "Other operation done")
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
        Log.d("ChatRepository", "Stopping listening for messages...")
        chatRegistration?.remove()
        chatRegistration = null
    }

    fun stopListeningForMessages() {
        Log.d("ChatRepository", "Stopping listening for messages...")
        messageRegistration?.remove()
        messageRegistration = null
    }

    fun returnNameIfExists(friendId: String, context: Context): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(friendId)
        )

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                val displayIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                var contactId = ""
                var contactName = ""
                if (idIndex >= 0) contactId = cursor.getString(idIndex)
                if (displayIndex >= 0) contactName = cursor.getString(displayIndex)

                Log.d("ChatRepository", "id : $contactId , Name : $contactName")
                cursor.close()
                return contactName
            } else {
                Log.d("ChatRepository", "Not in Contact")
                cursor.close()
                return friendId
            }
        } else {
            Log.d("Chats", "Cursor is null")
            return friendId
        }

    }
}
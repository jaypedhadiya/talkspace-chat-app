package com.example.talkspace.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.FirebaseContact
import com.example.talkspace.model.SQLiteContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsRepository(
    private val contactDao: ContactDao
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var registration : ListenerRegistration? = null
    fun addContacts(contact: SQLiteContact, coroutineScope: CoroutineScope) {
        if (currentUser == null){
            return
        }
       firestore.collection("users")
            .document(currentUser.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactPhoneNumber)
            .set(contact.toFirebaseContact())
            .addOnSuccessListener {
                Log.d("ContactsRepository","Contacts upload successfully")
//                Now add to the contact in SQLite database
                coroutineScope.launch(Dispatchers.IO) {
                    contactDao.insert(contact)
                }
            }
            .addOnFailureListener {
                Log.d("ContactsRepository","Failed upload contacts")
            }
    }

    fun getContact(): LiveData<List<SQLiteContact>> {
        return contactDao.getContacts().asLiveData()
    }

    fun startListeningForContacts(coroutineScope: CoroutineScope){
        Log.d("ContactsRepository","Start Listening for Chats...")
        registration = firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .addSnapshotListener { snapshot, e ->
                if (e == null){
                    Log.d("ContactsRepository","Failed to listening contacts")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    if (snapshot.metadata.isFromCache) {
                        Log.d("ContactsRepository", "Contacts data is coming from cache")
                    } else {
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val data = dc.document.data
                                    val contact = FirebaseContact(
                                        data["contactPhoneNumber"].toString(),
                                        data["contactName"].toString(),
                                        "",
                                        ""
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactDao.insert(contact.toSQLiteObject())
                                    }
                                }
                                else -> {
                                    Log.d("ContactsRepository", "Other operations done")
                                }
                            }
                        }
                    }
                }
            }

    }

    fun sotpListeningForContacts(){
        Log.d("ContactsRepository","Stoping listening for Contacts...")
        registration?.remove()
        registration = null
    }
}

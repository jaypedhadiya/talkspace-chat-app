package com.example.talkspace.repositories

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.ContactsContract
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

    fun getInContact(): LiveData<List<SQLiteContact>> {
        return contactDao.getInContacts().asLiveData()
    }

    fun getAppContacts(): LiveData<List<SQLiteContact>> {
        return contactDao.getAppContacts().asLiveData()
    }

    fun startListeningForContacts(coroutineScope: CoroutineScope){
        Log.d("ContactsRepository","Start Listening for Contacts...")
        registration = firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .addSnapshotListener { snapshot, e ->
                if (e != null){
                    Log.d("ContactsRepository","Failed to listening contacts : $e")
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
                                        data["contactAbout"].toString(),
                                        "",
                                        data["appUser"].toString().toBoolean()
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactDao.insert(contact.toSQLiteObject())
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val data = dc.document.data
                                    Log.d("ContactsRepository","Contacts Modified ${data["contactPhoneNumber"].toString()}")
                                    val contact = FirebaseContact(
                                        data["contactPhoneNumber"].toString(),
                                        data["contactName"].toString(),
                                        data["contactAbout"].toString(),
                                        data["contactPhotoUrl"].toString(),
                                        data["appUser"].toString().toBoolean()
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactDao.insert(contact.toSQLiteObject())
                                    }
                                    updateChat(contact.contactName,contact.contactAbout,
                                        contact.contactPhoneNumber,contact.contactPhotoUrl)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val data = dc.document.data
                                    val contact = FirebaseContact(
                                        data["contactPhoneNumber"].toString(),
                                        data["contactName"].toString(),
                                        data["contactAbout"].toString(),
                                        "",
                                        data["appUser"].toString().toBoolean()
                                    )
                                    coroutineScope.launch(Dispatchers.IO) {
                                        contactDao.delete(contact.toSQLiteObject())
                                    }
                                }
                                else ->{
                                    Log.d("ContactsRepository", "Other operations done")
                                }
                            }
                        }
                    }
                }
            }

    }
    fun updateChat(friendName: String,friendAbout: String,
                   friendPhoneNumber: String,friendPhotoUrl: String){
        val update = mapOf(
            "friendAbout" to friendAbout,
            "friendName" to friendName,
            "friendPhotoUrl" to friendPhotoUrl
        )
        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("friends")
            .document(friendPhoneNumber)
            .update(update)
            .addOnSuccessListener {
                Log.d("ContactsRepository","Chat is update through contact update")
            }
            .addOnFailureListener {
                Log.d("ContactsRepository","Chat is not update through contact update")
            }
    }

    fun stopListeningForContacts(){
        Log.d("ContactsRepository","Stop listening for Contacts...")
        registration?.remove()
        registration = null
    }

    fun syncContacts(contentResolver: ContentResolver){
        Log.d("ContactsRepository","Contacts syncing...")
        val serverContacts = mutableListOf<SQLiteContact>()

        val deviceContacts = getAllContactsList(contentResolver)
//        get contact on server
        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .get().addOnSuccessListener {snapshot ->
                for (dc in snapshot){
                    val data = dc.data
                    val contact = SQLiteContact(
                        data["contactPhoneNumber"].toString(),
                        data["contactName"].toString(),
                        data["contactAbout"].toString(),
                        data["contactPhotoUrl"].toString(),
                        data["appUser"].toString().toBoolean()
                    )
                    serverContacts.add(contact)
                }
                Log.d("ContactsRepository","Successful get contacts from server")

                for (contact in deviceContacts){
                    val serverContact = serverContacts.find {
                        contact.contactPhoneNumber == it.contactPhoneNumber
                    }
                    firestore.collection("users")
                        .document(contact.contactPhoneNumber)
                        .get()
                        .addOnSuccessListener { contactSnapshot ->
                            if (contactSnapshot.exists()){
                                contact.isAppUser = true
                                contact.contactAbout = contactSnapshot["userAbout"].toString()
                            }

                            if (serverContact != null) {
                                if (areDifferent(contact, serverContact)) {
//                                  update name in server
                                    updateContactOnServer(contact)
                                }
                            }else {
//                                  Add contact in server
                                if (contact.contactPhoneNumber != currentUser?.phoneNumber.toString()){
                                    addContactToServer(contact)
                                }
                            }
                        }.addOnFailureListener {
                        }
                }

                for (contact in serverContacts){
                    if (!deviceContacts.any{ it.contactPhoneNumber == contact.contactPhoneNumber}){
//                        delete contact in server
                        deleteContactOnServer(contact)
                    }
                }
            }.addOnFailureListener {
                Log.d("ContactsRepository","Filed to get contacts from server",it)
            }
    }

    private fun deleteContactOnServer(contact: SQLiteContact){
        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactPhoneNumber)
            .delete()
            .addOnSuccessListener {
                Log.d("ContactsRepository","successfully contact delete on server :" +
                        " ${contact.contactPhoneNumber}")
            }.addOnFailureListener {
                Log.d("ContactsRepository","Filed to delete contact on server",it)
            }
    }

    private fun updateContactOnServer(contact: SQLiteContact){
        val updates = mapOf(
            "contactName" to contact.contactName,
            "contactPhoneNumber" to contact.contactPhoneNumber,
            "contactAbout" to contact.contactAbout
        )
        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(contact.contactPhoneNumber)
            .update(updates)
            .addOnSuccessListener {
                Log.d("ContactsRepository","Contact name update : ${contact.contactName}")

            }.addOnFailureListener {
                Log.d("ContactsRepository","Filed to update Contact name :" +
                        " ${contact.contactName}")
            }
    }


    private fun addContactToServer(sqLiteContact : SQLiteContact){

        firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .collection("contacts")
            .document(sqLiteContact.contactPhoneNumber)
            .set(sqLiteContact)
            .addOnSuccessListener {
                Log.d("ContactsRepository","Contact add on server :" +
                        " ${sqLiteContact.contactPhoneNumber} isAppUser : ${sqLiteContact.isAppUser}")
            }.addOnFailureListener {
                Log.d("ContactsRepository","Filed to add contact in server :" +
                        " ${sqLiteContact.contactPhoneNumber}" ,it)
            }
    }
    private fun areDifferent(contact1: SQLiteContact, contact2 : SQLiteContact): Boolean{
        return contact1.contactName != contact2.contactName ||
                contact1.isAppUser != contact2.isAppUser||
                contact1.contactAbout != contact2.contactAbout
    }

//    private fun isAppUser(contact:SQLiteContact): String{
//        var isUser= "false"
//
//        Log.d("ContactsRepository","is User : $isUser")
//        return isUser
//    }
    @SuppressLint("Range")
    fun getAllContactsList(contentResolver: ContentResolver): List<SQLiteContact>{
        Log.d("ContactsRepository","getAllContactsList function call...")
//        get contact form address book in phone
        val contacts = mutableListOf<SQLiteContact>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null)

        if (cursor == null){
            Log.d("ContactsRepository","Contacts cursor is null in getAllContactsList")
        }else {
            if (cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    val contactId = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val contactName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    )
                    val contactPhoneNumber = getContactPhoneNumber(contactId , contentResolver)
                    Log.d("ContactsRepository","Contacts Id : $contactId | contactName : " +
                            "$contactName             | contactPhoneNumber : $contactPhoneNumber")
                    val contact = SQLiteContact(
                        contactPhoneNumber,
                        contactName,
                        "",
                        "",
                        false
                    )
                    contacts.add(contact)
                }
                cursor.close()
            }
        }
        return contacts
    }

    @SuppressLint("Range")
    fun getContactPhoneNumber(contactId : String,contentResolver: ContentResolver): String{

        var phoneNumber = ""
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        if ((phoneCursor?.count ?: 0 ) > 0){
            while (phoneCursor != null && phoneCursor.moveToNext()){
                phoneNumber = phoneCursor.getString(
                    phoneCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
            }
            phoneCursor?.close()
        }
        phoneNumber = phoneNumber.replace(" ", "")
        if (!phoneNumber.contains("+91")){
            phoneNumber = "+91$phoneNumber"
        }
        return phoneNumber
    }

}

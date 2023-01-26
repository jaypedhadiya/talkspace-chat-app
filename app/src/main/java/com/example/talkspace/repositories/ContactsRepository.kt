package com.example.talkspace.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsRepository(
    private val contactDao: ContactDao
) {
    fun addContacts(contact: SQLiteContact, coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            contactDao.insert(contact)
        }
    }

    fun getContact(): LiveData<List<SQLiteContact>> {
        return contactDao.getContacts().asLiveData()
    }
}

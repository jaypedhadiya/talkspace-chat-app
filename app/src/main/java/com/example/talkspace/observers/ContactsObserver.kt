package com.example.talkspace.observers

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import com.example.talkspace.viewmodels.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ContactsObserver(handler: Handler,
private val lifecycleScope: CoroutineScope,
private val contentResolver: ContentResolver,
private val chatViewModel: ChatViewModel
): ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        lifecycleScope.launch(Dispatchers.IO) {
            chatViewModel.syncContacts(contentResolver)
        }
    }
}
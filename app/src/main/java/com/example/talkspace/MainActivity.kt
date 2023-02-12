package com.example.talkspace

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.talkspace.databinding.ActivityMainBinding
import com.example.talkspace.observers.ContactsObserver
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController : NavController

    private val chatViewModel : ChatViewModel by viewModels {
        ChatViewModelFactory((this.application as ApplicationClass).chatRepository,(this.application as ApplicationClass).contactsRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Firebase.auth.currentUser == null){
            startActivity(Intent(this,SignInActivity::class.java))
            finish()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.outer_container) as NavHostFragment

        navController = navHostFragment.navController
        val setting = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()
        FirebaseFirestore.getInstance().firestoreSettings = setting




        //    First check permissions
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ){ isGranted ->
                if (isGranted){
                    Log.i("Permission" ,"Granted")
//                    sync contacts  is permission is granted in first time
//                    here, the app open first time check contact is add update or delete
                    lifecycleScope.launch(Dispatchers.IO){
                        chatViewModel.syncContacts(contentResolver)
                    }
//                    start listening for chats if permission is granted in first time
                    chatViewModel.startListeningForChats()
//                    start listening for contacts if permission is granted in first time
                    chatViewModel.startListeningForContacts()
//                    register contacts change observer if permission is granted in first time
//                    registerContactsChangeObserver() is observe the change contact in the
//                    phone Address book (contact)
                    registerContactsChangeObserver()
                }else{
                    Log.i("Permission","Denied")
                    Toast.makeText(this,
                        "You need to add permission in order to access contacts",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            Log.d("Permission","Permission Granted")
//            sync contacts is call if permission is granted
            lifecycleScope.launch(Dispatchers.IO) {
                chatViewModel.syncContacts(contentResolver)
            }
//            start listening for chats if permission is granted
            chatViewModel.startListeningForChats()
//            start listening for contacts if permission is granted
            chatViewModel.startListeningForContacts()
//            registerContactsChangeObserver() is call if permission is granted
            registerContactsChangeObserver()
        }else {
            requestPermissionLauncher.launch(
                Manifest.permission.READ_CONTACTS
            )
        }
    }

    private fun registerContactsChangeObserver(){
        val contactsObserver = ContactsObserver(
            Handler(Looper.getMainLooper()),
            lifecycleScope,
            contentResolver,
            chatViewModel
        )
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            contactsObserver
        )
    }
    override fun onStart() {
        super.onStart()
    }


    override fun onStop() {
        super.onStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.stopListeningForChats()
        chatViewModel.stopListeningForContacts()
    }
}
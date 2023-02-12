package com.example.talkspace.ui.chatsection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.adapter.SectionedContactAdapter
import com.example.talkspace.databinding.FragmentContactsOnAppBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore


class ContactsOnAppFragment : Fragment() {

    private lateinit var binding: FragmentContactsOnAppBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val chatViewModel: ChatViewModel by activityViewModels {
        ChatViewModelFactory(
            (activity?.application as ApplicationClass).chatRepository,
            (activity?.application as ApplicationClass).contactsRepository
        )
    }
    private val pickContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            if (uri == null) {
                return@registerForActivityResult
            }
            val contentResolver = requireContext().contentResolver
//        For getting contact name
            val contactCursor = contentResolver.query(
                uri, null, null,
                null, null
            )
            val nameIndex = contactCursor?.getColumnIndex(
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            )
            contactCursor?.moveToFirst()
            val contactName = nameIndex?.let { contactCursor.getString(it) }
            Log.d("PickContact", "contact Name : $contactName")

//        For getting contact Phone Number
            val phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(uri.lastPathSegment),
                null
            )
            var contactPhoneNumber = ""
            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                val index =
                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (index >= 0) contactPhoneNumber = phoneCursor.getString(index)
            }
//        process phone Number
            contactPhoneNumber = contactPhoneNumber.replace(" ", "")
            if (contactPhoneNumber.length != 10) {
                contactPhoneNumber = contactPhoneNumber.substring(3)
            }
            contactPhoneNumber = "+91${contactPhoneNumber}"
            ifContentUser(contactPhoneNumber, contactName.toString())
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactsOnAppBinding.inflate(inflater, container, false)
        val layoutManager =
            LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.contactsRecyclerview.layoutManager = layoutManager

        chatViewModel.contactOnAppUser.observe(viewLifecycleOwner){contacts ->
            contacts.let {
                chatViewModel.contactNotOnAppUser.observe(viewLifecycleOwner){inviteContacts ->
                    inviteContacts.let {
                        val adapter = SectionedContactAdapter(contacts,inviteContacts,chatViewModel,requireContext())
                        binding.contactsRecyclerview.adapter = adapter
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.selectContacts.setOnClickListener {
//            selectContacts()
//        }
        val backContactsButton = getView()?.findViewById<ImageView>(R.id.back_contacts_button)
        backContactsButton?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    fun selectContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            pickContact.launch(null)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission", "Granted")
                pickContact.launch(null)
            } else {
                view?.let {
                    Snackbar.make(
                        it,
                        "You need to grant permission in order to add contacts",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Dismissed") {
                            Log.d("Permission", "Dismissed")
                        }.show()

                }
            }

        }

    private fun ifContentUser(friendId: String, friendName: String) {
        firestore.collection("users")
            .document(friendId)
            .get()
            .addOnSuccessListener {
                val value = it.get("userId")
                if (value == null) {
                    Toast.makeText(
                        requireContext(),
                        "$friendName does not use the App",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val contact = SQLiteContact(
                        friendId,
                        friendName,
                        "",
                        "",
                        false

                    )
//                    chatViewModel.addContacts(contact)
                    chatViewModel.setFriendId(friendId)
                    chatViewModel.setFriendName(friendName)
                    navigateToChatFragment()
                }
            }
    }

    private fun navigateToChatFragment() {
//        navigate to chat Fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.outer_container, ChatFragment())
            .commit()
    }
}
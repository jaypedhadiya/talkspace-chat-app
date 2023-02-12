package com.example.talkspace.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.talkspace.R
import com.example.talkspace.SignInActivity
import com.example.talkspace.databinding.FragmentUserDetailBinding
import com.example.talkspace.repositories.LocalProfilePhotoStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class UserDetailFragment : Fragment() {

    private var binding: FragmentUserDetailBinding? = null

    private val currentUser = Firebase.auth.currentUser
    private var userName: String = ""
    private var userAbout: String = ""
    private var newPhotoUri = Uri.EMPTY

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        if (it != null){
            Log.d("UserDetailFragment","Image picker : ${it.toString()}")
//        Glide.with(binding!!.userProfileImage.context).load(it).into(binding!!.userProfileImage)
            showCustomPhotoDialog(it)

            newPhotoUri = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val fragmentBinding = FragmentUserDetailBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backChangeButton = getView()?.findViewById<ImageView>(R.id.back_contacts_button)
        loadUserDetails()
        loadUserProfilePhoto()
        binding?.editNameIcon?.setOnClickListener {
            showCustomDialog("Edit Name :", userName, "userName")
        }
        binding?.imageStorageButton?.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

//            updateProfilePhoto()
        }

        binding?.editAboutIcon?.setOnClickListener {
            showCustomDialog("Edit About :", userAbout, "userAbout")
        }
        backChangeButton?.setOnClickListener {
//            findNavController().navigate(R.id.action_userDetailFragment_to_mainFragment)
            findNavController().navigateUp()
        }
        binding?.deleteAccountImage?.setOnClickListener { logout() }
//        binding?.deleteAccountText?.setOnClickListener { logout() }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

//    upload to cloud storage

    private fun loadUserProfilePhoto(){
        Glide.with(binding!!.userProfileImage.context).load(R.drawable.ic_baseline_person_24).into(
            binding!!.userProfileImage)
        if (currentUser == null){
            logout()
            return
        }
        Log.d("UserDetailFragment"," Getting user Profile photo ")
        val image = LocalProfilePhotoStorage.getProfilePhotoFromLocalStorage(requireContext())
        if (image == null){
            val imageFile = File("${requireContext().cacheDir}/profile.png")
            Firebase.storage.reference.child("User profiles/${currentUser.uid}.png")
                .getFile(imageFile)
                .addOnSuccessListener {
                    Glide.with(binding?.userProfileImage!!.context).load(LocalProfilePhotoStorage.getProfilePhotoFromLocalStorage(requireContext())).into(binding!!.userProfileImage)
                }
                .addOnFailureListener{}
        }else{
            Log.d("UserDetailFragment","Got profile photo form cache ${image}")
            Glide.with(binding?.userProfileImage!!.context).load(image).into(binding!!.userProfileImage)
        }
    }

    private fun loadUserDetails() {
        Log.d("UserDetailFragment", "user Id : ${currentUser?.phoneNumber.toString()}")
        Firebase.firestore.collection("users")
            .document(currentUser?.phoneNumber.toString())
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.d("UserDetailFragment", "Error occurred in fetching User Details : ", e)
                }
                if (snapshot != null) {
                    userName = snapshot["userName"].toString()
                    binding?.displayName?.text = userName
                    userAbout = snapshot["userAbout"].toString()
                    binding?.displayAbout?.text = userAbout
                    binding?.displayMobileNo?.text = snapshot["userPhoneNumber"].toString()
                } else {
                    Log.d("UserDetailFragment", "User details not available in firebase")
                }
                val source = if (snapshot?.metadata?.isFromCache == true) "cache" else "server"
                Log.d("UserDetailFragment", "Data source : $source")
            }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        LocalProfilePhotoStorage.clearCache(requireContext())
        startActivity(Intent(requireContext(), SignInActivity::class.java))
        activity?.finish()
    }

    private fun showCustomDialog(title: String, setInputDialog: String, key: String) {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle(title)
//            .setView(R.layout.custom_dialog)
//            .show()

        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("setEditInput", setInputDialog)
        bundle.putString("key", key)

        val customDialog = CustomDialog()
        customDialog.arguments = bundle
        customDialog.show(parentFragmentManager, "coustem")
    }

    private fun showCustomPhotoDialog(photoUri: Uri){
        val bundle = Bundle()
        bundle.putString("photoUri",photoUri.toString())
        val customPhotoDialog = CustomPhotoDialog()
        customPhotoDialog.arguments = bundle
        customPhotoDialog.show(parentFragmentManager,"coustem")
    }

    override fun onPause() {
        super.onPause()
        Log.d("UserDetails", "On pause called!")
    }

    override fun onStop() {
        super.onStop()
        Log.d("UserDetails", "On stop called")
    }

    override fun onStart() {
        super.onStart()
        Log.d("UserDetails", "On start called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("UserDetails", "On resume called")
    }
}

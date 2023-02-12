package com.example.talkspace.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment

import androidx.navigation.fragment.findNavController
import com.example.talkspace.ApplicationClass
import com.example.talkspace.R
import com.example.talkspace.databinding.FragmentMainBinding
import com.example.talkspace.ui.*
import com.example.talkspace.viewmodels.ChatViewModel
import com.example.talkspace.viewmodels.ChatViewModelFactory

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

//    private val chatViewModel : ChatViewModel by activityViewModels {
//        ChatViewModelFactory((activity?.application as ApplicationClass)
//            .chatRepository,(activity?.application as ApplicationClass).contactsRepository)
//    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MainFragment","onViewCreated")

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.item_chat ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, FriendListFragment(),"")
                    .commit()
                    true
                }
                R.id.item_call ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, CallHistoryFragment(),"")
                    .commit()
                    true
                }
                R.id.item_add ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, AddContactFragment(),"")
                    .commit()
                    true
                }
                R.id.item_contact ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, ContactFragment(),"")
                    .commit()
                    true
                }
                R.id.item_setting ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, SettingFragment(),"")
                    .commit()
                    true
                }else -> {
                    false
            }
            }
        }
        Log.d("MainFragment","savedInstanceState : $savedInstanceState and " +
                "\n selectedItemId : ${binding.bottomNavigation.autofillId}")
        if(savedInstanceState == null || savedInstanceState.isEmpty){
            binding.bottomNavigation.selectedItemId = R.id.item_chat
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainFragment", "onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainFragment","stop main fragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainFragment.Kt","Destroy main fragment")
    }
}
package com.example.talkspace.adapter

import android.content.Context
import android.text.TextUtils.replace
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.ContactViewBinding
import com.example.talkspace.databinding.OptionButtonViewBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.ui.chatsection.ChatFragment
import com.example.talkspace.viewmodels.ChatViewModel

class ContactAdapter(private val chatViewModel: ChatViewModel):
    ListAdapter<SQLiteContact,RecyclerView.ViewHolder>(ContactsComparator()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("ContactAdapter","Call Contact Adapter ")
        val inflatere = LayoutInflater.from(parent.context)
        val view : View
        return when(viewType){
            0 -> {
                view = inflatere.inflate(R.layout.option_button_view,parent,false)
                OptionButtonViewHolder(OptionButtonViewBinding.bind(view))
            } else -> {
                view = inflatere.inflate(R.layout.contact_view,parent,false)
                ContactViewHolder(ContactViewBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder.itemViewType){
            0 -> {
                (holder as OptionButtonViewHolder).bind()
            }
            else -> {
                (holder as ContactViewHolder).bind(item)
                holder.itemView.setOnClickListener {
                    Log.d("ContactAdapter","select app contact")
                    chatViewModel.setFriendId(item.contactPhoneNumber)
                    chatViewModel.setFriendName(item.contactName)
                    holder.itemView.findNavController().navigate(R.id.action_contactsOnAppFragment_to_chatFragment)
                }
            }
        }
    }

    class ContactViewHolder(private val binding : ContactViewBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(item: SQLiteContact){
                binding.contactName.text = item.contactName
                binding.contactAbout.text = item.contactAbout
            }
    }
    class OptionButtonViewHolder(private val binding: OptionButtonViewBinding):
            RecyclerView.ViewHolder(binding.root){
                fun bind(){
                    binding.optionButtonText.text = "Select Contact"
                }
            }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return 1
    }
    class ContactsComparator: DiffUtil.ItemCallback<SQLiteContact>(){
        override fun areItemsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem.contactPhoneNumber == newItem.contactPhoneNumber
        }

    }
}
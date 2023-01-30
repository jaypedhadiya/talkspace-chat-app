package com.example.talkspace.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.ContactInviteViewBinding
import com.example.talkspace.databinding.ContactOnAppViewBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.viewmodels.ChatViewModel

class ContactAdapter(private val chatViewModel: ChatViewModel) :
ListAdapter<SQLiteContact,RecyclerView.ViewHolder>(ContactsComparator()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("ContactAdapter","call contact Adapter")
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        return when(viewType){
            0 -> {
                view = inflater.inflate(R.layout.contact_on_app_view, parent, false)
                ContactOnAppViewHolder(ContactOnAppViewBinding.bind(view))
            }
            else -> {
                view = inflater.inflate(R.layout.contact_invite_view,parent,false)
                ContactInviteViewHolder(ContactInviteViewBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder.itemViewType){
            0 -> {
                (holder as ContactOnAppViewHolder).bind(item)
                holder.itemView.setOnClickListener {
                    Log.d("ContactAdapter","Select contact")
                    chatViewModel.setFriendId(item.contactPhoneNumber)
                    chatViewModel.setFriendName(item.contactName)
                    holder.itemView.findNavController().navigate(R.id.action_contactsOnAppFragment_to_chatFragment)
                }
            }
            1 -> {
                (holder as ContactInviteViewHolder).bind(item)
            }
        }
    }

    inner class ContactOnAppViewHolder(private val binding: ContactOnAppViewBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: SQLiteContact){
            binding.contactName.text = item.contactName
            binding.contactAbout.text = "I have use Talk Space"
        }
    }

    inner class ContactInviteViewHolder(private val binding: ContactInviteViewBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(item: SQLiteContact){
            when(item.contactName.trim()) {
                " " -> {
                    binding.contactInviteName.text = item.contactPhoneNumber
                }
                else -> {
                    binding.contactInviteName.text = item.contactName
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    class ContactsComparator : DiffUtil.ItemCallback<SQLiteContact>(){
        override fun areItemsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SQLiteContact, newItem: SQLiteContact): Boolean {
            return oldItem.contactPhoneNumber == newItem.contactPhoneNumber
        }

    }
}
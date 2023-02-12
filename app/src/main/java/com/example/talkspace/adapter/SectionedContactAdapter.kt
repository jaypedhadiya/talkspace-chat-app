package com.example.talkspace.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.talkspace.R
import com.example.talkspace.databinding.ContactInviteViewBinding
import com.example.talkspace.databinding.ContactOnAppViewBinding
import com.example.talkspace.databinding.OptionButtonViewBinding
import com.example.talkspace.databinding.SectionViewBinding
import com.example.talkspace.model.SQLiteContact
import com.example.talkspace.viewmodels.ChatViewModel

class SectionedContactAdapter(private val onAppContacts: List<SQLiteContact>,
                              private val notInAppContacts: List<SQLiteContact>,
                              private val chatViewModel: ChatViewModel,
                              private val context: Context
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view : View
        val binding: ViewBinding
        when(viewType){
            NEW_ADD_CONTACTS, ADD_NEW_GROUP -> {
                view = inflater.inflate(R.layout.option_button_view,parent,false)
                binding = OptionButtonViewBinding.bind(view)
                return OptionButtonViewHolder(binding)
            }

//            ADD_GRUPH -> {
//                view = inflater.inflate(R.layout.option_button_view,parent,false)
//                binding = OptionButtonViewBinding.bind(view)
//                return OptionButtonViewHolder(binding)
//            }

            ON_APP_CONTACTS_SECTION_VIEW -> {
                view = inflater.inflate(R.layout.section_view,parent,false)
                binding = SectionViewBinding.bind(view)
                return SectionViewHolder(binding)
            }

            ON_APP_CONTACTS -> {
                view = inflater.inflate(R.layout.contact_on_app_view,parent,false)
                binding = ContactOnAppViewBinding.bind(view)
                return OnAppContactViewHolder(binding)
            }

            NOT_IN_APP_CONTACTS_SECTION_VIEW -> {
                view = inflater.inflate(R.layout.section_view,parent,false)
                binding = SectionViewBinding.bind(view)
                return SectionViewHolder(binding)
            }

            else -> {
                view = inflater.inflate(R.layout.contact_invite_view,parent,false)
                binding = ContactInviteViewBinding.bind(view)
                return NotInAppContactViewHolder(binding)
            }
        }

    }

    override fun getItemCount(): Int {
        return onAppContacts.size + notInAppContacts.size + 4
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is OptionButtonViewHolder -> {
                if (position == 0){
                    holder.bind(position)
                }else {
                    holder.bind(position)
                }
            }
            is SectionViewHolder -> {
                if (position == 2){
                    holder.bind("Contact on Talk Space")
                }else {
                    holder.bind("Invite to Talk Space")
                }
            }
            is OnAppContactViewHolder -> {
                val item = onAppContacts[position - 3]
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    chatViewModel.setFriendName(item.contactName)
                    chatViewModel.setFriendId(item.contactPhoneNumber)
                    holder.itemView.findNavController().navigate(R.id.action_contactsOnAppFragment_to_chatFragment)
                }
            }
            is NotInAppContactViewHolder -> {
                val item = notInAppContacts[position - onAppContacts.size - 4]
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
//        super.getItemViewType(position)
        return when {
            position == 0 -> NEW_ADD_CONTACTS
            position == 1 -> ADD_NEW_GROUP
            position == 2 -> ON_APP_CONTACTS_SECTION_VIEW
            position <= onAppContacts.size + 2 -> ON_APP_CONTACTS
            position == onAppContacts.size + 3 -> NOT_IN_APP_CONTACTS_SECTION_VIEW
            else ->  NOT_IN_APP_CONTACTS
        }
    }

    inner class SectionViewHolder(private val binding: SectionViewBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(title: String){
                binding.sectionTitle.text = title
            }
    }

    inner class OnAppContactViewHolder(private val binding: ContactOnAppViewBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(item: SQLiteContact){
                binding.contactName.text = item.contactName
                binding.contactAbout.text = item.contactAbout
            }
    }

    inner class NotInAppContactViewHolder(private val binding: ContactInviteViewBinding):
        RecyclerView.ViewHolder(binding.root){
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
    inner class OptionButtonViewHolder(private val binding: OptionButtonViewBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(position: Int){
                if (position == 0){
                    binding.optionButtonText.text = NEW_CONTACT
                    binding.optionButtonIcon.setImageDrawable(
                        ContextCompat.getDrawable(context,R.drawable.baseline_person_add_24)
                    )
                }else if (position == 1){
                    binding.optionButtonText.text = NEW_GROUP
                    binding.optionButtonIcon.setImageDrawable(
                        ContextCompat.getDrawable(context,R.drawable.baseline_groups_24)
                    )
                }
            }
    }

    companion object{
        const val NEW_ADD_CONTACTS = 0
        const val ADD_NEW_GROUP = 1
        const val ON_APP_CONTACTS_SECTION_VIEW = 2
        const val ON_APP_CONTACTS = 3
        const val NOT_IN_APP_CONTACTS_SECTION_VIEW = 4
        const val NOT_IN_APP_CONTACTS = 5
        const val NEW_GROUP = "New Group"
        const val NEW_CONTACT = "New Contact"
    }
}
package com.example.talkspace.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.talkspace.R
import com.example.talkspace.databinding.OptionButtonViewBinding
import com.example.talkspace.databinding.SectionViewBinding
import com.example.talkspace.model.Section
import com.example.talkspace.viewmodels.ChatViewModel

class SectionAdapter(private val chatViewModel: ChatViewModel, val context: Context):
    ListAdapter<Section,RecyclerView.ViewHolder>(SectionsComparator()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("SectionAdapter","Call Section Adapter ")
        val inflatere = LayoutInflater.from(parent.context)
        val view : View
        return when(viewType){
            0,1,2 -> {
                view = inflatere.inflate(R.layout.option_button_view,parent,false)
                OptionButtonViewHolder(OptionButtonViewBinding.bind(view))
            } else -> {
                view = inflatere.inflate(R.layout.section_view,parent,false)
                SectionViewHolder(SectionViewBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder.itemViewType){
            0 -> {
                (holder as OptionButtonViewHolder).bind(0)
                holder.itemView.setOnClickListener {
                    Log.d("SectionAdapter","Contact item type : 0")

                }
            }
            1 -> {
                (holder as OptionButtonViewHolder).bind(1)
                holder.itemView.setOnClickListener {
                    Log.d("SectionAdapter","Contact item type : 1")
                }
            }
            2 -> {
                (holder as OptionButtonViewHolder).bind(2)
                holder.itemView.setOnClickListener {
                    Log.d("SectionAdapter","Contact item type : 2")
                }
            }
            else -> {
                (holder as SectionViewHolder).bind(item)
            }
        }
    }

    inner class SectionViewHolder(private val binding : SectionViewBinding):
        RecyclerView.ViewHolder(binding.root){
            fun bind(item: Section){
                binding.sectionTitle.text = item.sectionTitle
                binding.sectionItemRecyclerview.layoutManager =
                    LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
                val adapter = ContactAdapter(chatViewModel)
                adapter.submitList(item.contacts)



                Log.d("SectionAdapter","List of contacts : ${adapter.currentList}")
                binding.sectionItemRecyclerview.adapter = adapter
                Log.d("SectionAdapter","List of contacts : ${item.contacts}")

            }
    }
    inner class OptionButtonViewHolder(private val binding: OptionButtonViewBinding):
            RecyclerView.ViewHolder(binding.root){
                fun bind(viewType: Int){
                    when(viewType){
                        0 -> {
                            binding.optionButtonIcon.setImageDrawable(
                                ContextCompat.getDrawable(context,R.drawable.ic_baseline_person_24)
                            )
                            binding.optionButtonText.text = SELECT_CONTACT
                        }
                        1 -> {
                            binding.optionButtonIcon.setImageDrawable(
                                ContextCompat.getDrawable(context,R.drawable.baseline_person_add_24)
                            )
                            binding.optionButtonText.text = NEW_CONTACT
                        }
                        else -> {
                            binding.optionButtonIcon.setImageDrawable(
                                ContextCompat.getDrawable(context,R.drawable.baseline_groups_24)
                            )
                            binding.optionButtonText.text = NEW_GROUP
                        }
                    }

                }
            }

    override fun getItemViewType(position: Int): Int {
        return when(position < 3){
            true -> position
            else -> 3
        }
    }
    class SectionsComparator: DiffUtil.ItemCallback<Section>(){
        override fun areItemsTheSame(oldItem: Section, newItem: Section): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Section, newItem: Section): Boolean {
            return oldItem.id == newItem.id
        }

    }

    companion object {
        const val NEW_GROUP = "New Group"
        const val NEW_CONTACT = "New Contact"
        const val SELECT_CONTACT = "Select Contact"
    }
}
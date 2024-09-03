package com.example.phonepeoptions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.phonepeoptions.R
import com.example.phonepeoptions.databinding.SavedInstrumentsListItemBinding

class SavedInstrumentsAdapter(private val onInstrumentSelected: (SavedInstrumentsListItem) -> Unit) :
    ListAdapter<SavedInstrumentsListItem, SavedInstrumentsAdapter.SavedInstrumentsViewHolder>(SavedInstrumentsDiffCallback()) {

    private var savedInstrumentsList: List<SavedInstrumentsListItem> = emptyList()
    private var selectedInstrument: SavedInstrumentsListItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SavedInstrumentsViewHolder(
        SavedInstrumentsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SavedInstrumentsViewHolder, position: Int) {
        holder.binding.title.text = savedInstrumentsList[position].title
        holder.binding.radioButton.isChecked = savedInstrumentsList[position] == selectedInstrument

        holder.binding.radioButton.setOnClickListener {
            onInstrumentSelected(savedInstrumentsList[position])
        }

        holder.binding.title.setOnClickListener {
            onInstrumentSelected(savedInstrumentsList[position])
        }

        if (!savedInstrumentsList[position].isAvailable) {
            holder.binding.title.setTextColor(
                getColor(
                    holder.binding.title.context,
                    R.color.disabled
                )
            )
        }
    }

    override fun getItemCount(): Int = savedInstrumentsList.size

    fun updateList(newList: List<SavedInstrumentsListItem>) {
        savedInstrumentsList = newList
        selectedInstrument = null
        notifyDataSetChanged()
    }

    fun setSelectedInstrument(instrument: SavedInstrumentsListItem?) {
        selectedInstrument = instrument
        notifyDataSetChanged()
    }

    inner class SavedInstrumentsViewHolder(val binding: SavedInstrumentsListItemBinding) : RecyclerView.ViewHolder(binding.root)

    class SavedInstrumentsDiffCallback: DiffUtil.ItemCallback<SavedInstrumentsListItem>() {
        override fun areItemsTheSame(
            oldItem: SavedInstrumentsListItem,
            newItem: SavedInstrumentsListItem
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: SavedInstrumentsListItem,
            newItem: SavedInstrumentsListItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
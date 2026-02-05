package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

sealed class SearchResultItem {
    data class Room(val id: String, val title: String) : SearchResultItem()
    data class User(val id: String, val title: String) : SearchResultItem()
    data class Agency(val id: String, val title: String) : SearchResultItem()
}

class RoomsSearchAdapter(
    private val onClick: (SearchResultItem) -> Unit
) : RecyclerView.Adapter<RoomsSearchAdapter.SearchViewHolder>() {

    private val items = mutableListOf<SearchResultItem>()

    fun submitList(list: List<SearchResultItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class SearchViewHolder(
        itemView: View,
        private val onClick: (SearchResultItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val typeText: TextView = itemView.findViewById(R.id.text_search_type)
        private val titleText: TextView = itemView.findViewById(R.id.text_search_title)

        fun bind(item: SearchResultItem) {
            when (item) {
                is SearchResultItem.Room -> {
                    typeText.text = "[ROOM]"
                    titleText.text = "${item.title} (${item.id})"
                }
                is SearchResultItem.User -> {
                    typeText.text = "[USER]"
                    titleText.text = "${item.title} (${item.id})"
                }
                is SearchResultItem.Agency -> {
                    typeText.text = "[AGENCY]"
                    titleText.text = "${item.title} (${item.id})"
                }
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }
}

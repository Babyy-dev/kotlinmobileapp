package com.kappa.app.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

class SimpleRowAdapter : RecyclerView.Adapter<SimpleRowAdapter.RowViewHolder>() {

    private val items = mutableListOf<Pair<String, String>>()

    fun submitRows(rows: List<Pair<String, String>>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_row, parent, false)
        return RowViewHolder(view)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(items[position].first, items[position].second)
    }

    override fun getItemCount(): Int = items.size

    class RowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.text_row_title)
        private val subtitle = itemView.findViewById<TextView>(R.id.text_row_subtitle)

        fun bind(title: String, subtitle: String) {
            this.title.text = title
            this.subtitle.text = subtitle
        }
    }
}

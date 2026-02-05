package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import com.kappa.app.domain.home.HomeBanner

class RoomsBannerAdapter(
    private val onClick: (HomeBanner) -> Unit
) : RecyclerView.Adapter<RoomsBannerAdapter.BannerViewHolder>() {

    private val items = mutableListOf<HomeBanner>()

    fun submitList(list: List<HomeBanner>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_banner, parent, false)
        return BannerViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BannerViewHolder(
        itemView: View,
        private val onClick: (HomeBanner) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_banner_title)
        private val subtitle: TextView = itemView.findViewById(R.id.text_banner_subtitle)

        fun bind(item: HomeBanner) {
            title.text = item.title
            subtitle.text = item.subtitle ?: ""
            itemView.setOnClickListener { onClick(item) }
        }
    }
}

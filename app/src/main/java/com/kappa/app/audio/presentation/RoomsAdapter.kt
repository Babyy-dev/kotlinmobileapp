package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kappa.app.R
import com.kappa.app.domain.audio.AudioRoom

class RoomsAdapter(
    private val onJoinClicked: (AudioRoom) -> Unit
) : RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {

    private val items = mutableListOf<AudioRoom>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view, onJoinClicked)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(rooms: List<AudioRoom>) {
        items.clear()
        items.addAll(rooms)
        notifyDataSetChanged()
    }

    class RoomViewHolder(
        itemView: View,
        private val onJoinClicked: (AudioRoom) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_room_name)
        private val modeText: TextView = itemView.findViewById(R.id.text_room_mode)
        private val lockText: TextView = itemView.findViewById(R.id.text_room_lock)
        private val badgeText: TextView = itemView.findViewById(R.id.text_room_badge)
        private val countText: TextView = itemView.findViewById(R.id.text_room_count)
        private val joinButton: MaterialButton = itemView.findViewById(R.id.button_join_room)

        fun bind(room: AudioRoom) {
            nameText.text = room.name
            modeText.text = room.seatMode.name
            lockText.text = if (room.requiresPassword) "Locked" else "Open"
            badgeText.visibility = if (room.isActive) View.VISIBLE else View.INVISIBLE
            badgeText.text = if (room.isActive) "LIVE" else "OFF"
            countText.text = "${room.participantCount} listening"
            joinButton.isEnabled = room.isActive
            joinButton.setOnClickListener { onJoinClicked(room) }
        }
    }
}

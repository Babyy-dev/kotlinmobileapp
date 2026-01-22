package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import com.kappa.app.domain.audio.RoomMessage

class RoomMessagesAdapter : RecyclerView.Adapter<RoomMessagesAdapter.MessageViewHolder>() {

    private val items = mutableListOf<RoomMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(messages: List<RoomMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userText: TextView = itemView.findViewById(R.id.text_message_user)
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)

        fun bind(message: RoomMessage) {
            userText.text = message.username
            messageText.text = message.message
        }
    }
}

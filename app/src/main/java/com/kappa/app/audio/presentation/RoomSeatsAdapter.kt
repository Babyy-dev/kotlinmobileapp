package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import com.kappa.app.domain.audio.RoomSeat
import com.kappa.app.domain.audio.SeatStatus

class RoomSeatsAdapter(
    private val onSeatClick: (RoomSeat) -> Unit,
    private val onSeatLongClick: (RoomSeat) -> Unit = {}
) : RecyclerView.Adapter<RoomSeatsAdapter.SeatViewHolder>() {

    private val items = mutableListOf<RoomSeat>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_seat, parent, false)
        return SeatViewHolder(view, onSeatClick, onSeatLongClick)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(seats: List<RoomSeat>) {
        items.clear()
        items.addAll(seats)
        notifyDataSetChanged()
    }

    class SeatViewHolder(
        itemView: View,
        private val onSeatClick: (RoomSeat) -> Unit,
        private val onSeatLongClick: (RoomSeat) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val seatCard: View = itemView.findViewById(R.id.seat_card)
        private val titleText: TextView = itemView.findViewById(R.id.text_seat_title)
        private val statusText: TextView = itemView.findViewById(R.id.text_seat_status)

        fun bind(seat: RoomSeat) {
            titleText.text = "No.${seat.seatNumber}"
            val occupant = seat.username ?: seat.userId?.take(6)
            val lightText = ContextCompat.getColor(itemView.context, R.color.kappa_cream)
            val mintText = ContextCompat.getColor(itemView.context, R.color.kappa_mint_100)
            val darkText = ContextCompat.getColor(itemView.context, R.color.kappa_charcoal)
            val mutedText = ContextCompat.getColor(itemView.context, R.color.kappa_text_muted)
            when (seat.status) {
                SeatStatus.FREE -> {
                    seatCard.setBackgroundResource(R.drawable.bg_seat_free)
                    titleText.setTextColor(lightText)
                    statusText.setTextColor(mintText)
                    statusText.text = "Open"
                }
                SeatStatus.OCCUPIED -> {
                    seatCard.setBackgroundResource(R.drawable.bg_seat_occupied)
                    titleText.setTextColor(darkText)
                    statusText.setTextColor(darkText)
                    statusText.text = occupant ?: "Live"
                }
                SeatStatus.BLOCKED -> {
                    seatCard.setBackgroundResource(R.drawable.bg_seat_blocked)
                    titleText.setTextColor(mintText)
                    statusText.setTextColor(mutedText)
                    statusText.text = "Locked"
                }
            }
            itemView.setOnClickListener { onSeatClick(seat) }
            itemView.setOnLongClickListener {
                onSeatLongClick(seat)
                true
            }
        }
    }
}

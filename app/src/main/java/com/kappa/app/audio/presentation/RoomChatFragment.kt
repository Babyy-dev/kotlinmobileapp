package com.kappa.app.audio.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoomChatFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels()
    private val messagesAdapter = RoomMessagesAdapter()
    private var selectedTab: String = "CHAT"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_room_messages)
        val input = view.findViewById<TextInputEditText>(R.id.input_room_message)
        val sendButton = view.findViewById<MaterialButton>(R.id.button_send_message)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_room_messages)

        tabLayout.addTab(tabLayout.newTab().setText("Chat").setTag("CHAT"))
        tabLayout.addTab(tabLayout.newTab().setText("Rewards").setTag("REWARD"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.tag?.toString() ?: "CHAT"
                audioViewModel.markMessagesSeen()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = messagesAdapter
        recycler.setHasFixedSize(true)

        sendButton.setOnClickListener {
            val message = input.text?.toString().orEmpty()
            if (message.isBlank()) return@setOnClickListener
            audioViewModel.sendMessage(message)
            input.setText("")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    val filtered = if (selectedTab == "REWARD") {
                        state.messages.filter { it.messageType.equals("REWARD", ignoreCase = true) }
                    } else {
                        state.messages.filterNot { it.messageType.equals("REWARD", ignoreCase = true) }
                    }
                    messagesAdapter.submitList(filtered)
                    if (state.messages.isNotEmpty()) {
                        recycler.scrollToPosition((filtered.size - 1).coerceAtLeast(0))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        audioViewModel.markMessagesSeen()
    }
}

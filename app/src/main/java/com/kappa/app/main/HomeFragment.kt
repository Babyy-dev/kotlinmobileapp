package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home screen fragment with inbox/friends/family.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var inboxAdapter: InboxAdapter
    private lateinit var friendsAdapter: InboxAdapter
    private val familyMembersAdapter = SimpleRowAdapter()
    private val familyRoomsAdapter = SimpleRowAdapter()
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messagesRecycler = view.findViewById<RecyclerView>(R.id.recycler_inbox_messages)
        val friendsRecycler = view.findViewById<RecyclerView>(R.id.recycler_inbox_friends)
        inboxAdapter = InboxAdapter { item ->
            viewModel.markThreadRead(item.id)
            Toast.makeText(requireContext(), "Opened chat with ${item.name}", Toast.LENGTH_SHORT).show()
        }
        friendsAdapter = InboxAdapter { item ->
            Toast.makeText(requireContext(), "Opened profile for ${item.name}", Toast.LENGTH_SHORT).show()
        }
        messagesRecycler.layoutManager = LinearLayoutManager(requireContext())
        messagesRecycler.adapter = inboxAdapter
        friendsRecycler.layoutManager = LinearLayoutManager(requireContext())
        friendsRecycler.adapter = friendsAdapter
        messagesRecycler.setHasFixedSize(true)
        friendsRecycler.setHasFixedSize(true)

        val familyMembersRecycler = view.findViewById<RecyclerView>(R.id.recycler_family_members)
        val familyRoomsRecycler = view.findViewById<RecyclerView>(R.id.recycler_family_rooms)
        val familyNameInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.input_family_name)
        val familyIdInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.input_family_id)
        val familyRoomNameInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.input_family_room_name)
        val familyCreateButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_family_create)
        val familyJoinButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_family_join)
        val familyRoomCreateButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_family_room_create)
        val familyCurrentText = view.findViewById<TextView>(R.id.text_family_current)
        val familyCodeText = view.findViewById<TextView>(R.id.text_family_code)
        val familyStatusText = view.findViewById<TextView>(R.id.text_family_status)
        familyMembersRecycler.layoutManager = LinearLayoutManager(requireContext())
        familyMembersRecycler.adapter = familyMembersAdapter
        familyRoomsRecycler.layoutManager = LinearLayoutManager(requireContext())
        familyRoomsRecycler.adapter = familyRoomsAdapter
        familyMembersRecycler.setHasFixedSize(true)
        familyRoomsRecycler.setHasFixedSize(true)

        val tabMensagem = view.findViewById<TextView>(R.id.tab_inbox_mensagem)
        val tabAmigos = view.findViewById<TextView>(R.id.tab_inbox_amigos)
        val tabFamilia = view.findViewById<TextView>(R.id.tab_inbox_familia)
        val sectionMensagem = view.findViewById<View>(R.id.section_inbox_mensagem)
        val sectionAmigos = view.findViewById<View>(R.id.section_inbox_amigos)
        val sectionFamilia = view.findViewById<View>(R.id.section_inbox_familia)
        val friendsSearchInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.input_friends_search)

        fun setActiveTab(active: TextView) {
            val activeColor = resources.getColor(R.color.kappa_gold_300, null)
            val inactiveColor = resources.getColor(R.color.kappa_cream, null)
            tabMensagem.setTextColor(inactiveColor)
            tabAmigos.setTextColor(inactiveColor)
            tabFamilia.setTextColor(inactiveColor)
            active.setTextColor(activeColor)
        }

        fun showSection(mensagem: Boolean, amigos: Boolean, familia: Boolean) {
            sectionMensagem.visibility = if (mensagem) View.VISIBLE else View.GONE
            sectionAmigos.visibility = if (amigos) View.VISIBLE else View.GONE
            sectionFamilia.visibility = if (familia) View.VISIBLE else View.GONE
        }

        tabMensagem.setOnClickListener {
            setActiveTab(tabMensagem)
            showSection(mensagem = true, amigos = false, familia = false)
        }
        tabAmigos.setOnClickListener {
            setActiveTab(tabAmigos)
            showSection(mensagem = false, amigos = true, familia = false)
        }
        tabFamilia.setOnClickListener {
            setActiveTab(tabFamilia)
            showSection(mensagem = false, amigos = false, familia = true)
        }

        familyCreateButton.setOnClickListener {
            val name = familyNameInput.text?.toString()?.trim().orEmpty()
            viewModel.createFamily(name)
        }

        familyJoinButton.setOnClickListener {
            val code = familyIdInput.text?.toString()?.trim().orEmpty()
            viewModel.joinFamily(code)
        }

        familyRoomCreateButton.setOnClickListener {
            val name = familyRoomNameInput.text?.toString()?.trim().orEmpty()
            viewModel.createFamilyRoom(name)
        }

        friendsSearchInput.addTextChangedListener {
            viewModel.searchFriends(it?.toString().orEmpty())
        }

        viewModel.loadAll()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    inboxAdapter.submitList(state.inbox)
                    friendsAdapter.submitList(state.friendSearch)
                    familyMembersAdapter.submitRows(state.familyMembers)
                    familyRoomsAdapter.submitRows(state.familyRooms)
                    if (state.familyName.isNullOrBlank()) {
                        familyCurrentText.text = "Nenhuma familia"
                        familyCodeText.text = "Codigo: -"
                    } else {
                        familyCurrentText.text = "Familia: ${state.familyName}"
                        familyCodeText.text = "Codigo: ${state.familyCode ?: "-"}"
                    }
                    val hasFamily = state.family != null
                    familyRoomCreateButton.isEnabled = hasFamily
                    familyRoomNameInput.isEnabled = hasFamily
                    if (state.isLoading) {
                        familyStatusText.text = "Atualizando..."
                        familyStatusText.visibility = View.VISIBLE
                    } else if (state.message.isNullOrBlank()) {
                        familyStatusText.visibility = View.GONE
                    } else {
                        familyStatusText.text = state.message
                        familyStatusText.visibility = View.VISIBLE
                    }
                    if (state.message != null) {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.clearMessage()
                    }
                }
            }
        }
    }
}

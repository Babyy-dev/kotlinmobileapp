package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home screen fragment with technical dashboard.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val inboxAdapter = InboxAdapter()
    private val friendsAdapter = InboxAdapter()

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
        messagesRecycler.layoutManager = LinearLayoutManager(requireContext())
        messagesRecycler.adapter = inboxAdapter
        friendsRecycler.layoutManager = LinearLayoutManager(requireContext())
        friendsRecycler.adapter = friendsAdapter

        val tabMensagem = view.findViewById<TextView>(R.id.tab_inbox_mensagem)
        val tabAmigos = view.findViewById<TextView>(R.id.tab_inbox_amigos)
        val tabFamilia = view.findViewById<TextView>(R.id.tab_inbox_familia)
        val sectionMensagem = view.findViewById<View>(R.id.section_inbox_mensagem)
        val sectionAmigos = view.findViewById<View>(R.id.section_inbox_amigos)
        val sectionFamilia = view.findViewById<View>(R.id.section_inbox_familia)

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

        inboxAdapter.submitList(
            listOf(
                InboxItem(name = "User10", message = "Esse cara é tão preguiçoso...", badge = "VIP", isOnline = true),
                InboxItem(name = "Be crazy", message = "Be crazy enough to know...", badge = null, isOnline = false),
                InboxItem(name = "Admin Japinha", message = "ABRIMOS SUA A...", badge = "ADM", isOnline = true),
                InboxItem(name = "AGT.JÜH", message = "Águia não anda com hiena.", badge = null, isOnline = true)
            )
        )

        friendsAdapter.submitList(
            listOf(
                InboxItem(name = "Sophie", message = "Online agora", badge = null, isOnline = true),
                InboxItem(name = "Luna", message = "Saiu há 5 min", badge = null, isOnline = false),
                InboxItem(name = "Admin Japinha", message = "Disponível", badge = "ADM", isOnline = true)
            )
        )
    }
}

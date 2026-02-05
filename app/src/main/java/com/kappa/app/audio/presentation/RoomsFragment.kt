package com.kappa.app.audio.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.kappa.app.R
import com.kappa.app.core.network.NetworkMonitor
import com.kappa.app.domain.audio.AudioRoom
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class RoomsFragment : Fragment() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val audioViewModel: AudioViewModel by activityViewModels()
    private lateinit var roomsAdapter: RoomsAdapter
    private lateinit var popularRoomsAdapter: RoomsAdapter
    private lateinit var bannerAdapter: RoomsBannerAdapter
    private lateinit var miniGameAdapter: MiniGameStripAdapter
    private lateinit var searchAdapter: RoomsSearchAdapter
    private var lastNavigatedRoomId: String? = null
    private var shouldRefreshOnReconnect = false
    private var selectedFilter: String? = null
    private var bannerJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rooms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_rooms)
        val popularRecycler = view.findViewById<RecyclerView>(R.id.recycler_rooms_popular)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_rooms)
        val errorText = view.findViewById<TextView>(R.id.text_rooms_error)
        val refreshButton = view.findViewById<View>(R.id.button_refresh_rooms)
        val createButton = view.findViewById<View>(R.id.button_create_room)
        val gamesButton = view.findViewById<View>(R.id.button_open_games)
        val tabMeu = view.findViewById<TextView>(R.id.tab_rooms_meu)
        val tabPopular = view.findViewById<TextView>(R.id.tab_rooms_popular)
        val tabPosts = view.findViewById<TextView>(R.id.tab_rooms_posts)
        val sectionMeu = view.findViewById<View>(R.id.section_rooms_meu)
        val sectionPopular = view.findViewById<View>(R.id.section_rooms_popular)
        val sectionPosts = view.findViewById<View>(R.id.section_rooms_posts)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_countries)
        val bannerPager = view.findViewById<ViewPager2>(R.id.pager_rooms_banner)
        val bannerTabs = view.findViewById<TabLayout>(R.id.tab_rooms_banner)
        val miniGamesRecycler = view.findViewById<RecyclerView>(R.id.recycler_rooms_games)
        val searchButton = view.findViewById<View>(R.id.button_rooms_search)
        val notifyButton = view.findViewById<View>(R.id.button_rooms_notifications)
        val myRoomButton = view.findViewById<View>(R.id.button_rooms_myroom)

        roomsAdapter = RoomsAdapter { room ->
            if (room.requiresPassword) {
                showPasswordPrompt(room)
            } else {
                audioViewModel.joinRoom(room.id)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = roomsAdapter
        recyclerView.setHasFixedSize(true)
        popularRoomsAdapter = RoomsAdapter { room ->
            if (room.requiresPassword) {
                showPasswordPrompt(room)
            } else {
                audioViewModel.joinRoom(room.id)
            }
        }
        popularRecycler.layoutManager = LinearLayoutManager(requireContext())
        popularRecycler.adapter = popularRoomsAdapter
        popularRecycler.setHasFixedSize(true)

        audioViewModel.loadAudioRooms()
        audioViewModel.loadHomeContent()

        refreshButton.setOnClickListener {
            audioViewModel.loadAudioRooms()
        }

        createButton.setOnClickListener {
            showCreateRoomDialog()
        }

        gamesButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_game_hub)
        }

        bannerAdapter = RoomsBannerAdapter { banner ->
            when (banner.actionType?.lowercase()) {
                "games" -> findNavController().navigate(R.id.navigation_game_hub)
                else -> {}
            }
        }
        bannerPager.adapter = bannerAdapter
        TabLayoutMediator(bannerTabs, bannerPager) { _, _ -> }.attach()

        miniGameAdapter = MiniGameStripAdapter { game ->
            val bundle = Bundle().apply {
                putString("game_type", game.id)
                putString("game_title", game.title)
                putLong("game_fee", game.entryFee)
            }
            findNavController().navigate(R.id.navigation_game_detail, bundle)
        }
        miniGamesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        miniGamesRecycler.adapter = miniGameAdapter

        searchAdapter = RoomsSearchAdapter { item ->
            when (item) {
                is SearchResultItem.Room -> audioViewModel.joinRoom(item.id)
                else -> Toast.makeText(requireContext(), "Open ${item}", Toast.LENGTH_SHORT).show()
            }
        }

        searchButton.setOnClickListener {
            showSearchDialog()
        }

        notifyButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_inbox)
        }

        myRoomButton.setOnClickListener {
            showCreateRoomDialog()
        }

        fun setActiveTab(active: TextView) {
            val activeColor = resources.getColor(R.color.kappa_gold_300, null)
            val inactiveColor = resources.getColor(R.color.kappa_cream, null)
            tabMeu.setTextColor(inactiveColor)
            tabPopular.setTextColor(inactiveColor)
            tabPosts.setTextColor(inactiveColor)
            active.setTextColor(activeColor)
        }

        fun showSection(meu: Boolean, popular: Boolean, posts: Boolean) {
            sectionMeu.visibility = if (meu) View.VISIBLE else View.GONE
            sectionPopular.visibility = if (popular) View.VISIBLE else View.GONE
            sectionPosts.visibility = if (posts) View.VISIBLE else View.GONE
        }

        fun applyFilter(rooms: List<AudioRoom>, regions: List<String>) {
            val selected = selectedFilter
            val filtered = when {
                selected.isNullOrBlank() || selected == "Popular" -> rooms.sortedByDescending { it.participantCount }
                regions.contains(selected) -> rooms.filter { it.region.equals(selected, ignoreCase = true) }
                else -> rooms.filter { it.country.equals(selected, ignoreCase = true) }
            }
            roomsAdapter.submitList(filtered)
            popularRoomsAdapter.submitList(filtered)
        }

        fun renderCountryChips(rooms: List<AudioRoom>) {
            val countries = rooms.mapNotNull { it.country }.distinct().sorted()
            val regions = rooms.mapNotNull { it.region }.distinct().sorted()
            val items = listOf("Popular") + regions + countries
            if (chipGroup.childCount == items.size && selectedFilter != null) {
                return
            }
            chipGroup.removeAllViews()
            items.forEach { label ->
                val chip = Chip(requireContext()).apply {
                    text = label
                    isCheckable = true
                    isClickable = true
                }
                chipGroup.addView(chip)
            }
            val initial = items.firstOrNull() ?: "Popular"
            val initialIndex = items.indexOf(selectedFilter ?: initial).coerceAtLeast(0)
            (chipGroup.getChildAt(initialIndex) as? Chip)?.isChecked = true
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId)
            selectedFilter = chip?.text?.toString()
            val regions = audioViewModel.viewState.value.rooms.mapNotNull { it.region }.distinct()
            applyFilter(audioViewModel.viewState.value.rooms, regions)
        }

        tabMeu.setOnClickListener {
            setActiveTab(tabMeu)
            showSection(meu = true, popular = false, posts = false)
        }
        tabPopular.setOnClickListener {
            setActiveTab(tabPopular)
            showSection(meu = false, popular = true, posts = false)
        }
        tabPosts.setOnClickListener {
            setActiveTab(tabPosts)
            showSection(meu = false, popular = false, posts = true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    progressBar.visibility = if (state.isLoading || state.isJoining) View.VISIBLE else View.GONE
                    if (state.error != null) {
                        errorText.text = state.error
                        errorText.visibility = View.VISIBLE
                        if (state.error.contains("No internet", ignoreCase = true) ||
                            state.error.contains("timeout", ignoreCase = true)
                        ) {
                            shouldRefreshOnReconnect = true
                        }
                    } else {
                        errorText.visibility = View.GONE
                    }
                    renderCountryChips(state.rooms)
                    val regions = state.rooms.mapNotNull { it.region }.distinct()
                    applyFilter(state.rooms, regions)
                    bannerAdapter.submitList(state.banners)
                    miniGameAdapter.submitList(state.popularGames)
                    if (state.banners.isNotEmpty()) {
                        scheduleBannerAutoScroll(bannerPager, state.banners.size)
                    }

                    val activeRoom = state.activeRoom
                    if (activeRoom == null) {
                        lastNavigatedRoomId = null
                    }
                    if (activeRoom != null && state.token != null) {
                        if (lastNavigatedRoomId != activeRoom.id &&
                            findNavController().currentDestination?.id == R.id.navigation_rooms
                        ) {
                            lastNavigatedRoomId = activeRoom.id
                            findNavController().navigate(R.id.navigation_room_detail)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.collect { isOnline ->
                    if (isOnline && shouldRefreshOnReconnect) {
                        shouldRefreshOnReconnect = false
                        audioViewModel.loadAudioRooms()
                    }
                }
            }
        }
    }

    private fun scheduleBannerAutoScroll(pager: ViewPager2, size: Int) {
        if (size <= 1) return
        if (bannerJob?.isActive == true) return
        bannerJob = viewLifecycleOwner.lifecycleScope.launch {
            var index = 0
            while (true) {
                delay(4000)
                index = (index + 1) % size
                pager.currentItem = index
            }
        }
    }

    private fun showSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rooms_search, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.input_search_query)
        val button = dialogView.findViewById<View>(R.id.button_search_submit)
        val recycler = dialogView.findViewById<RecyclerView>(R.id.recycler_search_results)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = searchAdapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Search")
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .create()

        button.setOnClickListener {
            val query = input.text?.toString()?.trim().orEmpty()
            if (query.isNotBlank()) {
                audioViewModel.searchAll(query)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    val results = buildList<SearchResultItem> {
                        state.searchResults.rooms.forEach { add(SearchResultItem.Room(it.id, it.name)) }
                        state.searchResults.users.forEach { add(SearchResultItem.User(it.id, it.nickname ?: it.username)) }
                        state.searchResults.agencies.forEach { add(SearchResultItem.Agency(it.id, it.name)) }
                    }
                    searchAdapter.submitList(results)
                }
            }
        }
        dialog.show()
    }

    private fun showCreateRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_name)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_password)

        AlertDialog.Builder(requireContext())
            .setTitle("Create Room")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    Toast.makeText(requireContext(), "Room name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val password = passwordInput.text?.toString()?.trim().orEmpty().ifBlank { null }
                audioViewModel.createRoom(name, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPasswordPrompt(room: AudioRoom) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_name)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_password)
        nameInput.setText(room.name)
        nameInput.isEnabled = false

        AlertDialog.Builder(requireContext())
            .setTitle("Room Password")
            .setView(dialogView)
            .setPositiveButton("Join") { _, _ ->
                val password = passwordInput.text?.toString()?.trim().orEmpty()
                if (password.isBlank()) {
                    Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                audioViewModel.joinRoom(room.id, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        bannerJob?.cancel()
        bannerJob = null
        super.onDestroyView()
    }
}

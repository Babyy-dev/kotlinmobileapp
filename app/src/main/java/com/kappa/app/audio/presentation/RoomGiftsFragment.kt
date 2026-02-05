package com.kappa.app.audio.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.kappa.app.R
import com.kappa.app.domain.audio.GiftCatalogItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoomGiftsFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels()
    private lateinit var giftAdapter: RoomGiftAdapter
    private lateinit var recipientAdapter: RoomGiftRecipientsAdapter
    private var selectedGift: GiftCatalogItem? = null
    private var selectedCategory: String? = null
    private var targetMode: String = "SELF"
    private val selectedRecipients = mutableSetOf<String>()
    private var roomRecipientIds: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_gifts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_gift_catalog)
        val selectedText = view.findViewById<TextView>(R.id.text_gift_selected)
        val sendButton = view.findViewById<MaterialButton>(R.id.button_send_gift)
        val categoryGroup = view.findViewById<ChipGroup>(R.id.chip_group_gift_category)
        val targetGroup = view.findViewById<ChipGroup>(R.id.chip_group_gift_target)
        val recipientsRecycler = view.findViewById<RecyclerView>(R.id.recycler_gift_recipients)

        giftAdapter = RoomGiftAdapter { item ->
            selectedGift = GiftCatalogItem(
                id = item.id,
                name = item.name,
                giftType = item.giftType,
                costCoins = item.price,
                diamondPercent = (item.conversionRate * 100).toInt(),
                category = item.category.name,
                imageUrl = null
            )
            selectedText.text = "${item.name} - ${item.price} coins"
            giftAdapter.setSelected(item.id)
        }

        recycler.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler.adapter = giftAdapter

        recipientAdapter = RoomGiftRecipientsAdapter { item ->
            if (selectedRecipients.contains(item.userId)) {
                selectedRecipients.remove(item.userId)
            } else {
                selectedRecipients.add(item.userId)
            }
            recipientAdapter.setSelected(selectedRecipients)
        }
        recipientsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recipientsRecycler.adapter = recipientAdapter

        fun updateCategoryChips(categories: List<String>) {
            if (categoryGroup.childCount == categories.size && selectedCategory != null) return
            categoryGroup.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(requireContext()).apply {
                    text = category
                    isCheckable = true
                }
                categoryGroup.addView(chip)
            }
            val initial = selectedCategory ?: categories.firstOrNull()
            val initialIndex = categories.indexOf(initial).coerceAtLeast(0)
            (categoryGroup.getChildAt(initialIndex) as? Chip)?.isChecked = true
            selectedCategory = initial
        }

        fun updateTargetChips() {
            if (targetGroup.childCount > 0) return
            listOf("SELF", "ALL", "SELECTED").forEach { label ->
                val chip = Chip(requireContext()).apply {
                    text = label
                    isCheckable = true
                }
                targetGroup.addView(chip)
            }
            (targetGroup.getChildAt(0) as? Chip)?.isChecked = true
        }

        updateTargetChips()

        categoryGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId)
            selectedCategory = chip?.text?.toString()
        }

        targetGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId)
            targetMode = chip?.text?.toString() ?: "SELF"
        }

        sendButton.setOnClickListener {
            val gift = selectedGift ?: return@setOnClickListener
            val currentUser = audioViewModel.currentUserId()
            val target = targetMode.uppercase()
            if (target == "SELECTED" && selectedRecipients.isEmpty()) {
                selectedText.text = "Select recipients first"
                return@setOnClickListener
            }
            audioViewModel.sendGift(
                amount = gift.costCoins,
                recipientId = if (target == "SELF") currentUser else null,
                giftId = gift.id,
                giftType = gift.giftType,
                target = target,
                recipientIds = when (target) {
                    "ALL" -> roomRecipientIds
                    "SELECTED" -> selectedRecipients.toList()
                    else -> null
                }
            )
        }

        audioViewModel.loadGiftCatalog()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                val derivedCategories = state.giftCatalog.map {
                    it.category ?: if (it.giftType.contains("MULTI", true)) "Multiplier" else "Unique"
                }.distinct()
                updateCategoryChips(derivedCategories.ifEmpty { listOf("Unique", "Multiplier") })
                val activeCategory = selectedCategory ?: derivedCategories.firstOrNull()
                val giftItems = state.giftCatalog
                    .map { item ->
                        val categoryName = item.category ?: if (item.giftType.contains("MULTI", true)) "Multiplier" else "Unique"
                        item to categoryName
                    }
                    .filter { activeCategory == null || it.second.equals(activeCategory, ignoreCase = true) }
                    .map { (item, categoryName) ->
                        GiftItem(
                            id = item.id,
                            name = item.name,
                            price = item.costCoins,
                            conversionRate = item.diamondPercent / 100.0,
                            category = if (categoryName.equals("Multiplier", ignoreCase = true)) {
                                GiftCategory.MULTIPLIER
                            } else {
                                GiftCategory.UNIQUE
                            },
                            giftType = item.giftType
                        )
                    }
                giftAdapter.submitList(giftItems)

                val recipients = state.seats
                    .filter { it.userId != null && it.username != null }
                    .map { GiftRecipientItem(it.userId!!, it.username!!, it.seatNumber) }
                roomRecipientIds = recipients.map { it.userId }
                recipientAdapter.submitList(recipients)
                recipientAdapter.setSelected(selectedRecipients)
                }
            }
        }
    }
}

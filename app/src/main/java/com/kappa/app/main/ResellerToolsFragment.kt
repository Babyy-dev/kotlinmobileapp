package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kappa.app.R
import com.kappa.app.reseller.presentation.ResellerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResellerToolsFragment : Fragment() {

    private val viewModel: ResellerViewModel by viewModels()

    private var pendingProofMeta: ProofMeta? = null

    private val proofPicker = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val meta = pendingProofMeta
        if (uri != null && meta != null) {
            viewModel.addPaymentProof(uri.toString(), meta.amount, meta.date, meta.beneficiary, meta.note)
        }
        pendingProofMeta = null
    }

    private data class ProofMeta(
        val amount: Long,
        val date: String,
        val beneficiary: String,
        val note: String?
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reseller_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipientInput = view.findViewById<TextInputEditText>(R.id.input_reseller_recipient)
        val amountInput = view.findViewById<TextInputEditText>(R.id.input_reseller_amount)
        val messageText = view.findViewById<TextView>(R.id.text_reseller_message)
        val sendButton = view.findViewById<MaterialButton>(R.id.button_reseller_send)
        val historyButton = view.findViewById<MaterialButton>(R.id.button_reseller_history)
        val sellersRecycler = view.findViewById<RecyclerView>(R.id.recycler_reseller_sellers)
        val limitsRecycler = view.findViewById<RecyclerView>(R.id.recycler_reseller_limits)
        val salesRecycler = view.findViewById<RecyclerView>(R.id.recycler_reseller_sales)
        val proofsRecycler = view.findViewById<RecyclerView>(R.id.recycler_reseller_proofs)
        val addSellerButton = view.findViewById<MaterialButton>(R.id.button_reseller_add_seller)
        val addSaleButton = view.findViewById<MaterialButton>(R.id.button_reseller_add_sale)
        val addProofButton = view.findViewById<MaterialButton>(R.id.button_reseller_add_proof)
        val limitSellerInput = view.findViewById<TextInputEditText>(R.id.input_reseller_limit_seller)
        val limitTotalInput = view.findViewById<TextInputEditText>(R.id.input_reseller_limit_total)
        val limitDailyInput = view.findViewById<TextInputEditText>(R.id.input_reseller_limit_daily)
        val setLimitsButton = view.findViewById<MaterialButton>(R.id.button_reseller_set_limits)
        val saleIdInput = view.findViewById<TextInputEditText>(R.id.input_reseller_sale_id)
        val saleSellerInput = view.findViewById<TextInputEditText>(R.id.input_reseller_sale_seller)
        val saleBuyerInput = view.findViewById<TextInputEditText>(R.id.input_reseller_sale_buyer)
        val saleAmountInput = view.findViewById<TextInputEditText>(R.id.input_reseller_sale_amount)
        val saleCurrencyInput = view.findViewById<TextInputEditText>(R.id.input_reseller_sale_currency)
        val saleDestinationInput = view.findViewById<TextInputEditText>(R.id.input_reseller_sale_destination)
        val proofAmountInput = view.findViewById<TextInputEditText>(R.id.input_reseller_proof_amount)
        val proofDateInput = view.findViewById<TextInputEditText>(R.id.input_reseller_proof_date)
        val proofBeneficiaryInput = view.findViewById<TextInputEditText>(R.id.input_reseller_proof_beneficiary)
        val proofNoteInput = view.findViewById<TextInputEditText>(R.id.input_reseller_proof_note)

        val sellerAdapter = SimpleRowAdapter()
        val limitsAdapter = SimpleRowAdapter()
        val salesAdapter = SimpleRowAdapter()
        val proofsAdapter = SimpleRowAdapter()
        sellersRecycler.adapter = sellerAdapter
        limitsRecycler.adapter = limitsAdapter
        salesRecycler.adapter = salesAdapter
        proofsRecycler.adapter = proofsAdapter

        sendButton.setOnClickListener {
            val recipient = recipientInput.text?.toString()?.trim().orEmpty()
            val amount = amountInput.text?.toString()?.trim()?.toLongOrNull()
            if (recipient.isBlank()) {
                messageText.text = "Recipient is required"
                messageText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (amount == null || amount <= 0) {
                messageText.text = "Enter a valid amount"
                messageText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            messageText.visibility = View.GONE
            viewModel.sendCoins(recipient, amount)
        }

        addSellerButton.setOnClickListener {
            val sellerId = limitSellerInput.text?.toString()?.trim().orEmpty()
            if (sellerId.isBlank()) {
                messageText.text = "Enter seller id or username"
                messageText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            viewModel.addSeller(sellerId)
        }

        setLimitsButton.setOnClickListener {
            val sellerId = limitSellerInput.text?.toString()?.trim().orEmpty()
            val total = limitTotalInput.text?.toString()?.trim()?.toLongOrNull()
            val daily = limitDailyInput.text?.toString()?.trim()?.toLongOrNull()
            if (sellerId.isBlank() || total == null || daily == null) {
                messageText.text = "Enter seller and limits"
                messageText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            viewModel.setSellerLimits(sellerId, total, daily)
        }

        addSaleButton.setOnClickListener {
            val saleId = saleIdInput.text?.toString()?.trim().orEmpty()
            val sellerId = saleSellerInput.text?.toString()?.trim().orEmpty()
            val buyerId = saleBuyerInput.text?.toString()?.trim().orEmpty()
            val amount = saleAmountInput.text?.toString()?.trim()?.toLongOrNull()
            val currency = saleCurrencyInput.text?.toString()?.trim().orEmpty()
            val destination = saleDestinationInput.text?.toString()?.trim().orEmpty()
            if (saleId.isBlank() || sellerId.isBlank() || buyerId.isBlank() ||
                amount == null || currency.isBlank() || destination.isBlank()
            ) {
                messageText.text = "Fill all sale fields"
                messageText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            viewModel.addSale(saleId, sellerId, buyerId, amount, currency, destination)
        }

        addProofButton.setOnClickListener {
            val amount = proofAmountInput.text?.toString()?.trim()?.toLongOrNull()
            val date = proofDateInput.text?.toString()?.trim().orEmpty()
            val beneficiary = proofBeneficiaryInput.text?.toString()?.trim().orEmpty()
            val note = proofNoteInput.text?.toString()?.trim().orEmpty().ifBlank { null }
            if (amount == null || date.isBlank() || beneficiary.isBlank()) {
                messageText.text = "Fill proof amount, date, and beneficiary"
                messageText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            pendingProofMeta = ProofMeta(amount, date, beneficiary, note)
            proofPicker.launch("*/*")
        }

        historyButton.setOnClickListener {
            Toast.makeText(requireContext(), "History not available yet", Toast.LENGTH_SHORT).show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    sellerAdapter.submitRows(state.sellers.map { "Seller: ${it.sellerId}" to "Status: Active" })
                    limitsAdapter.submitRows(
                        state.limits.map { it.sellerId to "Total ${it.totalLimit} - Daily ${it.dailyLimit}" }
                    )
                    salesAdapter.submitRows(
                        state.sales.map {
                            "${it.saleId} - ${it.currency} ${it.amount}" to
                                "Seller ${it.sellerId} - Buyer ${it.buyerId} - Dest ${it.destinationAccount}"
                        }
                    )
                    proofsAdapter.submitRows(
                        state.proofs.map { it.beneficiary to "${it.amount} on ${it.date}" }
                    )
                    if (state.message != null) {
                        messageText.text = state.message
                        messageText.visibility = View.VISIBLE
                    } else {
                        messageText.visibility = View.GONE
                    }
                }
            }
        }
    }
}


package com.example.myapplicationx.ui.accounts.receipts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplicationx.databinding.FragmentReceiptDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptDetailFragment : Fragment() {

    private var _binding: FragmentReceiptDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: InvoicePaidAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the Add Invoice button as the receipt is read-only
        //binding.btnAddInvoice.visibility = View.GONE
        // Also hide the Save button if present
        //binding.btnSaveReceipt.visibility = View.GONE

        // Set up the RecyclerView adapter with delete buttons hidden
        adapter = InvoicePaidAdapter(
            onItemClick = { /* Optionally handle item click */ },
            onDeleteClick = { /* No delete action in detail view */ },
            showDeleteButton = false
        )
        binding.rvInvoicesPaid.adapter = adapter

        // Load the receipt details and invoice list to populate the UI here
        // For example: adapter.submitList(receiptDetails.invoicesPaid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
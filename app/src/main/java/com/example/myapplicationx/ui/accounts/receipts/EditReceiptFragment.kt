package com.example.myapplicationx.ui.accounts.receipts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.databinding.FragmentEditReceiptBinding
import com.example.myapplicationx.database.model.ReceiptEntity
import com.example.myapplicationx.database.model.ReceiptInvoice
import com.example.myapplicationx.database.model.ReceiptInvoiceCrossRef
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint 
class EditReceiptFragment : Fragment() { 
    private var _binding: FragmentEditReceiptBinding? = null 
    private val binding get() = _binding!! 
    private val viewModel: ReceiptsViewModel by viewModels() 
    private val args: EditReceiptFragmentArgs by navArgs()

    private val addedInvoices = mutableListOf<ReceiptInvoice>()
    private var originalReceipt: ReceiptEntity? = null
    private lateinit var adapter: InvoicePaidAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupEditMode()
        setupSaveButton()
    }

    private fun setupUI() {
        adapter = InvoicePaidAdapter(
            onDeleteClick = { invoice ->
                addedInvoices.remove(invoice)
                adapter.submitList(addedInvoices.toList())
            },
            onItemClick = { /* no-op */ },
            showDeleteButton = true
        )
        binding.recyclerViewInvoicesPaid.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewInvoicesPaid.adapter = adapter
    }

    private fun setupEditMode() {
        viewModel.getReceiptById(args.receiptId).observe(viewLifecycleOwner) { receipt ->
            receipt?.let {
                originalReceipt = it
                binding.etReceiptNumber.setText(it.receiptNumber)
                binding.etAmount.setText(it.amountReceived.toString())
                binding.etTenantId.setText(it.tenantId.toString())
                binding.etTenantName.setText(it.tenantName)
                binding.etReceiptDate.setText(it.receiptDate)
                binding.etReceiptNote.setText(it.receiptNote ?: "")

                lifecycleScope.launch {
                    val crossRefs = viewModel.getReceiptInvoiceCrossRefs(it.receiptNumber)
                    val paidInvoices = crossRefs.mapNotNull { crossRef ->
                        viewModel.getInvoiceDetails(crossRef.invoiceNumber)?.let { invoice ->
                            ReceiptInvoice(
                                invoiceNumber = crossRef.invoiceNumber,
                                amountPaid = crossRef.amountPaid,
                                remainingBalance = invoice.invoiceAmountDue ?: 0.0,
                                invoiceDate = invoice.invoiceDate,
                                invoiceStatus = invoice.status
                            )
                        }
                    }
                    
                    addedInvoices.clear()
                    addedInvoices.addAll(paidInvoices)
                    adapter.submitList(addedInvoices.toList())
                    viewModel.loadUnpaidInvoicesForTenant(it.tenantId)
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnUpdateReceipt.setOnClickListener {
            lifecycleScope.launch {
                validateAndUpdateReceipt()
            }
        }
    }

    private suspend fun validateAndUpdateReceipt() {
        val receipt = originalReceipt?.copy(
            receiptNumber = binding.etReceiptNumber.text.toString(),
            amountReceived = binding.etAmount.text.toString().toDouble(),
            receiptDate = binding.etReceiptDate.text.toString(),
            receiptNote = binding.etReceiptNote.text.toString()
        ) ?: return

        val updatedRefs = addedInvoices.map { invoice ->
            ReceiptInvoiceCrossRef(
                receiptNumber = receipt.receiptNumber,
                invoiceNumber = invoice.invoiceNumber,
                amountPaid = invoice.amountPaid,
                paymentDate = invoice.invoiceDate
            )
        }

        try {
            withContext(Dispatchers.IO) {
                viewModel.updateReceiptWithInvoices(receipt, updatedRefs)
            }
            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "Receipt updated", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "Update failed: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
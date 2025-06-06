package com.example.myapplicationx.ui.accounts.receipts

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.ReceiptEntity
import com.example.myapplicationx.database.model.ReceiptInvoice
import com.example.myapplicationx.database.model.ReceiptInvoiceCrossRef
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.databinding.FragmentAddReceiptBinding
import com.example.myapplicationx.ui.tenants.TenantsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AddReceiptFragment : Fragment(R.layout.fragment_add_receipt) {

    private var _binding: FragmentAddReceiptBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReceiptsViewModel by viewModels()
    private val tenantsViewModel: TenantsViewModel by viewModels()

    private lateinit var adapter: InvoiceListAdapter
    private val selectedInvoices = mutableListOf<ReceiptInvoice>()
    private var originalCreditBalance = 0.0
    private var selectedTenantId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddReceiptBinding.bind(view)

        setupRecyclerView()
        setupTenantSelector()
        setupDatePicker()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = InvoiceListAdapter { invoice ->
            invoice.isSelected = !invoice.isSelected
            if (invoice.isSelected) selectedInvoices.add(invoice)
            else selectedInvoices.remove(invoice)
            updateUI()
        }
        binding.invoiceRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AddReceiptFragment.adapter
        }
    }

    private fun setupTenantSelector() {
        // Observe tenant list from the dedicated TenantsViewModel and attach the click listener
        tenantsViewModel.tenantsL.observe(viewLifecycleOwner) { tenants ->
            binding.tenantSelectorLayout.editText?.setOnClickListener {
                showTenantSelectionDialog(tenants)
            }
        }
    }

    private fun showTenantSelectionDialog(tenants: List<TenantEntity>) {
        val tenantNames = tenants.map { it.tenantName }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Select Tenant")
            .setItems(tenantNames) { _, position ->
                val tenant = tenants[position]
                binding.tenantSelectorEditText.setText(tenant.tenantName)
                selectedTenantId = tenant.tenantId
                // load tenant credit balance and their unpaid invoices
                loadTenantCreditBalance(tenant.tenantId)
                viewModel.loadUnpaidInvoicesForTenant(tenant.tenantId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupDatePicker() {
        binding.etReceiptDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    binding.etReceiptDate.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            if (hasSufficientCredit()) saveReceipt() else showCreditError()
        }
    }

    private fun loadTenantCreditBalance(tenantId: Int) {
        lifecycleScope.launch {
            viewModel.getTenantById(tenantId)?.let { tenant ->
                originalCreditBalance = tenant.creditBalance
                updateCreditDisplay()
            }
        }
    }

    private fun observeData() {
        viewModel.unpaidInvoices.observe(viewLifecycleOwner) { invoices ->
            adapter.submitList(invoices.map { invoice ->
                ReceiptInvoice(
                    invoiceNumber = invoice.invoiceNumber,
                    amountPaid = invoice.invoiceAmount - (invoice.invoiceAmountDue ?: 0.0),
                    remainingBalance = invoice.invoiceAmountDue ?: 0.0,
                    invoiceDate = invoice.invoiceDate,
                    invoiceStatus = invoice.status
                )
            })
        }
    }

    private fun updateUI() {
        val totalPaid = selectedInvoices.sumOf { it.amountPaid }
        binding.etPaymentAmount.setText("%.2f".format(totalPaid))
        updateCreditDisplay()
        validateForm()
    }

    private fun updateCreditDisplay() {
        val remainingCredit = originalCreditBalance - selectedInvoices.sumOf { it.amountPaid }
        binding.tvCreditBalance.text = "Available Credit: ${"%.2f".format(remainingCredit)}"
    }

    private fun hasSufficientCredit(): Boolean {
        return selectedInvoices.sumOf { it.amountPaid } <= originalCreditBalance
    }

    private fun showCreditError() {
        Snackbar.make(binding.root, "Insufficient credit!", Snackbar.LENGTH_SHORT).show()
    }

    private fun validateForm() {
        binding.btnSave.isEnabled = selectedTenantId != null &&
                selectedInvoices.isNotEmpty() &&
                binding.etReceiptNumber.text?.isNotBlank() == true &&
                binding.etReceiptDate.text?.isNotBlank() == true
    }

    private fun saveReceipt() {
        val receipt = ReceiptEntity(
            receiptNumber = binding.etReceiptNumber.text.toString(),
            tenantId = selectedTenantId!!,
            tenantName = binding.tenantSelectorEditText.text.toString(),
            amountReceived = selectedInvoices.sumOf { it.amountPaid },
            receiptDate = binding.etReceiptDate.text.toString(),
            receiptNote = binding.etReceiptNote.text.toString(),
            isAutoGenerated = false
        )

        val crossRefs = selectedInvoices.map { invoice ->
            ReceiptInvoiceCrossRef(
                receiptNumber = receipt.receiptNumber,
                invoiceNumber = invoice.invoiceNumber,
                amountPaid = invoice.amountPaid,
                paymentDate = receipt.receiptDate
            )
        }

        lifecycleScope.launch {
            viewModel.insertReceiptWithInvoices(receipt, crossRefs)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
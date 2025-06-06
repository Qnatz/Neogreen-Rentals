package com.example.myapplicationx.ui.accounts.receivables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplicationx.databinding.FragmentAddReceivableBinding
import com.example.myapplicationx.ui.accounts.receivables.ReceivableViewModel
import com.example.myapplicationx.database.model.ReceivableEntity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class AddReceivableFragment : Fragment() {

    private var _binding: FragmentAddReceivableBinding? = null
    private val binding get() = _binding!!

    private val receivableViewModel: ReceivableViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReceivableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe available invoice numbers
        receivableViewModel.availableInvoiceNumbers.observe(viewLifecycleOwner) { invoiceNumbers ->
            // Populate the invoice number dropdown
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, invoiceNumbers)
            binding.invoiceNumberEditText.setAdapter(adapter)
        }

        // Fetch available invoice numbers when fragment is ready
        receivableViewModel.fetchAvailableInvoiceNumbers()

        // Handle selection of an invoice number
        binding.invoiceNumberEditText.setOnItemClickListener { parent, view, position, id ->
            val selectedInvoiceNumber = parent.getItemAtPosition(position) as String
            // Fetch details for the selected invoice
            receivableViewModel.observeInvoice(selectedInvoiceNumber)
        }

        // Observe selected invoice and populate other fields
        receivableViewModel.invoice.observe(viewLifecycleOwner) { invoiceEntity ->
            // Populate fields based on selected invoice
            invoiceEntity?.let {
                binding.invoiceNumberEditText.setText(it.invoiceNumber.toString())
                binding.tenantNameEditText.setText(it.tenantName.toString())
                binding.amountDueEditText.setText(it.invoiceAmountDue.toString())
                binding.dateReceivableEditText.setText(it.receivableDate.toString())
            }
        }

        binding.btnSave.setOnClickListener {
            val invoiceNumber = binding.invoiceNumberEditText.text.toString()
            val amountDueStr = binding.amountDueEditText.text.toString()
            val dateReceivableStr = binding.dateReceivableEditText.text.toString()
            val tenantName = binding.tenantNameEditText.text.toString()

            if (listOf(invoiceNumber, amountDueStr, dateReceivableStr, tenantName).any { it.isEmpty() }) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dateReceivable = dateFormat.parse(dateReceivableStr)?.time ?: 0L
                    val amountDue = amountDueStr.toDouble()

                    val receivable = ReceivableEntity(
                        invoiceNumber = invoiceNumber,
                        tenantName = tenantName,  
                        amountDue = amountDue,
                        dateReceivable = dateReceivable
                    )

                    // Insert the receivable via ViewModel
                    receivableViewModel.insertReceivable(receivable)
                    Toast.makeText(requireContext(), "Receivable Saved: ${receivable.invoiceNumber}", Toast.LENGTH_LONG).show()

                    findNavController().navigateUp() // Navigate back after saving
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Invalid input format", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
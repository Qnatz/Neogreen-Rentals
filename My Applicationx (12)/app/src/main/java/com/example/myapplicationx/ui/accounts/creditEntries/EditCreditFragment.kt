package com.example.myapplicationx.ui.accounts.creditEntries

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.databinding.FragmentAddCreditBinding
import com.example.myapplicationx.database.model.CreditEntryEntity
import com.example.myapplicationx.ui.accounts.receipts.ReceiptsViewModel
import com.example.myapplicationx.ui.tenants.TenantsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class EditCreditFragment : Fragment() {
    private var _binding: FragmentAddCreditBinding? = null
    private val binding get() = _binding!!
    
    private val creditViewModel: CreditViewModel by viewModels()
    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val receiptsViewModel: ReceiptsViewModel by viewModels()
    
    private val args: EditCreditFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCreditBinding.inflate(inflater, container, false)
        
        // Get the credit entry from navigation arguments
        val creditEntry = args.creditEntry
        
        // Populate form with existing data
        with(binding) {
            tenantIdInput.setText(creditEntry.tenantId.toString())
            tenantNameInput.setText(creditEntry.tenantName)
            creditDateInput.setText(creditEntry.creditDate)
            amountInput.setText(creditEntry.amount.toString())
            creditNoteInput.setText(creditEntry.creditNote)
            
            // Make tenant fields non-editable
            tenantIdInput.isEnabled = false
            tenantNameInput.isEnabled = false
        }

        setupSaveButton(creditEntry)
        
        return binding.root
    }

    private fun setupSaveButton(originalEntry: CreditEntryEntity) {
        binding.saveButton.setOnClickListener {
            val amount = binding.amountInput.text.toString().toDoubleOrNull()
            val creditNote = binding.creditNoteInput.text.toString()
            val creditDate = binding.creditDateInput.text.toString()

            if (amount != null && creditDate.isNotEmpty()) {
                lifecycleScope.launch {
                    // Create updated credit entry
                    val updatedEntry = originalEntry.copy(
                        amount = amount,
                        creditNote = creditNote,
                        creditDate = creditDate
                    )

                    // Update in database
                    creditViewModel.updateCreditEntry(updatedEntry)

                    // Calculate balance adjustment
                    val amountDifference = amount - originalEntry.amount
                    if (amountDifference != 0.0) {
                        receiptsViewModel.createReceipt(
                            tenantId = updatedEntry.tenantId,
                            tenantName = updatedEntry.tenantName,
                            amount = amountDifference,
                            isAuto = true,
                            creditNote = "Credit Adjustment: $creditNote"
                        )
                    }

                    // Show confirmation dialog
                    AlertDialog.Builder(requireContext())
                        .setTitle("Update Successful")
                        .setMessage("Credit entry for ${updatedEntry.tenantName} updated")
                        .setPositiveButton("OK") { _, _ ->
                            findNavController().navigateUp()
                        }
                        .show()
                }
            } else {
                // Show error for invalid input
                AlertDialog.Builder(requireContext())
                    .setTitle("Invalid Input")
                    .setMessage("Please fill all required fields correctly")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
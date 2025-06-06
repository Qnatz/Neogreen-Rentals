package com.example.myapplicationx.ui.accounts.creditEntries

import android.app.DatePickerDialog 
import android.os.Bundle 
import android.view.LayoutInflater 
import android.view.View 
import android.view.ViewGroup 
import android.widget.DatePicker 
import androidx.appcompat.app.AlertDialog 
import androidx.fragment.app.Fragment 
import androidx.fragment.app.viewModels 
import androidx.lifecycle.lifecycleScope 
import androidx.navigation.fragment.findNavController 
import com.example.myapplicationx.R 
import com.example.myapplicationx.databinding.FragmentAddCreditBinding 
import com.example.myapplicationx.database.model.CreditEntryEntity 
import com.example.myapplicationx.database.model.TenantEntity 
import com.example.myapplicationx.ui.accounts.receipts.ReceiptsViewModel 
import com.example.myapplicationx.ui.tenants.TenantsViewModel 
import dagger.hilt.android.AndroidEntryPoint 
import kotlinx.coroutines.Dispatchers 
import kotlinx.coroutines.launch 
import kotlinx.coroutines.withContext 
import java.text.SimpleDateFormat 
import java.util.Calendar 
import java.util.Date 
import java.util.Locale

@AndroidEntryPoint 
class AddCreditFragment : Fragment() {

private var _binding: FragmentAddCreditBinding? = null
private val binding get() = _binding!!

private val creditViewModel: CreditViewModel by viewModels()
private val tenantsViewModel: TenantsViewModel by viewModels()
private val receiptsViewModel: ReceiptsViewModel by viewModels()

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    _binding = FragmentAddCreditBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUI()
    setupTenantSelection()
    setupDatePicker()
    setupSaveButton()
}

private fun setupUI() {
    // Disable direct editing on generated fields
    binding.creditEntryNumberInput.isEnabled = false
    binding.tenantNameInput.isFocusable = false
    binding.creditDateInput.isFocusable = false

    // Pre-fill current date
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    binding.creditDateInput.setText(today)
    
    // Setup receipt creation switch
    binding.receiptCreationSwitch.setOnCheckedChangeListener { _, isChecked ->
        updateReceiptCreationDescription(isChecked)
    }
    
    // Set initial description
    updateReceiptCreationDescription(binding.receiptCreationSwitch.isChecked)
}

private fun updateReceiptCreationDescription(isAutomatic: Boolean) {
    val descriptionText = if (isAutomatic) {
        "Automatic: Receipt will be generated immediately when credit is saved."
    } else {
        "Manual: You will need to create receipts separately through the Receipts section."
    }
    binding.receiptCreationDescription.text = descriptionText
}

private fun setupTenantSelection() {
    tenantsViewModel.tenantsL.observe(viewLifecycleOwner) { tenants ->
        binding.tenantNameInput.setOnClickListener {
            if (tenants.isNotEmpty()) {
                val names = tenants.map(TenantEntity::tenantName).toTypedArray()
                AlertDialog.Builder(requireContext())
                    .setTitle("Select Tenant")
                    .setItems(names) { _, pos ->
                        val tenant = tenants[pos]
                        binding.tenantNameInput.setText(tenant.tenantName)
                        binding.tenantIdInput.setText(tenant.tenantId.toString())
                        // Generate a new entry number
                        binding.creditEntryNumberInput.setText(generateCreditEntryNumber(tenant.tenantName))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}

private fun setupDatePicker() {
    binding.creditDateInput.setOnClickListener {
        val cal = Calendar.getInstance()
        val currentText = binding.creditDateInput.text.toString()
        if (currentText.isNotBlank()) {
            runCatching {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentText)
            }.getOrNull()?.let { cal.time = it }
        }
        DatePickerDialog(
            requireContext(),
            { _: DatePicker, y, m, d ->
                binding.creditDateInput.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

private fun setupSaveButton() {
    binding.saveButton.setOnClickListener {
        val tenantId = binding.tenantIdInput.text.toString().toIntOrNull()
        val tenantName = binding.tenantNameInput.text.toString().trim()
        val creditDate = binding.creditDateInput.text.toString().trim()
        val entryNumber = binding.creditEntryNumberInput.text.toString().trim()
        val amount = binding.amountInput.text.toString().toDoubleOrNull()
        val note = binding.creditNoteInput.text.toString().trim()

        when {
            tenantId == null || tenantName.isEmpty() -> showError("Please select a tenant.")
            amount == null || amount <= 0.0          -> showError("Please enter a valid amount.")
            creditDate.isEmpty()                    -> showError("Please select a date.")
            entryNumber.isEmpty()                   -> showError("Entry number generation failed.")
            else -> saveCreditEntry(tenantId, tenantName, entryNumber, creditDate, amount, note)
        }
    }
}

private fun saveCreditEntry(
    tenantId: Int,
    tenantName: String,
    entryNumber: String,
    creditDate: String,
    amount: Double,
    note: String
) {
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            // 1. Insert credit entry
            val entry = CreditEntryEntity(
                creditEntryId     = 0,
                creditEntryNumber = entryNumber,
                tenantId          = tenantId,
                tenantName        = tenantName,
                creditDate        = creditDate,
                amount            = amount,
                creditNote        = note
            )
            creditViewModel.addCreditEntry(entry)

            // 2. Update tenant balance
            tenantsViewModel.updateCreditBalance(tenantId, amount)

            // 3. Optionally create receipt based on switch selection
            if (binding.receiptCreationSwitch.isChecked) {
                receiptsViewModel.createReceipt(
                    tenantId   = tenantId,
                    tenantName = tenantName,
                    amount     = amount,
                    isAuto     = true,
                    creditNote = "Credit Entry: $note"
                )
            }

            // 4. Success feedback
            withContext(Dispatchers.Main) {
                showSuccess("Credit Applied", "$$amount credit added to $tenantName's account.")
            }
        } catch (ex: Exception) {
            withContext(Dispatchers.Main) {
                showError("Error saving credit entry: ${ex.message}")
            }
        }
    }
}

private fun showError(message: String) {
    AlertDialog.Builder(requireContext())
        .setTitle("Error")
        .setMessage(message)
        .setPositiveButton("OK", null)
        .show()
}

private fun showSuccess(title: String, message: String) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK") { _, _ -> findNavController().navigateUp() }
        .show()
}

private fun generateCreditEntryNumber(tenantName: String): String {
    val initials  = tenantName
        .split(Regex("\\s+"))
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString(separator = "").take(3)
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "CR-$initials-$timeStamp"
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}

}


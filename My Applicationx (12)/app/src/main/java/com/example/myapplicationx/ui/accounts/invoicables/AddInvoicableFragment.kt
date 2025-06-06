package com.example.myapplicationx.ui.accounts.invoicables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.InvoicableEntity
import com.example.myapplicationx.ui.accounts.invoicables.InvoicablesViewModel
import com.example.myapplicationx.databinding.FragmentAddInvoicableBinding
import java.time.LocalDate

class AddInvoicableFragment : Fragment() {

    private var _binding: FragmentAddInvoicableBinding? = null
    private val binding get() = _binding!!
    private val invoicablesViewModel: InvoicablesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddInvoicableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle save button click
        binding.saveInvoicableButton.setOnClickListener {
            // Get the data from the inputs
            val tenantId = binding.tenantIdInput.text.toString().toIntOrNull()
            val tenantName = binding.tenantNameInput.text.toString()
            val nextBillingDateStr = binding.nextBillingDateInput.text.toString().trim()
            val status = binding.statusSpinner.selectedItem.toString()

            // Validate input
            if (tenantId != null && tenantName.isNotEmpty() && nextBillingDateStr.isNotEmpty()) {
                // Convert the nextBillingDate to LocalDate and then to Long (epoch day)
                val nextBillingDate = LocalDate.parse(nextBillingDateStr)
                val nextBillingDateLong = nextBillingDate.toEpochDay()

                // Create the InvoicableEntity object with the required data
                val invoicable = InvoicableEntity(
                    tenantId = tenantId,
                    tenantName = tenantName,
                    nextBillingDate = nextBillingDateLong,  // Use Long here
                    status = status
                )

                // Insert the invoicable entry using ViewModel
                invoicablesViewModel.addInvoicable(invoicable)

                // Optionally, navigate back or show a message to the user
                Toast.makeText(context, "Invoicable added successfully", Toast.LENGTH_SHORT).show()

                // Navigate back or update UI as needed (e.g., pop the fragment or refresh list)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                // Handle invalid input (e.g., show a toast or message)
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
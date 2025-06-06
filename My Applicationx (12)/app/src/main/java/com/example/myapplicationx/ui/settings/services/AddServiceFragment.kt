package com.example.myapplicationx.ui.settings.services

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.database.model.ServiceEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.ui.settings.taxes.TaxesViewModel
import com.example.myapplicationx.databinding.FragmentAddServiceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddServiceFragment : Fragment() {

    private var _binding: FragmentAddServiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ServicesViewModel by viewModels()
    private val taxesViewModel: TaxesViewModel by viewModels()

    // List of tax associations for the service
    private val selectedTaxes = mutableListOf<ServiceTaxCrossRef>()
    private lateinit var taxesAdapter: SelectedTaxesAdapter
    private lateinit var availableTaxes: List<TaxEntity>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupServiceNameSuggestions()
        setupSwitchListeners()
        setupTaxesRecyclerView()
        setupAddTaxesButton()
        setupSaveButton()
        fetchAvailableTaxes()
    }

    private fun setupServiceNameSuggestions() {
        val serviceNames = listOf("Rent", "Water", "Electricity", "Internet", "Maintenance", "Garbage Collection")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, serviceNames)
        binding.autoCompleteServiceName.setAdapter(adapter)
        binding.autoCompleteServiceName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.autoCompleteServiceName.showDropDown()
        }
    }

    private fun setupSwitchListeners() {
        binding.meteredSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.unitPriceLayout.visibility = View.VISIBLE
                binding.fixedPriceSwitch.visibility = View.GONE
                binding.fixedPriceLayout.visibility = View.GONE
            } else {
                binding.unitPriceLayout.visibility = View.GONE
                binding.fixedPriceSwitch.visibility = View.VISIBLE
            }
        }

        binding.fixedPriceSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.fixedPriceLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    // Adjust adapter initialization to separate deletion and inclusive toggle actions.
    private fun setupTaxesRecyclerView() {
        taxesAdapter = SelectedTaxesAdapter(
            selectedTaxes,
            onDelete = { position ->
                // Remove the tax at the given position
                selectedTaxes.removeAt(position)
                binding.rvSelectedTaxes.post {
                    taxesAdapter.notifyItemRemoved(position)
                }
            },
            onInclusiveChanged = { position, isInclusive, overrideValue ->
                selectedTaxes[position] = selectedTaxes[position].copy(
                    isInclusive = isInclusive,
                    taxPercentage = overrideValue
                )
                binding.rvSelectedTaxes.post {
                    taxesAdapter.notifyItemChanged(position)
                }
            }
        )
        binding.rvSelectedTaxes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectedTaxes.adapter = taxesAdapter
    }

    private fun setupAddTaxesButton() {
        binding.addTaxesButton.setOnClickListener {
            showAddTaxDialog()
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener { saveService() }
    }

    private fun fetchAvailableTaxes() {
        taxesViewModel.taxes.asLiveData().observe(viewLifecycleOwner) { taxes ->
            availableTaxes = taxes
        }
    }

    // In your Service creation/editing logic (e.g., AddServiceFragment)
private fun showAddTaxDialog() {
    AlertDialog.Builder(requireContext()).apply {
        setTitle("Select a Tax")
        setItems(availableTaxes.map { it.taxName }.toTypedArray()) { _, which ->
            val selectedTax = availableTaxes[which]
            
            // Check for existing tax with same name (case-insensitive)
            if (selectedTaxes.any { 
                it.taxName.equals(selectedTax.taxName, ignoreCase = true) 
            }) {
                Toast.makeText(
                    requireContext(),
                    "${selectedTax.taxName} already added",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val taxCrossRef = ServiceTaxCrossRef(
                    serviceId = 0,
                    taxId = selectedTax.taxId,
                    taxPercentage = selectedTax.taxPercentage ?: 0.0,
                    isInclusive = false,
                    taxName = selectedTax.taxName
                )
                selectedTaxes.add(taxCrossRef)
                taxesAdapter.notifyItemInserted(selectedTaxes.size - 1)
            }
        }
        setNegativeButton("Cancel", null)
    }.show()
}

    private fun saveService() {
        val serviceName = binding.autoCompleteServiceName.text.toString().trim()
        val isMetered = binding.meteredSwitch.isChecked
        val isFixedPrice = binding.fixedPriceSwitch.isChecked

        val unitPrice = binding.unitPriceEditText.text.toString().toDoubleOrNull()
        val fixedPrice = binding.fixedPriceEditText.text.toString().toDoubleOrNull()

        if (serviceName.isEmpty()) {
            Toast.makeText(requireContext(), "Service name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (isMetered && unitPrice == null) {
            Toast.makeText(requireContext(), "Unit price must be provided for metered services", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isMetered && isFixedPrice && fixedPrice == null) {
            Toast.makeText(requireContext(), "Fixed price must be provided for fixed-price services", Toast.LENGTH_SHORT).show()
            return
        }

        val service = ServiceEntity(
            serviceName = serviceName,
            isMetered = isMetered,
            isFixedPrice = isFixedPrice,
            unitPrice = if (isMetered) unitPrice else null,
            fixedPrice = if (!isMetered && isFixedPrice) fixedPrice else null
        )

        // Debug: Show what will be saved
        showDebugDialog("Debug: Saving Service", "Service: $service\nTaxes: $selectedTaxes") {
            viewModel.insertServiceWithTaxes(service, selectedTaxes)
            Toast.makeText(requireContext(), "Service saved successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    // Helper: Show an AlertDialog with debug info. Optionally, execute an action on OK.
    private fun showDebugDialog(title: String, message: String, onPositive: (() -> Unit)? = null) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                onPositive?.invoke()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
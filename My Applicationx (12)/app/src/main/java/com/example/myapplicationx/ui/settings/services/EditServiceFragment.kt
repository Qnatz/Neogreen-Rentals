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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.database.model.ServiceEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.ui.settings.taxes.TaxesViewModel
import com.example.myapplicationx.databinding.FragmentAddServiceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditServiceFragment : Fragment() {

    // We reuse the AddService layout for consistency.
    private var _binding: FragmentAddServiceBinding? = null
    private val binding get() = _binding!!

    // ViewModels for services and taxes.
    private val viewModel: ServicesViewModel by viewModels()
    private val taxesViewModel: TaxesViewModel by viewModels()

    // Safe Args – expecting a serviceId to prepopulate data.
    private val args: EditServiceFragmentArgs by navArgs()

    // List of tax associations for the service.
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

        // Set up common UI functionality.
        setupServiceNameSuggestions()
        setupSwitchListeners()
        setupTaxesRecyclerView()
        setupAddTaxesButton()
        setupSaveButton()
        fetchAvailableTaxes()

        // Prepopulate data if editing an existing service.
        prepopulateServiceData()
    }

    /**
     * If args.serviceId is nonzero, load the existing ServiceEntity and its associated taxes.
     * After retrieval, show a debug dialog with the retrieved data.
     */
    private fun prepopulateServiceData() {
        val serviceId = args.serviceId
        if (serviceId != 0) {
            // Prepopulate service details.
            viewModel.getServiceById(serviceId).observe(viewLifecycleOwner) { service ->
                service?.let {
                    binding.autoCompleteServiceName.setText(it.serviceName)
                    binding.meteredSwitch.isChecked = it.isMetered
                    binding.fixedPriceSwitch.isChecked = it.isFixedPrice
                    binding.unitPriceEditText.setText(it.unitPrice?.toString() ?: "")
                    binding.fixedPriceEditText.setText(it.fixedPrice?.toString() ?: "")
                    
                    if (binding.meteredSwitch.isChecked) {
                        binding.unitPriceLayout.visibility = View.VISIBLE
                        binding.fixedPriceSwitch.visibility = View.GONE
                        binding.fixedPriceLayout.visibility = View.GONE
                    } else {
                        binding.unitPriceLayout.visibility = View.GONE
                        binding.fixedPriceSwitch.visibility = View.VISIBLE
                        binding.fixedPriceLayout.visibility =
                            if (binding.fixedPriceSwitch.isChecked) View.VISIBLE else View.GONE
                    }
                }
            }
            // Prepopulate tax associations using actual join data.
            viewModel.getServiceWithTaxCrossRefs(serviceId).observe(viewLifecycleOwner) { serviceWithTaxCrossRefs ->
                serviceWithTaxCrossRefs?.let {
                    selectedTaxes.clear()
                    selectedTaxes.addAll(it.taxCrossRefs)
                    taxesAdapter.notifyDataSetChanged()
                    // Show retrieved data in a debug dialog.
                    val debugMsg = "Retrieved Service:\n${it.service}\n\nRetrieved Taxes:\n${it.taxCrossRefs}"
                    showDebugDialog("Retrieved Data", debugMsg)
                }
            }
        }
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

    private fun setupTaxesRecyclerView() {
        taxesAdapter = SelectedTaxesAdapter(
            selectedTaxes,
            onDelete = { position ->
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

    private fun showAddTaxDialog() {
        if (!::availableTaxes.isInitialized || availableTaxes.isEmpty()) {
            Toast.makeText(requireContext(), "No taxes available", Toast.LENGTH_SHORT).show()
            return
        }
        val taxNames = availableTaxes.map { it.taxName }.toTypedArray()
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Select a Tax")
            setItems(taxNames) { _, which ->
                val selectedTaxEntity = availableTaxes[which]
                // Check if this tax is already added.
                if (selectedTaxes.any { it.taxId == selectedTaxEntity.taxId }) {
                    Toast.makeText(requireContext(), "${selectedTaxEntity.taxName} already added", Toast.LENGTH_SHORT).show()
                } else {
                    val taxCrossRef = ServiceTaxCrossRef(
                        serviceId = args.serviceId, // Use the current service id.
                        taxId = selectedTaxEntity.taxId,
                        taxPercentage = selectedTaxEntity.taxPercentage ?: 0.0,
                        isInclusive = false,
                        taxName = selectedTaxEntity.taxName
                    )
                    selectedTaxes.add(taxCrossRef)
                    taxesAdapter.notifyItemInserted(selectedTaxes.size - 1)
                    showDebugDialog("Debug: Tax Added", "Added: $taxCrossRef\nAll Taxes: $selectedTaxes")
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

        // Create the ServiceEntity to be saved.
        val newServiceEntity = ServiceEntity(
            serviceId = args.serviceId,
            serviceName = serviceName,
            isMetered = isMetered,
            isFixedPrice = isFixedPrice,
            unitPrice = unitPrice,
            fixedPrice = fixedPrice
        )

        // Prepare debug info for the service and associated taxes.
        val debugMsg = "Service to be saved:\n$newServiceEntity\n\nTaxes to be saved:\n$selectedTaxes"

        showDebugDialog("Stored Data", debugMsg) {
            if (args.serviceId == 0) {
                // Insert new service and its taxes.
                viewModel.insertServiceWithTaxes(newServiceEntity, selectedTaxes)
            } else {
                // Update existing service and its taxes.
                viewModel.updateServiceWithTaxes(newServiceEntity, selectedTaxes)
            }
            Toast.makeText(requireContext(), "Service saved successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    // Helper method to show a debug dialog.
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
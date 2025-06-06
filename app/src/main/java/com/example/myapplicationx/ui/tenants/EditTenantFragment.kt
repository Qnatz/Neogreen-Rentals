package com.example.myapplicationx.ui.tenants

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.databinding.FragmentEditTenantBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditTenantFragment : Fragment() {

    private var _binding: FragmentEditTenantBinding? = null
    private val binding get() = _binding!!

    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val args: EditTenantFragmentArgs by navArgs()

    // Sample house data (House Name -> House ID)
    private val houseData = mapOf(
        "House A" to "101",
        "House B" to "102",
        "House C" to "103"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTenantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the tenant from arguments
        val tenant: TenantEntity = args.tenant

        // Populate fields with existing tenant data
        populateFields(tenant)

        // Setup house dropdown with pre-selection
        setupHouseDropdown(tenant)

        // Set date pickers for Date Occupied and Date Vacated
        setupDatePicker(binding.editDateOccupied)
        setupDatePicker(binding.editDateVacated)

        // Save button click listener
        binding.saveButton.setOnClickListener {
            saveTenantDetails(tenant.tenantId)
        }
    }

    private fun populateFields(tenant: TenantEntity) {
        binding.editTenantName.setText(tenant.tenantName)
        binding.editPrimaryPhone.setText(tenant.primaryPhone)
        binding.editSecondaryPhone.setText(tenant.secondaryPhone)
        binding.editEmail.setText(tenant.email)
        binding.editDateOccupied.setText(tenant.dateOccupied)
        binding.editDateVacated.setText(tenant.dateVacated)
        binding.editHouseId.setText(tenant.houseId.toString())
    }

    private fun setupHouseDropdown(tenant: TenantEntity) {
        val houseNames = houseData.keys.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, houseNames)
        binding.editHouseNameDropdown.setAdapter(adapter)

        // Pre-select house name if available
        binding.editHouseNameDropdown.setText(tenant.houseName, false)

        // Set house ID when a house is selected
        binding.editHouseNameDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedHouse = houseNames[position]
            binding.editHouseId.setText(houseData[selectedHouse])
        }
    }

    private fun setupDatePicker(targetView: View) {
        targetView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    (targetView as? android.widget.EditText)?.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun saveTenantDetails(tenantId: Int) {
    val updatedTenant = TenantEntity(
        tenantId = tenantId,
        tenantName = binding.editTenantName.text.toString(),
        primaryPhone = binding.editPrimaryPhone.text.toString(),
        secondaryPhone = binding.editSecondaryPhone.text.toString(),
        email = binding.editEmail.text.toString(),
        houseId = binding.editHouseId.text.toString().toIntOrNull() ?: 0,
        houseName = binding.editHouseNameDropdown.text.toString(),
        dateOccupied = binding.editDateOccupied.text.toString(),
        dateVacated = binding.editDateVacated.text.toString(),
        creditBalance = 0.0,
        debitBalance = 0.0
    )

    tenantsViewModel.updateTenant(updatedTenant)

    // Notify the previous fragment about the update
    setFragmentResult("tenantUpdated", Bundle().apply {
        putParcelable("updatedTenant", updatedTenant)
    })

    Toast.makeText(requireContext(), "Tenant details updated", Toast.LENGTH_SHORT).show()

    // Navigate back to the previous screen
    findNavController().navigateUp()
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
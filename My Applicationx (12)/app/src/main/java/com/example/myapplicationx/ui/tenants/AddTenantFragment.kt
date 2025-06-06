package com.example.myapplicationx.ui.tenants

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.database.model.HouseEntity
import com.example.myapplicationx.databinding.FragmentAddTenantBinding
import com.example.myapplicationx.databinding.DialogListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.myapplicationx.ui.shared.SharedTenantViewModel
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddTenantFragment : Fragment() {

    private var _binding: FragmentAddTenantBinding? = null
    private val binding get() = _binding!!
    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val sharedTenantViewModel: SharedTenantViewModel by activityViewModels()  // Shared ViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTenantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Log when the fragment view is created
        Log.d("AddTenantFragment", "onViewCreated called")

        // Observe vacant houses and log the data
        tenantsViewModel.vacantHouses.observe(viewLifecycleOwner) { vacantHouses ->
            Log.d("AddTenantFragment", "Vacant houses observed: $vacantHouses")
            setupHouseDialog(vacantHouses)
        }

        setupDatePicker()

        // Handle the save button click
        binding.addSaveButton.setOnClickListener {
            Log.d("AddTenantFragment", "Save button clicked")

            // Get the house ID from user input
            val houseId = binding.addHouseId.text.toString().toIntOrNull()
            if (houseId == null) {
                Log.w("AddTenantFragment", "Invalid house ID entered")
                return@setOnClickListener
            }
            Log.d("AddTenantFragment", "House ID: $houseId")

            viewLifecycleOwner.lifecycleScope.launch {
                // Create a new tenant object from user input
                val newTenant = TenantEntity(
                    tenantId = 0,
                    tenantName = binding.addTenantName.text.toString(),
                    primaryPhone = binding.addPrimaryPhone.text.toString(),
                    secondaryPhone = binding.addSecondaryPhone.text.toString(),
                    email = binding.addEmail.text.toString(),
                    houseId = houseId,
                    houseName = binding.houseNameDropdown.text.toString(),
                    dateOccupied = binding.addDateOccupied.text.toString().trim(),
                    dateVacated = null,
                    creditBalance = 0.0,
                    debitBalance = 0.0
                )

                // Log tenant details
                Log.d("AddTenantFragment", "New tenant created: $newTenant")

                // Add tenant and handle invoicable and occupancy updates
                tenantsViewModel.addTenant(newTenant)
                Log.d("AddTenantFragment", "Tenant added, date occupied: ${newTenant.dateOccupied}")

                // Observe the tenant ID once the tenant is added
                tenantsViewModel.addedTenantId.observe(viewLifecycleOwner, Observer { tenantId ->
                    Log.d("AddTenantFragment", "Observed added tenant ID: $tenantId")
                    
                    // Set tenant data in the shared ViewModel
                    sharedTenantViewModel.setTenantData(
                        tenantId = tenantId.toInt(),
                        tenantName = newTenant.tenantName,
                        dateOccupied = newTenant.dateOccupied,
                        nextBillingDate = calculateNextDueDate(newTenant.dateOccupied).toString(),
                        status = "pending",
                        houseId = houseId,
                        occupied = 1,
                        vacant = 0
                    )

                    // Log navigation event
                    Log.d("AddTenantFragment", "Navigating back after adding tenant")
                    Toast.makeText(requireContext(), "Tenant added successfully", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                })
            }
        }
    }

    // Helper method to calculate the next billing date (one month from the date occupied)
    private fun calculateNextDueDate(currentDate: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDate = LocalDate.parse(currentDate, formatter)
        return parsedDate.plusMonths(1)
    }

    private fun setupHouseDialog(vacantHouses: List<HouseEntity>) {
        val houseNames = vacantHouses.map { it.houseName }

        // Trigger the dialog on dropdown click
        binding.houseNameDropdown.setOnClickListener {
            // Inflate the reusable dialog layout using binding
            val dialogBinding = DialogListBinding.inflate(layoutInflater)

            // Set up the title
            dialogBinding.dialogTitle.text = "Select a House"
            dialogBinding.dialogTitle.visibility = View.VISIBLE

            // Set up the adapter for the ListView
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, houseNames)
            dialogBinding.dialogListView.adapter = adapter

            // Create the dialog
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .create()

            // Handle item clicks
            dialogBinding.dialogListView.setOnItemClickListener { _, _, position, _ ->
                val selectedHouse = vacantHouses[position]
                binding.houseNameDropdown.setText(selectedHouse.houseName) // Update dropdown
                binding.addHouseId.setText(selectedHouse.houseId.toString()) // Update house ID
                dialog.dismiss() // Dismiss the dialog
            }

            // Show the dialog
            dialog.show()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()

        binding.addDateOccupied.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.addDateOccupied.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
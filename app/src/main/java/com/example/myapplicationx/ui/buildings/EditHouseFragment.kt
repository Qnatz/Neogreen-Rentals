package com.example.myapplicationx.ui.buildings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.database.model.HouseEntity
import com.example.myapplicationx.databinding.FragmentEditHouseBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.myapplicationx.ui.shared.SharedTenantViewModel
import androidx.fragment.app.activityViewModels
import com.example.myapplicationx.database.model.TenantData

@AndroidEntryPoint
class EditHouseFragment : Fragment() {

    private var _binding: FragmentEditHouseBinding? = null
    private val binding get() = _binding!!
    private val args: EditHouseFragmentArgs by navArgs() // Get houseId from arguments
    private val housesViewModel: HousesViewModel by viewModels()
    private val sharedTenantViewModel: SharedTenantViewModel by activityViewModels() // Shared ViewModel

    private var currentHouse: HouseEntity? = null // Store current house details

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val houseId = args.houseId
        Log.d("EditHouseFragment", "Editing house with ID: $houseId")

        // Load existing house details from ViewModel
        housesViewModel.getHouseByHouseId(houseId).observe(viewLifecycleOwner, Observer { house: HouseEntity? ->
            house?.let { houseEntity ->
                currentHouse = houseEntity // Store current house details
                binding.houseNameInput.setText(houseEntity.houseName)
                binding.occupiedInput.setText(houseEntity.occupied.toString())
                binding.vacantInput.setText(houseEntity.vacant.toString())
                binding.statusInput.setText(houseEntity.status)
                binding.buildingIdInput.setText(houseEntity.buildingId.toString())
            }
        })

        // Observe SharedTenantViewModel for tenant updates
        sharedTenantViewModel.tenantData.observe(viewLifecycleOwner, Observer { tenantData: TenantData? ->
            tenantData?.let { tenant ->
                if (tenant.houseId == houseId) {
                    Log.d("EditHouseFragment", "Updating house for tenant: ${tenant.tenantName}")

                    val updatedHouse = currentHouse?.copy(
                        occupied = tenant.occupied,
                        vacant = tenant.vacant
                    )

                    updatedHouse?.let { house ->
                        housesViewModel.updateHouse(house)
                        Toast.makeText(requireContext(), "House updated for new tenant", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        // Save button click listener
        binding.saveButton.setOnClickListener {
            val updatedHouseName = binding.houseNameInput.text.toString()
            val updatedOccupied = binding.occupiedInput.text.toString().toIntOrNull() ?: 0
            val updatedVacant = binding.vacantInput.text.toString().toIntOrNull() ?: 0
            val updatedBuildingId = binding.buildingIdInput.text.toString().toIntOrNull() ?: 0

            // Validate input fields
            if (updatedHouseName.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update house details manually
            val updatedHouse = currentHouse?.copy(
                houseName = updatedHouseName,
                occupied = updatedOccupied,
                vacant = updatedVacant,
                buildingId = updatedBuildingId
            )

            updatedHouse?.let {
                housesViewModel.updateHouse(it)
                Toast.makeText(requireContext(), "House updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Navigate back to previous screen
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
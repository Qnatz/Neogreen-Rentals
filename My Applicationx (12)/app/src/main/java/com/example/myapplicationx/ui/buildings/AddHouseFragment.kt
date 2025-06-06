package com.example.myapplicationx.ui.buildings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.database.model.HouseEntity
import com.example.myapplicationx.databinding.FragmentAddHouseBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.myapplicationx.ui.buildings.HousesViewModel
import com.example.myapplicationx.ui.buildings.BuildingsViewModel

@AndroidEntryPoint
class AddHouseFragment : Fragment() {

    private var _binding: FragmentAddHouseBinding? = null
    private val binding get() = _binding!!
    private val args: AddHouseFragmentArgs by navArgs()
    private val housesViewModel: HousesViewModel by viewModels()
    private val buildingsViewModel: BuildingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buildingId = args.buildingId
        Log.d("AddHouseFragment", "Adding house to building ID: $buildingId")

        binding.btnSaveHouse.setOnClickListener {
            val houseName = binding.addHouseName.text.toString().trim()
            val rentText = binding.addRentAmount.text.toString().trim()

            if (houseName.isEmpty() || rentText.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val houseRent = rentText.toDouble()

            // Create a new HouseEntity object with default values for occupied/vacant
            val newHouse = HouseEntity(
                houseId = 0, // ID will be auto-generated
                houseName = houseName,
                occupied = 0,
                vacant = 1,
                rentAmount = houseRent,
                buildingId = buildingId
            )

            // Save the house using the ViewModel
            housesViewModel.addHouse(newHouse)

            // Now, update the building's vacantUnits by adding 1
            buildingsViewModel.getBuildingDetails(buildingId).observe(viewLifecycleOwner) { building ->
                if (building != null) {
                    // Increment vacantUnits by 1
                    val updatedBuilding = building.copy(
                        vacantUnits = building.vacantUnits + 1
                    )
                    // Update building in ViewModel
                    buildingsViewModel.updateBuilding(updatedBuilding)
                }
            }

            // Show a success message
            Toast.makeText(requireContext(), "House added successfully", Toast.LENGTH_SHORT).show()

            // Navigate back to the previous screen
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
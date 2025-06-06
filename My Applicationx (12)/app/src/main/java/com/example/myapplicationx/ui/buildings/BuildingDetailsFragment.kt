package com.example.myapplicationx.ui.buildings

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentBuildingDetailsBinding
import com.example.myapplicationx.ui.buildings.HousesViewModel
import com.example.myapplicationx.ui.buildings.BuildingsViewModel
import com.example.myapplicationx.database.model.OccupancyStatus
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BuildingDetailsFragment : Fragment() {

    private var _binding: FragmentBuildingDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: BuildingDetailsFragmentArgs by navArgs()
    private val viewModel: BuildingsViewModel by viewModels()
    private val housesViewModel: HousesViewModel by viewModels()
    private lateinit var housesAdapter: HousesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildingDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

        val buildingId = args.buildingId

        // Load building details
        viewModel.getBuildingDetails(buildingId).observe(viewLifecycleOwner) { building ->
            building?.let {
                binding.buildingName.text = it.buildingName
                binding.buildingLocation.text = it.buildingLocation
                //binding.occupiedUnits.text = "Occupied: ${it.occupiedUnits}"
                //binding.vacantUnits.text = "Vacant: ${it.vacantUnits}"

                Glide.with(binding.buildingImage.context)
                    .load(Uri.parse(it.imageUrl))
                    .into(binding.buildingImage)
            }
        }

        // Set vacantunits and occupiedunits from count houses
        housesViewModel.fetchOccupancyStatus(buildingId).observe(viewLifecycleOwner) { occupancy: OccupancyStatus? ->
            occupancy?.let {
                binding.occupiedUnits.text = "Occupied: ${it.occupiedUnits}"
                binding.vacantUnits.text = "Vacant: ${it.vacantUnits}"
            }
        }
        
        // Set up the adapter and handle house click
        housesAdapter = HousesAdapter(emptyList()) { houseId ->
            onHouseClicked(houseId) // Call the function when a house is clicked
        }

        // Configure RecyclerView
        binding.housesRecyclerView.apply {
            adapter = housesAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_gray)!!)
            })
        }

        // Load houses for this building
        housesViewModel.getHousesByBuildingId(buildingId).observe(viewLifecycleOwner) { houses ->
            housesAdapter.updateHouses(houses)
        }
    }

    // Handle house click and navigate to HouseDetailsFragment
    private fun onHouseClicked(houseId: Int) {
        val action = BuildingDetailsFragmentDirections
            .actionBuildingDetailsFragmentToHouseDetailsFragment(houseId)
        findNavController().navigate(action) // Navigate to the house details
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_building_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_building -> {
                findNavController().navigate(
                    BuildingDetailsFragmentDirections
                        .actionBuildingDetailsFragmentToEditBuildingFragment(buildingId = args.buildingId)
                )
                true
            }
            R.id.add_house -> {
                findNavController().navigate(
                    BuildingDetailsFragmentDirections
                        .actionBuildingDetailsFragmentToAddHouseFragment(args.buildingId)
                )
                true
            }
            R.id.delete_building -> {
                showDeleteBuildingDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

   /**override fun onResume() {
    super.onResume()
    refreshOccupancyData()
}*/

    /**private fun refreshOccupancyData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val building = buildingsViewModel.getBuildingById(buildingId) // Get building details
            if (building != null) {
                binding.occupiedUnits.text = building.occupiedUnits.toString()
                binding.vacantUnits.text = building.vacantUnits.toString()
            }
        }
    }*/

    private fun showDeleteBuildingDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
        builder.setTitle("Delete Building")
            .setMessage("Are you sure you want to delete this building?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteBuilding(args.buildingId)
                Toast.makeText(requireContext(), "Building deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.navigation_buildings, false)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()

        // Set width and height after showing the dialog
        alertDialog.setOnShowListener {
            val window = alertDialog.window
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // Set the button text colors
            positiveButton.setTextColor(requireContext().getColor(R.color.alert)) // Custom red color
            negativeButton.setTextColor(requireContext().getColor(R.color.blue)) // Optional color
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
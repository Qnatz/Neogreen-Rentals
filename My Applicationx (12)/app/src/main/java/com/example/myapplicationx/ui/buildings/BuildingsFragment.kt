package com.example.myapplicationx.ui.buildings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentBuildingsBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.myapplicationx.database.model.BuildingEntity
import com.example.myapplicationx.ui.buildings.BuildingsViewModel
import com.example.myapplicationx.ui.buildings.HousesViewModel
import android.util.Log

@AndroidEntryPoint
class BuildingsFragment : Fragment() {

    private var _binding: FragmentBuildingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BuildingsViewModel by viewModels()
    private val housesViewModel: HousesViewModel by viewModels()

    private lateinit var adapter: BuildingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Ensure options menu is enabled
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up adapter with click listener for navigation
        adapter = BuildingsAdapter(emptyList()) { building ->
            val action = BuildingsFragmentDirections
                .actionNavigationBuildingsToBuildingDetailsFragment(building.buildingId)
            findNavController().navigate(action)
        }

        // Set up RecyclerView with adapter and layout manager
        binding.buildingsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@BuildingsFragment.adapter
        }

        // Observe buildings data to update adapter
        observeBuildings()
    }

    private fun observeBuildings() {
        viewModel.buildings.observe(viewLifecycleOwner) { buildingEntities ->
            adapter.submitList(buildingEntities)
            buildingEntities.forEach { building ->
                // Fetch occupancy status for each building and update adapter
                housesViewModel.fetchOccupancyStatus(building.buildingId)
                    .observe(viewLifecycleOwner) { occupancyStatus ->
                        occupancyStatus?.let { status ->
                            adapter.updateBuildingOccupancy(
                                buildingId = building.buildingId,
                                occupiedUnits = status.occupiedUnits,
                                vacantUnits = status.vacantUnits
                            )
                        }
                    }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_buildings, menu) // Inflate menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_building -> {
                val action = BuildingsFragmentDirections
                    .actionNavigationBuildingsToAddBuildingFragment()
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
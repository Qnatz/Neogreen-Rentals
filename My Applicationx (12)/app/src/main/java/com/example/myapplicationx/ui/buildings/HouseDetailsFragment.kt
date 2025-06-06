package com.example.myapplicationx.ui.buildings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.app.AlertDialog
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.HouseEntity
import com.example.myapplicationx.databinding.FragmentHouseDetailsBinding
import com.example.myapplicationx.ui.buildings.HousesViewModel
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HouseDetailsFragment : Fragment() {

    private var _binding: FragmentHouseDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: HouseDetailsFragmentArgs by navArgs()
    private val housesViewModel: HousesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set this fragment to have options menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHouseDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
    
        val houseId = args.houseId
        Log.d("HouseDetailsFragment", "House ID: $houseId")

        // Load house details by house ID
        housesViewModel.getHouseByHouseId(houseId).observe(viewLifecycleOwner) { house ->
            house?.let {
                binding.houseName.text = it.houseName
                binding.occupiedUnits.text = "Occupied: ${it.occupied}"
                binding.vacantUnits.text = "Vacant: ${it.vacant}"
                binding.status.text = it.status
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_house_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_house -> {
                showEditHouseDialog()
                true
            }
            R.id.delete_house -> {
                // Handle delete house action
                showDeleteHouseDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
   private fun showDeleteHouseDialog() {
    val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
    builder.setTitle("Delete House")
        .setMessage("Are you sure you want to delete this house?")
        .setPositiveButton("Delete") { dialog, _ ->
            housesViewModel.deleteHouse(args.houseId)
            Toast.makeText(requireContext(), "House deleted successfully", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.buildingDetailsFragment, false)
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

    val alertDialog = builder.create()

    // Set width and height after showing the dialog
    alertDialog.setOnShowListener {
        val window = alertDialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Adjust width here
            ViewGroup.LayoutParams.WRAP_CONTENT   // Adjust height here
        )
       val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        // Set the button text colors
        positiveButton.setTextColor(requireContext().getColor(R.color.alert))   // Custom red color
        negativeButton.setTextColor(requireContext().getColor(R.color.blue))  // Optional color
       
    }

    alertDialog.show()
}

    private fun showEditHouseDialog(){
        val action = HouseDetailsFragmentDirections
                .actionHouseDetailsFragmentToEditHouseFragment(args.houseId)
            findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
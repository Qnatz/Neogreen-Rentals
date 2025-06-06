package com.example.myapplicationx.ui.settings.taxes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.databinding.FragmentAddTaxBinding
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.slider.Slider

@AndroidEntryPoint
class AddTaxFragment : Fragment() {

    private var _binding: FragmentAddTaxBinding? = null
    private val binding get() = _binding!!
    private val taxesViewModel: TaxesViewModel by viewModels()

    // Data class representing a tax suggestion
    data class TaxSuggestion(val name: String, val isFixedLevy: Boolean)

    // List of predefined tax suggestions
    private val taxSuggestions = listOf(
        TaxSuggestion("Property Tax", true),
        TaxSuggestion("Rental Income Tax", true),
        TaxSuggestion("Stamp Duty", true),
        TaxSuggestion("Capital Gains Tax", true),
        TaxSuggestion("Land Tax", true),
        TaxSuggestion("Value Added Tax(VAT)", true)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Extract tax names for the AutoCompleteTextView
        val taxNames = taxSuggestions.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, taxNames)
        binding.autoCompleteTaxName.setAdapter(adapter)

        // Handle tax name selection
        binding.autoCompleteTaxName.setOnItemClickListener { parent, _, position, _ ->
            val selectedTaxName = parent.getItemAtPosition(position) as String
            val selectedTax = taxSuggestions.find { it.name == selectedTaxName }
            if (selectedTax != null) {
                // Show or hide percentage based on tax type
                binding.editTextTaxPercentage.visibility = if (selectedTax.isFixedLevy) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

        // Slider listener to show/hide percentage input based on the isFixedLevy value
        binding.sliderIsFixed.addOnChangeListener { slider, value, _ ->
            // Show percentage field only if slider value indicates fixed tax
            binding.editTextTaxPercentage.visibility = if (value > 0.5f) {
                binding.textIsFixedAnswer.text = "Yes" // If fixed, show "Yes"
                View.VISIBLE
            } else {
                binding.textIsFixedAnswer.text = "No"  // If not fixed, show "No"
                View.GONE
            }
        }

        // Focus listener to show the dropdown when AutoCompleteTextView is focused
        binding.autoCompleteTaxName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.autoCompleteTaxName.showDropDown() // Show dropdown on focus
            }
        }

        // Save button click listener
        binding.buttonSaveTax.setOnClickListener {
            val name = binding.autoCompleteTaxName.text.toString()
            val percentageText = binding.editTextTaxPercentage.text.toString()

            // Validate input fields
            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Please select a tax name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate percentage if the tax requires it
            if (binding.editTextTaxPercentage.visibility == View.VISIBLE && percentageText.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a tax percentage", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val percentage = percentageText.toDoubleOrNull() ?: 0.0
            if (percentage <= 0 && binding.editTextTaxPercentage.visibility == View.VISIBLE) {
                Toast.makeText(requireContext(), "Please enter a valid percentage", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the tax entry
            saveTax(name, percentage)
        }
    }

    private fun saveTax(name: String, percentage: Double) {
        val taxEntity = TaxEntity(
            taxId = 0, // Use 0 for a new tax entity (Room will auto-generate the ID)
            taxName = name,
            taxPercentage = percentage
        )

        taxesViewModel.addTax(taxEntity)
        requireActivity().onBackPressedDispatcher.onBackPressed() // Navigate back to the previous screen
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
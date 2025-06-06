package com.example.myapplicationx.ui.settings.taxes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.databinding.FragmentEditTaxBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditTaxFragment : Fragment() {

    private var _binding: FragmentEditTaxBinding? = null
    private val binding get() = _binding!!
    private val taxesViewModel: TaxesViewModel by viewModels()
    private val args: EditTaxFragmentArgs by navArgs()
    private lateinit var taxEntity: TaxEntity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taxEntity = args.tax
        binding.autoCompleteTaxName.setText(taxEntity.taxName)
        binding.editTextTaxPercentage.setText(taxEntity.taxPercentage?.toString() ?: "")

        // Determine if tax is fixed (i.e., percentage is null)
        val isFixed = taxEntity.taxPercentage == null
        binding.textIsFixedAnswer.text = if (isFixed) "No" else "Yes"

        binding.sliderIsFixed.value = if (isFixed) 0.0f else 1.0f
        binding.sliderIsFixed.addOnChangeListener { _, value, _ ->
            binding.textIsFixedAnswer.text = if (value == 1.0f) "Yes" else "No"
        }

        binding.buttonUpdateTax.setOnClickListener {
            val name = binding.autoCompleteTaxName.text.toString().trim()
            val percentageText = binding.editTextTaxPercentage.text.toString().trim()
            val isFixedNew = binding.sliderIsFixed.value == 0.0f

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Tax name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val percentageValue = percentageText.toDoubleOrNull()
            val updatedTax = taxEntity.copy(
                taxName = name,
                taxPercentage = if (isFixedNew) null else percentageValue
            )

            taxesViewModel.updateTax(updatedTax)
            Toast.makeText(requireContext(), "Tax updated successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
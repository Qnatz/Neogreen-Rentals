package com.example.myapplicationx.ui.settings.taxes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.databinding.FragmentTaxesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaxesFragment : Fragment() {

    private var _binding: FragmentTaxesBinding? = null
    private val binding get() = _binding!!
    private val taxesViewModel: TaxesViewModel by viewModels()
    private lateinit var taxAdapter: TaxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaxesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taxAdapter = TaxAdapter(
            onEdit = { tax -> navigateToEditTax(tax) },
            onDelete = { tax -> deleteTax(tax) }
        )

        binding.recyclerViewTaxes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taxAdapter
        }

        // Collect the taxes Flow in a lifecycle-aware manner
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                taxesViewModel.taxes.collect { taxes ->
                    taxAdapter.submitList(taxes)
                }
            }
        }

        binding.fabAddTax.setOnClickListener {
            findNavController().navigate(R.id.action_taxesFragment_to_addTaxFragment)
        }
    }

    private fun navigateToEditTax(tax: TaxEntity) {
        val action = TaxesFragmentDirections.actionTaxesFragmentToEditTaxFragment(tax)
        findNavController().navigate(action)
    }

    private fun deleteTax(tax: TaxEntity) {
        taxesViewModel.deleteTax(tax.taxId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
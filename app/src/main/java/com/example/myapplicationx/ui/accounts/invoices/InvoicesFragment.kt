package com.example.myapplicationx.ui.accounts.invoices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.InvoiceWithItems
import com.example.myapplicationx.databinding.FragmentInvoicesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InvoicesFragment : Fragment() {

    private var _binding: FragmentInvoicesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InvoicesViewModel by viewModels()
    private lateinit var adapter: InvoicesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InvoicesAdapter(
            onEditClick = { invoiceWithItems -> navigateToEditInvoice(invoiceWithItems) },
            onDeleteClick = { invoiceWithItems -> viewModel.deleteInvoice(invoiceWithItems) },
            onRowClick = { invoiceWithItems -> navigateToInvoiceDetails(invoiceWithItems) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.invoicesWithItems.collectLatest { invoiceWithItemsList ->
                adapter.submitList(invoiceWithItemsList)
            }
        }

        binding.fabAddInvoice.setOnClickListener {
            findNavController().navigate(R.id.action_invoicesFragment_to_addInvoiceFragment)
        }
    }

    private fun navigateToEditInvoice(invoiceWithItems: InvoiceWithItems) {
        val invoice = invoiceWithItems.invoice
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToEditInvoiceFragment(invoice.invoiceNumber, invoice.invoiceId)
        findNavController().navigate(action)
    }

    private fun navigateToInvoiceDetails(invoiceWithItems: InvoiceWithItems) {
        val invoice = invoiceWithItems.invoice
        val action = InvoicesFragmentDirections.actionInvoicesFragmentToInvoiceDetailsFragment(invoice.invoiceNumber)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
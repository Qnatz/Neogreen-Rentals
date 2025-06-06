package com.example.myapplicationx.ui.accounts.invoices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.database.model.InvoiceItemEntity
import com.example.myapplicationx.database.model.ServiceWithTaxCrossRefs
import com.example.myapplicationx.databinding.FragmentInvoiceDetailsBinding
import com.example.myapplicationx.ui.settings.services.ServicesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InvoiceDetailsFragment : Fragment() {

    private var _binding: FragmentInvoiceDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: InvoiceDetailsFragmentArgs by navArgs()
    private val viewModel: InvoicesViewModel by viewModels()
    private val servicesViewModel: ServicesViewModel by viewModels()

    private val itemsList = mutableListOf<InvoiceItemEntity>()
    private lateinit var adapter: InvoiceItemAdapter
    private var availableServices: List<ServiceWithTaxCrossRefs> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoiceDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeInvoiceData()
        observeServices()
    }

    private fun setupRecyclerView() {
        adapter = InvoiceItemAdapter(itemsList, availableServices, {}, {})
        binding.recyclerDetailsInvoiceItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDetailsInvoiceItems.adapter = adapter
    }

    private fun observeInvoiceData() {
        itemsList.clear()
        adapter.notifyDataSetChanged()

        lifecycleScope.launch {
            viewModel.getInvoiceWithItems(args.invoiceNumber).collect { invoiceWithItems ->
                invoiceWithItems?.let {
                    binding.textInvoiceNumber.text = it.invoice.invoiceNumber
                    binding.textInvoiceDate.text = "Invoice Date: ${it.invoice.invoiceDate}"
                    binding.textReceivableDate.text = "Receivable Date: ${it.invoice.receivableDate}"
                    binding.textTenantName.text = it.invoice.tenantName
                    binding.textTenantId.text = it.invoice.tenantId.toString()
                    binding.textInvoiceAmount.text = "Amount: UGx ${it.invoice.invoiceAmount.toString()}"
                    binding.textNotes.text = it.invoice.invoiceNotes
                    binding.textInvoiceDue.text = "Due: UGx ${it.invoice.invoiceAmountDue?.toString().orEmpty()}"
                    binding.textStatus.text = it.invoice.status.orEmpty()

                    itemsList.clear()
                    itemsList.addAll(it.items)
                    updateAdapterData()
                } ?: run {
                    Toast.makeText(requireContext(), "Invoice not found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeServices() {
    lifecycleScope.launch {
        servicesViewModel.allSTInvoiceDetails.collect { services ->
            availableServices = services
            updateAdapterData()
        }
    }
}

    private fun updateAdapterData() {
        if (_binding == null) return
        adapter = InvoiceItemAdapter(itemsList, availableServices, {}, {})
        binding.recyclerDetailsInvoiceItems.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

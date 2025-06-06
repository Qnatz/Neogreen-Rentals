package com.example.myapplicationx.ui.accounts.invoices

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.InvoiceItemEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.ServiceWithTaxCrossRefs
import com.example.myapplicationx.databinding.FragmentAddInvoiceBinding
import com.example.myapplicationx.databinding.PopupAddItemBinding
import com.example.myapplicationx.databinding.PopupEnterBaseRateBinding
import com.example.myapplicationx.databinding.PopupEnterMeteredUsageBinding
import com.example.myapplicationx.ui.settings.services.ServicesViewModel
import com.example.myapplicationx.ui.tenants.TenantsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditInvoiceFragment : Fragment() {

    private var _binding: FragmentAddInvoiceBinding? = null
    private val binding get() = _binding!!

    private val args: EditInvoiceFragmentArgs by navArgs()
    private val invoiceViewModel: InvoicesViewModel by viewModels()
    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val servicesViewModel: ServicesViewModel by viewModels()

    private val itemsList = mutableListOf<InvoiceItemEntity>()
    private lateinit var adapter: InvoiceItemAdapter
    private var availableServices: List<ServiceWithTaxCrossRefs> = emptyList()

    private var originalTotalAmount: Double = 0.0
    private var originalAmountDue: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddInvoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadInvoiceData()
        observeServices()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.apply {
            editTenantName.isEnabled = false
            editTenantId.isEnabled = false
            editInvoiceNumber.isEnabled = false
            previewButton.text = "Preview"
            saveButton.text = "Update"
        }
        setupRecyclerView()
    }

    private fun loadInvoiceData() {
        lifecycleScope.launch {
            invoiceViewModel.getInvoiceWithItems(args.invoiceNumber)
                .collect { data ->
                    if (data == null) {
                        Toast.makeText(requireContext(), "Invoice not found", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        return@collect
                    }

                    populateInvoiceData(data.invoice)
                    originalTotalAmount = data.invoice.invoiceAmount
                    originalAmountDue = data.invoice.invoiceAmountDue ?: 0.0

                    itemsList.clear()
                    itemsList.addAll(data.items)
                    adapter.notifyDataSetChanged()
                    updateTotalAmount()
                }
        }
    }

    private fun observeServices() {
        servicesViewModel.allServicesWithTaxes.observe(viewLifecycleOwner) { services ->
            services?.let {
                availableServices = it
                adapter = InvoiceItemAdapter(itemsList, availableServices, ::onItemUpdated, ::onItemDeleted)
                binding.invoiceItemsRecyclerView.adapter = adapter
            }
        }
    }

    private fun populateInvoiceData(invoice: InvoiceEntity) {
        binding.apply {
            editInvoiceNumber.setText(invoice.invoiceNumber)
            editInvoiceDate.setText(invoice.invoiceDate)
            editReceivableDate.setText(invoice.receivableDate)
            editTenantName.setText(invoice.tenantName)
            editTenantId.setText(invoice.tenantId.toString())
            editAmount.setText(invoice.invoiceAmount.toString())
            editAmountDue.setText(invoice.invoiceAmountDue.toString())
            editNotesId.setText(invoice.invoiceNotes)
        }
    }

    private fun setupRecyclerView() {
        adapter = InvoiceItemAdapter(itemsList, availableServices, ::onItemUpdated, ::onItemDeleted)
        binding.invoiceItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@EditInvoiceFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.addItemButton.setOnClickListener { showAddItemDialog() }
        binding.previewButton.setOnClickListener { navigateToInvoicePdf(isPreviewOnly = true) }
        binding.saveButton.setOnClickListener { updateInvoice() }
    }

    private fun updateInvoice() {
        val tenantId = binding.editTenantId.text.toString().toIntOrNull()
        if (tenantId == null) {
            Toast.makeText(requireContext(), "Invalid tenant ID", Toast.LENGTH_SHORT).show()
            return
        }

        val newTotal = itemsList.sumOf { it.computedTotal }
        val originalPaidAmount = originalTotalAmount - originalAmountDue
        val newAmountDue = calculateNewAmountDue(newTotal)
        val newPaidAmount = newTotal - newAmountDue

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                invoiceViewModel.updateInvoiceWithItems(
                    invoice            = createUpdatedInvoice(newTotal),
                    items              = itemsList,
                    tenantId           = tenantId,
                    originalTotal      = originalTotalAmount,
                    newTotal           = newTotal,
                    originalPaidAmount = originalPaidAmount,
                    newPaidAmount      = newPaidAmount
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Invoice updated", Toast.LENGTH_SHORT).show()
                    navigateToInvoicePdf(isPreviewOnly = false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditInvoiceFragment", "Error updating invoice", e)
                }
            }
        }
    }

    private fun createUpdatedInvoice(newTotal: Double): InvoiceEntity {
        val newDue = calculateNewAmountDue(newTotal)
        return InvoiceEntity(
            invoiceId        = args.invoiceId,
            invoiceNumber    = args.invoiceNumber,
            invoiceDate      = binding.editInvoiceDate.text.toString(),
            receivableDate   = binding.editReceivableDate.text.toString(),
            tenantId         = binding.editTenantId.text.toString().toInt(),
            tenantName       = binding.editTenantName.text.toString(),
            invoiceAmount    = newTotal,
            invoiceAmountDue = newDue,
            invoiceNotes     = binding.editNotesId.text.toString(),
            status           = determineInvoiceStatus(newTotal)
        )
    }

    private fun calculateNewAmountDue(newTotal: Double): Double {
        val originalPaid = originalTotalAmount - originalAmountDue
        return (newTotal - originalPaid).coerceAtLeast(0.0)
    }

    private fun determineInvoiceStatus(newTotal: Double): String {
        val due = calculateNewAmountDue(newTotal)
        return when {
            due <= 0.0         -> "Paid"
            due < newTotal     -> "Partial"
            else               -> "Unpaid"
        }
    }

    private fun showAddItemDialog() {
        val dialogBinding = PopupAddItemBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        fun setupServiceList(rentRate: Double) {
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
            val displayList = mutableListOf<ServiceItemDisplay>()

            availableServices
                .find { it.service.serviceName.equals("Rent", true) }
                ?.takeIf { rentRate > 0.0 }
                ?.also {
                    displayList.add(
                        ServiceItemDisplay.Rent(
                            rentRate     = rentRate,
                            description  = "Rent – $monthName",
                            taxCrossRefs = it.taxCrossRefs,
                            serviceId    = it.service.serviceId
                        )
                    )
                }

            availableServices
                .filterNot { it.service.serviceName.equals("Rent", true) }
                .mapTo(displayList) { ServiceItemDisplay.Service(it) }

            dialogBinding.serviceListView.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayList.map {
                    when (it) {
                        is ServiceItemDisplay.Rent    -> it.description
                        is ServiceItemDisplay.Service -> it.serviceWithTaxes.service.serviceName
                    }
                }
            )

            dialogBinding.serviceListView.setOnItemClickListener { _, _, pos, _ ->
                when (val sel = displayList[pos]) {
                    is ServiceItemDisplay.Rent    -> handleRentItem(sel)
                    is ServiceItemDisplay.Service -> handleServiceItem(sel.serviceWithTaxes)
                }
                dialog.dismiss()
            }
        }

        val tenantId = binding.editTenantId.text.toString().toIntOrNull()
        if (tenantId != null) {
            tenantsViewModel.getHouseRentByTenantId(tenantId) { rent ->
                setupServiceList(rent)
            }
        } else {
            setupServiceList(0.0)
        }
        dialog.show()
    }

    private fun handleRentItem(rentItem: ServiceItemDisplay.Rent) {
        val invoiceItem = InvoiceItemEntity(
            invoiceNumber  = args.invoiceNumber,
            itemDescription = "Rent",
            itemUnits       = 1.0,
            itemUnitPrice   = rentItem.rentRate,
            itemRate        = rentItem.rentRate,
            computedNetBase = calculateNetBase(rentItem.rentRate, rentItem.taxCrossRefs),
            inclusiveTax    = calculateInclusiveTax(rentItem.rentRate, rentItem.taxCrossRefs),
            exclusiveTax    = calculateExclusiveTax(rentItem.rentRate, rentItem.taxCrossRefs),
            computedTax     = calculateTotalTax(rentItem.rentRate, rentItem.taxCrossRefs),
            computedTotal   = calculateTotalAmount(rentItem.rentRate, rentItem.taxCrossRefs),
            taxCrossRefs    = rentItem.taxCrossRefs,
            serviceId       = rentItem.serviceId
        )
        itemsList.add(invoiceItem)
        adapter.notifyItemInserted(itemsList.size - 1)
        updateTotalAmount()
    }

    private fun handleServiceItem(serviceWithTaxes: ServiceWithTaxCrossRefs) {
        when {
            serviceWithTaxes.service.isMetered   -> showEnterUsageDialog(serviceWithTaxes)
            serviceWithTaxes.service.isFixedPrice -> addFixedPriceItem(serviceWithTaxes)
            else                                  -> showEnterBaseRateDialog(serviceWithTaxes)
        }
    }

    private fun showEnterUsageDialog(service: ServiceWithTaxCrossRefs) {
        val usageBinding = PopupEnterMeteredUsageBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setView(usageBinding.root)
            .create()
            .apply {
                usageBinding.saveButton.setOnClickListener {
                    val usage    = usageBinding.editUsage.text.toString().toDoubleOrNull()
                    val unitPrice = service.service.unitPrice
                        ?: usageBinding.editUnitPrice.text.toString().toDoubleOrNull()

                    if (usage != null && unitPrice != null) {
                        addMeteredItem(service, usage, unitPrice)
                        dismiss()
                    } else {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                }
                show()
            }
    }

    private fun addMeteredItem(service: ServiceWithTaxCrossRefs, usage: Double, unitPrice: Double) {
        val subtotal = usage * unitPrice
        val item = InvoiceItemEntity(
            invoiceNumber  = args.invoiceNumber,
            itemDescription = service.service.serviceName,
            itemUnits       = usage,
            itemUnitPrice   = unitPrice,
            itemRate        = subtotal,
            computedNetBase = calculateNetBase(subtotal, service.taxCrossRefs),
            inclusiveTax    = calculateInclusiveTax(subtotal, service.taxCrossRefs),
            exclusiveTax    = calculateExclusiveTax(subtotal, service.taxCrossRefs),
            computedTax     = calculateTotalTax(subtotal, service.taxCrossRefs),
            computedTotal   = calculateTotalAmount(subtotal, service.taxCrossRefs),
            taxCrossRefs    = service.taxCrossRefs,
            serviceId       = service.service.serviceId
        )
        itemsList.add(item)
        adapter.notifyItemInserted(itemsList.size - 1)
        updateTotalAmount()
    }

    private fun addFixedPriceItem(service: ServiceWithTaxCrossRefs) {
        val rate = service.service.fixedPrice ?: 0.0
        val item = InvoiceItemEntity(
            invoiceNumber  = args.invoiceNumber,
            itemDescription = service.service.serviceName,
            itemUnits       = 1.0,
            itemUnitPrice   = rate,
            itemRate        = rate,
            computedNetBase = calculateNetBase(rate, service.taxCrossRefs),
            inclusiveTax    = calculateInclusiveTax(rate, service.taxCrossRefs),
            exclusiveTax    = calculateExclusiveTax(rate, service.taxCrossRefs),
            computedTax     = calculateTotalTax(rate, service.taxCrossRefs),
            computedTotal   = calculateTotalAmount(rate, service.taxCrossRefs),
            taxCrossRefs    = service.taxCrossRefs,
            serviceId       = service.service.serviceId
        )
        itemsList.add(item)
        adapter.notifyItemInserted(itemsList.size - 1)
        updateTotalAmount()
    }

    private fun showEnterBaseRateDialog(service: ServiceWithTaxCrossRefs) {
        val baseRateBinding = PopupEnterBaseRateBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext())
            .setView(baseRateBinding.root)
            .create()
            .apply {
                baseRateBinding.saveButton.setOnClickListener {
                    val rate = baseRateBinding.editBaseRate.text.toString().toDoubleOrNull()
                    if (rate != null) {
                        val item = InvoiceItemEntity(
                            invoiceNumber  = args.invoiceNumber,
                            itemDescription = service.service.serviceName,
                            itemUnits       = 1.0,
                            itemUnitPrice   = rate,
                            itemRate        = rate,
                            computedNetBase = calculateNetBase(rate, service.taxCrossRefs),
                            inclusiveTax    = calculateInclusiveTax(rate, service.taxCrossRefs),
                            exclusiveTax    = calculateExclusiveTax(rate, service.taxCrossRefs),
                            computedTax     = calculateTotalTax(rate, service.taxCrossRefs),
                            computedTotal   = calculateTotalAmount(rate, service.taxCrossRefs),
                            taxCrossRefs    = service.taxCrossRefs,
                            serviceId       = service.service.serviceId
                        )
                        itemsList.add(item)
                        adapter.notifyItemInserted(itemsList.size - 1)
                        updateTotalAmount()
                        dismiss()
                    } else {
                        Toast.makeText(context, "Invalid rate", Toast.LENGTH_SHORT).show()
                    }
                }
                show()
            }
    }

    private fun calculateNetBase(amount: Double, taxes: List<ServiceTaxCrossRef>): Double {
        val inclusive = taxes.filter { it.isInclusive }
            .sumOf { amount * (it.taxPercentage ?: 0.0) / (100 + (it.taxPercentage ?: 0.0)) }
        return amount - inclusive
    }

    private fun calculateInclusiveTax(amount: Double, taxes: List<ServiceTaxCrossRef>): Double {
        return taxes.filter { it.isInclusive }
            .sumOf { amount * (it.taxPercentage ?: 0.0) / (100 + (it.taxPercentage ?: 0.0)) }
    }

    private fun calculateExclusiveTax(amount: Double, taxes: List<ServiceTaxCrossRef>): Double {
        val netBase = calculateNetBase(amount, taxes)
        return taxes.filter { !it.isInclusive }
            .sumOf { netBase * (it.taxPercentage ?: 0.0) / 100 }
    }

    private fun calculateTotalTax(amount: Double, taxes: List<ServiceTaxCrossRef>): Double {
        return calculateInclusiveTax(amount, taxes) + calculateExclusiveTax(amount, taxes)
    }

    private fun calculateTotalAmount(amount: Double, taxes: List<ServiceTaxCrossRef>): Double {
        return calculateNetBase(amount, taxes) + calculateExclusiveTax(amount, taxes)
    }

    private fun updateTotalAmount() {
        val total = itemsList.sumOf { it.computedTotal }
        binding.editAmount.setText(total.toString())
        binding.editAmountDue.setText(total.toString())
    }

    private fun navigateToInvoicePdf(isPreviewOnly: Boolean) {
        val action = EditInvoiceFragmentDirections.actionEditInvoiceFragmentToInvoicePreviewFragment(
            templateName = "invoice_template_default",
            invoice      = createTempInvoiceEntity(),
            items        = itemsList.toTypedArray(),
            isPreviewOnly = isPreviewOnly
        )
        if (findNavController().currentDestination?.id == R.id.editInvoiceFragment) {
            findNavController().navigate(action)
        }
    }

    private fun createTempInvoiceEntity(): InvoiceEntity {
        return InvoiceEntity(
            invoiceNumber   = args.invoiceNumber,
            invoiceDate     = binding.editInvoiceDate.text.toString(),
            receivableDate  = binding.editReceivableDate.text.toString(),
            tenantId        = binding.editTenantId.text.toString().toInt(),
            tenantName      = binding.editTenantName.text.toString(),
            invoiceAmount   = itemsList.sumOf { it.computedTotal },
            invoiceAmountDue= binding.editAmountDue.text.toString().toDouble(),
            invoiceNotes    = binding.editNotesId.text.toString()
        )
    }

    private fun onItemUpdated(updatedItem: InvoiceItemEntity) {
        val idx = itemsList.indexOfFirst { it.invoiceItemId == updatedItem.invoiceItemId }
        if (idx >= 0) {
            itemsList[idx] = updatedItem
            adapter.notifyItemChanged(idx)
            updateTotalAmount()
        }
    }

    private fun onItemDeleted(position: Int) {
        itemsList.removeAt(position)
        adapter.notifyItemRemoved(position)
        updateTotalAmount()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private sealed class ServiceItemDisplay {
        data class Rent(
            val rentRate: Double,
            val description: String,
            val taxCrossRefs: List<ServiceTaxCrossRef>,
            val serviceId: Int
        ) : ServiceItemDisplay()

        data class Service(val serviceWithTaxes: ServiceWithTaxCrossRefs) : ServiceItemDisplay()
    }
}


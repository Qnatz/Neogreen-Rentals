package com.example.myapplicationx.ui.accounts.invoices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.InvoiceItemEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.ServiceWithTaxCrossRefs
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.databinding.FragmentAddInvoiceBinding
import com.example.myapplicationx.databinding.PopupAddItemBinding
import com.example.myapplicationx.databinding.PopupEnterBaseRateBinding
import com.example.myapplicationx.databinding.PopupEnterMeteredUsageBinding
import com.example.myapplicationx.ui.accounts.invoicables.InvoicablesViewModel
import com.example.myapplicationx.ui.settings.services.ServicesViewModel
import com.example.myapplicationx.ui.tenants.TenantsViewModel
import java.text.SimpleDateFormat
import java.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddInvoiceFragment : Fragment() {

    private var _binding: FragmentAddInvoiceBinding? = null
    private val binding get() = _binding!!

    private val invoiceViewModel: InvoicesViewModel by viewModels()
    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val servicesViewModel: ServicesViewModel by viewModels()
    private val invoicablesViewModel: InvoicablesViewModel by viewModels()

    // Store invoicableId as Int? for later use
    private var invoicableId: Int? = null

    // This list holds the final invoice items that will be saved.
    private val itemsList = mutableListOf<InvoiceItemEntity>()
    private lateinit var adapter: InvoiceItemAdapter
    //private lateinit var serviceWithTaxes: ServiceWithTaxCrossRefs
    
    private var generatedInvoiceNumber: String = ""
    private var availableServices: List<ServiceWithTaxCrossRefs> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddInvoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeServices()
        setupRecyclerView()
        observeTenants()
        setupClickListeners()

        // Extract safe args
        val args = AddInvoiceFragmentArgs.fromBundle(requireArguments())
        if (args.isFromInvoicable) {
            invoicableId = if (args.invoicableId == -1) null else args.invoicableId
            fromInvoicable(
                tenantId = if (args.tenantId == -1) null else args.tenantId,
                tenantName = args.tenantName.takeIf { it.isNotEmpty() },
                nextBillingDate = args.nextBillingDate.takeIf { it.isNotEmpty() }
            )
        } else {
            autoFillFields()
        }
    }

    private fun observeServices() {
    servicesViewModel.allServicesWithTaxes.observe(viewLifecycleOwner) { services ->
        availableServices = services
        // Update the adapter with the new list.
        adapter = InvoiceItemAdapter(itemsList, availableServices, ::onItemUpdated, ::onItemDeleted)
        binding.invoiceItemsRecyclerView.adapter = adapter
    }
}

    private fun observeTenants() {
        tenantsViewModel.tenantsL.observe(viewLifecycleOwner) { tenants ->
            setupTenantsDropdown(tenants)
        }
    }

    private fun setupTenantsDropdown(tenants: List<TenantEntity>) {
        binding.editTenantName.setOnClickListener {
            val tenantNames = tenants.map { it.tenantName }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Select Tenant")
                .setItems(tenantNames) { _, position ->
                    val selectedTenant = tenants[position]
                    binding.editTenantName.setText(selectedTenant.tenantName)
                    binding.editTenantId.setText(selectedTenant.tenantId.toString())
                    generateInvoiceNumber(selectedTenant.tenantId)
                }
                .show()
        }
    }

    private fun setupRecyclerView() {
        adapter = InvoiceItemAdapter(itemsList, availableServices, ::onItemUpdated, ::onItemDeleted)
        binding.invoiceItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AddInvoiceFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.addItemButton.setOnClickListener { showAddItemDialog() }
        binding.previewButton.setOnClickListener { navigateToInvoicePdf(isPreviewOnly = true) }
        binding.saveButton.setOnClickListener { saveInvoice() }
    }

    /**
     * Sealed class to represent either a Rent item (from house rent) or a Service item.
     */
    private sealed class ServiceItemDisplay {
    data class Rent(
        val rentRate: Double,
        val description: String,
        val taxCrossRefs: List<ServiceTaxCrossRef>,
        val serviceId: Int  // Added service id for Rent
    ) : ServiceItemDisplay()

    data class Service(val serviceWithTaxes: ServiceWithTaxCrossRefs) : ServiceItemDisplay()
    }

    /**
     * Creates an InvoiceItemEntity for non-metered services.
     *
     * For non-metered services, if isFixedPrice is true the preentered unitPrice is used;
     * otherwise, a user-entered base rate is expected.
     */
    private fun createInvoiceItemFromService(
    serviceWithTaxes: ServiceWithTaxCrossRefs,
    invoiceNumber: String,
    enteredBaseRate: Double? = null
): InvoiceItemEntity {
    val service = serviceWithTaxes.service
    // For non-metered services:
    // Use fixedPrice if the service is fixed price, otherwise use the entered base rate.
    val units = 1.0
    val unitPrice = if (service.isFixedPrice) {
        service.fixedPrice ?: 0.0
    } else {
        enteredBaseRate ?: 0.0
    }
    val subtotal = unitPrice * units

    var inclusiveTaxTotal = 0.0
    var exclusiveTaxTotal = 0.0
    for (taxRef in serviceWithTaxes.taxCrossRefs) {
        val taxPercentage = taxRef.taxPercentage ?: 0.0
        if (taxRef.isInclusive) {
            inclusiveTaxTotal += subtotal * (taxPercentage / (100 + taxPercentage))
        } else {
            exclusiveTaxTotal += subtotal * (taxPercentage / 100)
        }
    }
    val netBase = subtotal - inclusiveTaxTotal
    val totalTax = inclusiveTaxTotal + exclusiveTaxTotal
    val computedTotal = netBase + exclusiveTaxTotal

    return InvoiceItemEntity(
        invoiceItemId = itemsList.size + 1,
        invoiceNumber = invoiceNumber,
        itemDescription = service.serviceName,
        itemUnits = units,
        itemUnitPrice = unitPrice,  // Displayed unit price (fixedPrice for fixed-price services)
        itemRate = subtotal,        // The subtotal before tax adjustments
        computedNetBase = netBase,
        inclusiveTax = inclusiveTaxTotal,
        exclusiveTax = exclusiveTaxTotal,
        computedTax = totalTax,
        computedTotal = computedTotal,
        taxCrossRefs = serviceWithTaxes.taxCrossRefs,
        serviceId = service.serviceId
    )
}

    /**
     * Creates an InvoiceItemEntity for Rent, merging house rent and its taxes (if any).
     */
    private fun createInvoiceItemForRent(
    rentRate: Double,
    taxCrossRefs: List<ServiceTaxCrossRef>,
    invoiceNumber: String,
    serviceId: Int  // Add the serviceId parameter here.
): InvoiceItemEntity {
    var inclusiveTaxTotal = 0.0
    var exclusiveTaxTotal = 0.0
    for (taxRef in taxCrossRefs) {
        val taxPercentage = taxRef.taxPercentage ?: 0.0
        if (taxRef.isInclusive) {
            inclusiveTaxTotal += rentRate * (taxPercentage / (100 + taxPercentage))
        } else {
            exclusiveTaxTotal += rentRate * (taxPercentage / 100)
        }
    }
    val netBase = rentRate - inclusiveTaxTotal
    val totalTax = inclusiveTaxTotal + exclusiveTaxTotal
    val computedTotal = netBase + totalTax

    return InvoiceItemEntity(
        invoiceItemId = itemsList.size + 1,
        invoiceNumber = invoiceNumber,
        itemDescription = "Rent",
        itemUnits = 1.0,
        itemUnitPrice = rentRate,
        itemRate = rentRate,
        computedNetBase = netBase,
        inclusiveTax = inclusiveTaxTotal,
        exclusiveTax = exclusiveTaxTotal,
        computedTax = totalTax,
        computedTotal = computedTotal,
        taxCrossRefs = taxCrossRefs,
        serviceId = serviceId  // Use the provided serviceId.
    )
}

    /**
     * For metered services, prompt the user to enter usage units.
     *
     * If unitPrice is already provided (non-null) in the service, it is used automatically.
     */
    private fun showEnterUsageDialog(
    serviceWithTaxes: ServiceWithTaxCrossRefs,
    invoiceNumber: String
) {
    val usageBinding = PopupEnterMeteredUsageBinding.inflate(layoutInflater)
    val usageDialog = AlertDialog.Builder(requireContext())
        .setView(usageBinding.root)
        .create()

    val serviceEntity = serviceWithTaxes.service
    Log.d("AddInvoiceFragment", "Showing usage dialog for service: ${serviceEntity.serviceName}, isMetered=${serviceEntity.isMetered}, unitPrice=${serviceEntity.unitPrice}")

    // Always show the usage input field.
    usageBinding.editUsage.visibility = View.VISIBLE

    // For metered services:
    // If a unit price is provided in the service, display it and disable editing.
    // (Note: Instead of reading from the EditText later, we will use serviceEntity.unitPrice if available.)
    if (serviceEntity.unitPrice != null) {
        usageBinding.editUnitPrice.apply {
            setText(serviceEntity.unitPrice.toString())
            isEnabled = false
            visibility = View.VISIBLE
        }
    } else {
        usageBinding.editUnitPrice.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
    }

    usageBinding.saveButton.setOnClickListener {
        val usageStr = usageBinding.editUsage.text.toString().trim()
        val usage = usageStr.toDoubleOrNull()
        if (usage == null || usage <= 0) {
            Toast.makeText(requireContext(), "Enter a valid usage", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        // Use serviceEntity.unitPrice if available; otherwise, get the value from the EditText.
        val unitPrice = serviceEntity.unitPrice ?: usageBinding.editUnitPrice.text.toString().trim().toDoubleOrNull()
        if (unitPrice == null || unitPrice <= 0) {
            Toast.makeText(requireContext(), "Enter a valid unit price", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        val subtotal = unitPrice * usage

        var inclusiveTaxTotal = 0.0
        var exclusiveTaxTotal = 0.0
        for (taxRef in serviceWithTaxes.taxCrossRefs) {
            val taxPercentage = taxRef.taxPercentage ?: 0.0
            if (taxRef.isInclusive) {
                inclusiveTaxTotal += subtotal * (taxPercentage / (100 + taxPercentage))
            } else {
                exclusiveTaxTotal += subtotal * (taxPercentage / 100)
            }
        }
        val netBase = subtotal - inclusiveTaxTotal
        val totalTax = inclusiveTaxTotal + exclusiveTaxTotal
        val computedTotal = netBase + totalTax

        Log.d("AddInvoiceFragment", "Usage: $usage, UnitPrice: $unitPrice, Subtotal: $subtotal, ComputedTotal: $computedTotal")

        val invoiceItem = InvoiceItemEntity(
            invoiceItemId = itemsList.size + 1,
            invoiceNumber = invoiceNumber,
            itemDescription = serviceEntity.serviceName,
            itemUnits = usage,
            itemUnitPrice = unitPrice,
            itemRate = subtotal,
            computedNetBase = netBase,
            inclusiveTax = inclusiveTaxTotal,
            exclusiveTax = exclusiveTaxTotal,
            computedTax = totalTax,
            computedTotal = computedTotal,
            taxCrossRefs = serviceWithTaxes.taxCrossRefs,
            serviceId = serviceEntity.serviceId
        )
        itemsList.add(invoiceItem)
        adapter.notifyItemInserted(itemsList.size - 1)
        updateTotalAmount()

        usageDialog.dismiss()
    }

    usageDialog.show()
}

    /**
     * Shows the Add Item dialog. This method builds a combined list for selection.
     * Rent is taken from the house object (if available) and merged with available services.
     */
    private fun showAddItemDialog() {
    // Inflate the popup layout using view binding.
    val dialogBinding = PopupAddItemBinding.inflate(layoutInflater)
    // Create an AlertDialog with the inflated view.
    val dialog = AlertDialog.Builder(requireContext())
        .setView(dialogBinding.root)
        .create()
    
    // Get tenantId from the main layout (if available).
    val tenantId = binding.editTenantId.text.toString().toIntOrNull()

    // Function to setup and display the list of services (including Rent if applicable).
    fun setupServiceList(rentRate: Double) {
        Log.d("AddInvoiceFragment", "setupServiceList: availableServices size = ${availableServices.size}")
        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
        val displayList = mutableListOf<ServiceItemDisplay>()

        // Find Rent service from availableServices (ensure it matches exactly what is stored)
        val rentService = availableServices.find { 
            it.service.serviceName.trim().equals("Rent", ignoreCase = true)
        }
        if (rentRate > 0.0 && rentService != null) {
            displayList.add(
                ServiceItemDisplay.Rent(
                    rentRate = rentRate,
                    description = "Rent - for the month of $currentMonth",
                    taxCrossRefs = rentService.taxCrossRefs,
                    serviceId = rentService.service.serviceId  // Pass the rent service id
                )
            )
        }

        // Filter out Rent services and add all others
        val filteredServices = availableServices.filterNot { 
            it.service.serviceName.trim().equals("Rent", ignoreCase = true)
        }
        filteredServices.forEach { serviceWithTaxes ->
            displayList.add(ServiceItemDisplay.Service(serviceWithTaxes))
            // Add debug log to check services and their tax references
            Log.d("AddInvoiceFragment", "Service: ${serviceWithTaxes.service.serviceName}, " +
                    "ID: ${serviceWithTaxes.service.serviceId}, " +
                    "Has ${serviceWithTaxes.taxCrossRefs.size} taxes")
        }
        
        // Log the resulting display list size
        Log.d("AddInvoiceFragment", "Display list size: ${displayList.size}")

        // Prepare list items to show in the popup
        val adapterItems = displayList.map {
            when (it) {
                is ServiceItemDisplay.Rent -> it.description
                is ServiceItemDisplay.Service -> it.serviceWithTaxes.service.serviceName
            }
        }
        val listAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            adapterItems
        )
        dialogBinding.serviceListView.adapter = listAdapter

        dialogBinding.serviceListView.setOnItemClickListener { _, _, position, _ ->
    val selectedItem = displayList[position]
    when (selectedItem) {
        is ServiceItemDisplay.Rent -> {
            val invoiceItem = createInvoiceItemForRent(
                rentRate = selectedItem.rentRate,
                taxCrossRefs = selectedItem.taxCrossRefs,
                invoiceNumber = generatedInvoiceNumber,
                serviceId = selectedItem.serviceId
            )
            itemsList.add(invoiceItem)
            adapter.notifyItemInserted(itemsList.size - 1)
            updateTotalAmount()
            dialog.dismiss()
        }
        is ServiceItemDisplay.Service -> {
            val serviceWithTaxes = selectedItem.serviceWithTaxes
            val service = serviceWithTaxes.service
            Log.d("AddInvoiceFragment", "Selected service: ${service.serviceName}, unitPrice=${service.unitPrice}, isMetered=${service.isMetered}, isFixedPrice=${service.isFixedPrice}")

            if (service.isMetered) {
                // For metered services, always prompt for usage.
                showEnterUsageDialog(serviceWithTaxes, generatedInvoiceNumber)
                dialog.dismiss()
            } else {
                // For non-metered services:
                if (!service.isFixedPrice && service.unitPrice == null) {
                    // If not fixed price and no unit price provided, prompt for base rate.
                    showEnterBaseRateDialog(serviceWithTaxes, generatedInvoiceNumber)
                    dialog.dismiss()
                } else {
                    // Otherwise, create invoice item automatically using the preset unitPrice.
                    val invoiceItem = createInvoiceItemFromService(serviceWithTaxes, generatedInvoiceNumber)
                    itemsList.add(invoiceItem)
                    adapter.notifyItemInserted(itemsList.size - 1)
                    updateTotalAmount()
                    dialog.dismiss()
                }
            }
        }
    }
}
}

    // Use the tenantId to fetch the house rent rate.
    if (tenantId != null) {
        tenantsViewModel.getHouseRentByTenantId(tenantId) { rentRate ->
            setupServiceList(rentRate)
        }
    } else {
        setupServiceList(0.0)
    }

    // Finally, show the dialog.
    dialog.show()
}

    /**
     * Opens a dialog to prompt the user for the base rate for a non-metered service with no preentered unit price.
     */
    private fun showEnterBaseRateDialog(
    serviceWithTaxes: ServiceWithTaxCrossRefs,
    invoiceNumber: String
) {
    val baseRateBinding = PopupEnterBaseRateBinding.inflate(layoutInflater)
    val baseRateDialog = AlertDialog.Builder(requireContext())
        .setView(baseRateBinding.root)
        .create()

    baseRateBinding.saveButton.setOnClickListener {
        val baseRateStr = baseRateBinding.editBaseRate.text.toString().trim()
        val enteredBaseRate = baseRateStr.toDoubleOrNull()
        if (enteredBaseRate == null || enteredBaseRate <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid base rate.", Toast.LENGTH_SHORT).show()
        } else {
            val invoiceItem = createInvoiceItemFromService(serviceWithTaxes, invoiceNumber, enteredBaseRate)
            itemsList.add(invoiceItem)
            adapter.notifyItemInserted(itemsList.size - 1)
            updateTotalAmount()
            baseRateDialog.dismiss()
        }
    }
    baseRateDialog.show()
}

    private fun updateTotalAmount() {
        // Use the sum of computedTotal so that the total reflects taxes.
        val totalAmount = itemsList.sumOf { it.computedTotal }
        binding.editAmount.setText(totalAmount.toString())
        binding.editAmountDue.setText(totalAmount.toString())
    }

    private fun getCurrentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getFutureDate(baseDate: String? = null, days: Int): String {
        val calendar = Calendar.getInstance()
        baseDate?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = dateFormat.parse(it) ?: Date()
        }
        calendar.add(Calendar.DATE, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun saveInvoice() {
        val tenantId = binding.editTenantId.text.toString().toIntOrNull()
        if (tenantId == null || itemsList.isEmpty()) {
            Toast.makeText(requireContext(), "Please complete all fields and add items.", Toast.LENGTH_SHORT).show()
            return
        }
        // Use computedTotal to ensure consistency between the displayed total and the saved invoice.
        val invoiceTotal = itemsList.sumOf { it.computedTotal }
        val invoice = InvoiceEntity(
            invoiceId = 0,
            invoiceNumber = generatedInvoiceNumber,
            invoiceDate = binding.editInvoiceDate.text.toString(),
            receivableDate = binding.editReceivableDate.text.toString(),
            tenantId = tenantId,
            tenantName = binding.editTenantName.text.toString(),
            invoiceAmount = invoiceTotal,
            invoiceAmountDue = invoiceTotal,
            invoiceNotes = binding.editNotesId.text.toString(),
            status = "Unpaid"
        )
        invoiceViewModel.saveInvoiceWithItems(invoice, itemsList)
        tenantsViewModel.addDebitToTenant(tenantId, invoice.invoiceAmount)
        invoicableId?.let { id ->
            invoicablesViewModel.updateInvoicableData(id)
        }
        Toast.makeText(requireContext(), "Invoice saved successfully!", Toast.LENGTH_SHORT).show()
        navigateToInvoicePdf(false)
    }

    private fun navigateToInvoicePdf(isPreviewOnly: Boolean) {
        val tenantId = binding.editTenantId.text.toString().toIntOrNull()
        if (tenantId == null || itemsList.isEmpty()) {
            Toast.makeText(requireContext(), "Please complete all fields and add items.", Toast.LENGTH_SHORT).show()
            return
        }
        val invoice = InvoiceEntity(
            invoiceId = 0,
            invoiceNumber = generatedInvoiceNumber,
            invoiceDate = binding.editInvoiceDate.text.toString(),
            receivableDate = binding.editReceivableDate.text.toString(),
            tenantId = tenantId,
            tenantName = binding.editTenantName.text.toString(),
            invoiceAmount = itemsList.sumOf { it.computedTotal },
            invoiceAmountDue = itemsList.sumOf { it.computedTotal },
            invoiceNotes = binding.editNotesId.text.toString()
        )
        val action = AddInvoiceFragmentDirections.actionAddInvoiceFragmentToInvoicePreviewFragment(
            templateName = "invoice_template_default",
            invoice = invoice,
            items = itemsList.toTypedArray(),
            isPreviewOnly = isPreviewOnly
        )
        findNavController().navigate(action)
    }

    private fun fromInvoicable(tenantId: Int?, tenantName: String?, nextBillingDate: String?) {
        val invoiceDate = nextBillingDate ?: getCurrentDate()
        val receivableDate = getFutureDate(invoiceDate, 10)
        binding.editInvoiceDate.setText(invoiceDate)
        binding.editReceivableDate.setText(receivableDate)
        binding.editTenantName.setText(tenantName ?: "")
        binding.editTenantId.setText(tenantId?.toString() ?: "")
        tenantId?.let { generateInvoiceNumber(it) }
    }

    private fun autoFillFields() {
        val currentDate = getCurrentDate()
        val receivableDate = getFutureDate(currentDate, 10)
        binding.editInvoiceDate.setText(currentDate)
        binding.editReceivableDate.setText(receivableDate)
    }

    private fun onItemUpdated(updatedItem: InvoiceItemEntity) {
        itemsList.find { it.invoiceItemId == updatedItem.invoiceItemId }?.apply {
            itemDescription = updatedItem.itemDescription
            itemRate = updatedItem.itemRate
        }
        adapter.notifyDataSetChanged()
        updateTotalAmount()
    }

    private fun onItemDeleted(position: Int) {
        if (position in itemsList.indices) {
            itemsList.removeAt(position)
            adapter.notifyItemRemoved(position)
            updateTotalAmount()
        }
    }

    private fun generateInvoiceNumber(tenantId: Int) {
        val date = getCurrentDate().replace("-", "")
        val uniqueSuffix = (System.currentTimeMillis() % 1000).toString().padStart(3, '0')
        generatedInvoiceNumber = "INV-$tenantId-$date-$uniqueSuffix"
        binding.editInvoiceNumber.setText(generatedInvoiceNumber)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
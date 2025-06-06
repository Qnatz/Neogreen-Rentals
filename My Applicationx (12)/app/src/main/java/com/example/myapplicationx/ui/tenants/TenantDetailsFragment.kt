package com.example.myapplicationx.ui.tenants

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.databinding.FragmentTenantDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplicationx.ui.accounts.invoicables.InvoicablesViewModel
import com.example.myapplicationx.ui.buildings.HousesViewModel

@AndroidEntryPoint
class TenantDetailsFragment : Fragment() {

    private var _binding: FragmentTenantDetailsBinding? = null
    private val binding get() = _binding!!
    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val housesViewModel: HousesViewModel by viewModels()
    private val invoicablesViewModel: InvoicablesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true) // Enable menu options

        // Retrieve tenant from arguments safely
        val tenant = arguments?.getParcelable<TenantEntity>("tenant")
        tenant?.let {
            setupUI(it)
            setupButtonListeners(it)
        } ?: showError()

        // Listen for result from EditTenantFragment
        setFragmentResultListener("tenantUpdated") { _, bundle ->
            val updatedTenant = bundle.getParcelable<TenantEntity>("updatedTenant")
            updatedTenant?.let { setupUI(it) }
        }
    }

    private fun setupUI(tenant: TenantEntity) {
        displayTenantDetails(tenant)
       // displayBillingInfo(tenant)
        updateVacateButtonVisibility(tenant)
    }

    private fun setupButtonListeners(tenant: TenantEntity) {
        binding.vacatedButton.setOnClickListener {
            val currentDate = getCurrentDate()
            updateTenantVacatedDate(tenant, currentDate)
        }
    }

    private fun updateVacateButtonVisibility(tenant: TenantEntity) {
        binding.vacatedButton.visibility =
            if (tenant.dateVacated == null) View.VISIBLE else View.GONE
    }

    private fun updateTenantVacatedDate(tenant: TenantEntity, date: String) {
        val updatedTenant = tenant.copy(dateVacated = date)
        tenantsViewModel.updateTenant(updatedTenant)
        houseVacated(tenant)
        stopBilling(tenant)
        setupUI(updatedTenant) // Refresh UI
    }
    
    private fun houseVacated(tenant: TenantEntity){
        val houseId = tenant.houseId
        val occupied = 0
        val vacant = 1
        housesViewModel.houseVacated(houseId, occupied, vacant)
    }
    
    private fun stopBilling(tenant: TenantEntity){
        val tenantId = tenant.tenantId
        invoicablesViewModel.deleteInvoicable(tenantId)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tenant_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val tenant = arguments?.getParcelable<TenantEntity>("tenant")
        return when (item.itemId) {
            R.id.action_edit -> {
                tenant?.let {
                    val action = TenantDetailsFragmentDirections
                        .actionTenantDetailsFragmentToEditTenantFragment(it)
                    findNavController().navigate(action)
                }
                true
            }
            R.id.action_delete -> {
               showDeleteTenantDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayTenantDetails(tenant: TenantEntity) {
        binding.apply {
            tenantName.text = tenant.tenantName
            primaryPhone.text = tenant.primaryPhone
            secondaryPhone.text = tenant.secondaryPhone ?: "N/A"
            email.text = tenant.email ?: "N/A"
            houseId.text = tenant.houseId.toString()
            houseName.text = tenant.houseName
            dateOccupied.text = "Date Occupied: ${tenant.dateOccupied}"
            dateVacated.text = "Date Vacated: ${tenant.dateVacated ?: "Still Occupying"}"
            balance.text = (tenant.creditBalance - tenant.debitBalance).toString()
            //debitBalance.text = 0.0
        }
    }
    
   private fun showDeleteTenantDialog() {
    val tenant = arguments?.getParcelable<TenantEntity>("tenant")
    if (tenant == null) {
        Toast.makeText(requireContext(), "Tenant not found", Toast.LENGTH_SHORT).show()
        return
    }

    val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
    builder.setTitle("Delete Tenant")
        .setMessage("Are you sure you want to delete this tenant?")
        .setPositiveButton("Delete") { dialog, _ ->
            // Delete the tenant
            tenantsViewModel.deleteTenant(tenant.tenantId)
            // Update the house occupancy to reflect vacancy
            houseVacated(tenant)
            Toast.makeText(requireContext(), "Tenant deleted successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

    val alertDialog = builder.create()
    alertDialog.setOnShowListener {
        val window = alertDialog.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setTextColor(requireContext().getColor(R.color.alert))
        negativeButton.setTextColor(requireContext().getColor(R.color.blue))
        }
    alertDialog.show()
    }

    /**
    private fun displayBillingInfo(tenant: TenantEntity) {
        binding.unpaidMonthsContainer.removeAllViews()
        var totalBalance = 0.0

        tenant.unpaidMonths.forEach { monthInfo ->
            val parts = monthInfo.split(": ")
            if (parts.size == 2) {
                val month = parts[0]
                val amount = parts[1].replace("UGX ", "").toDoubleOrNull() ?: 0.0
                totalBalance += amount

                val monthView = createTextView(month)
                val amountView = createTextView("UGX $amount")

                val billingItemLayout = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    addView(monthView)
                    addView(amountView)
                }

                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(
                        ResourcesCompat.getColor(resources, R.color.blue, null)
                    )
                }

                binding.unpaidMonthsContainer.apply {
                    addView(billingItemLayout)
                    addView(divider)
                }
            }
        }
        binding.balance.text = "Total Balance: UGX $totalBalance"
    }*/

    private fun createTextView(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 15f
            setTextColor(ResourcesCompat.getColor(resources, R.color.blue, null))
            typeface = ResourcesCompat.getFont(requireContext(), R.font.poppins_light)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, 8, 0, 8)
        }
    }

    private fun showError() {
        binding.noBillsDue.text = getString(R.string.error_tenant_not_found)
    }

    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
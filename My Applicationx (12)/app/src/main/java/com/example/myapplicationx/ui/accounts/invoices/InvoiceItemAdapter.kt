package com.example.myapplicationx.ui.accounts.invoices

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.InvoiceItemEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.ServiceWithTaxCrossRefs
import com.example.myapplicationx.database.model.TaxDetail
import com.example.myapplicationx.databinding.ViewInvoiceItemBinding

class InvoiceItemAdapter(
    private val items: MutableList<InvoiceItemEntity>,
    private val availableServices: List<ServiceWithTaxCrossRefs>,
    private val onItemUpdated: (InvoiceItemEntity) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<InvoiceItemAdapter.InvoiceItemViewHolder>() {

    inner class InvoiceItemViewHolder(val binding: ViewInvoiceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InvoiceItemEntity) {
            with(binding) {
                tvDescription.text = item.itemDescription
                itemUnitsEditText.setText(item.itemUnits.toString())
                itemUnitPriceEditText.setText(item.itemUnitPrice.toString())
                itemRateEditText.setText(item.itemRate.toString())
                computedTotalTextView.text = "Total: ${String.format("%.2f", item.computedTotal)}"
                binding.root.background = null

                // Add tax validation visual feedback
               /** if (hasDuplicateTaxes(item)) {
                    binding.root.setBackgroundColor(Color.parseColor("#FFFFE0")) // Light yellow
                } else {
                    binding.root.background = null
                }*/

                // Try to find the related service using serviceId
                var relatedService = availableServices.find { it.service.serviceId == item.serviceId }
                // If not found and the description is "Rent", fallback to a service whose name is "Rent"
                if (relatedService == null && item.itemDescription.equals("rent", ignoreCase = true)) {
                    relatedService = availableServices.find {
                        it.service.serviceName.equals("rent", ignoreCase = true)
                    }
                }

                if (relatedService != null) {
                    val taxDetails = getTaxDetailsForInvoiceItem(item, relatedService)
                    if (taxDetails.isNotEmpty()) {
                        rvInvoiceTaxes.visibility = View.VISIBLE
                        rvInvoiceTaxes.layoutManager = LinearLayoutManager(root.context)
                        rvInvoiceTaxes.adapter = ItemTaxesAdapter(taxDetails)
                    } else {
                        rvInvoiceTaxes.visibility = View.GONE
                    }
                } else {
                    rvInvoiceTaxes.visibility = View.GONE
                }

                deleteItemButton.setOnClickListener {
                    onItemDeleted(adapterPosition)
                }
            }
        }

        private fun hasDuplicateTaxes(item: InvoiceItemEntity): Boolean {
            val serviceWithTaxes = availableServices.find { it.service.serviceId == item.serviceId }
            serviceWithTaxes?.let {
                val taxNames = it.taxCrossRefs.map { ref -> ref.taxName }
                return taxNames.size != taxNames.distinct().size
            }
            return false
        }

        private fun getTaxDetailsForInvoiceItem(
            item: InvoiceItemEntity,
            serviceWithTaxes: ServiceWithTaxCrossRefs
        ): List<TaxDetail> {
            val serviceId = item.serviceId ?: return emptyList()

            // Group taxes by name and sum percentages
            val groupedTaxes = serviceWithTaxes.taxCrossRefs
                .groupBy { it.taxName }
                .map { (name, refs) ->
                    val totalPercentage = refs.sumOf { it.taxPercentage ?: 0.0 }
                    val isInclusive = refs.any { it.isInclusive }

                    ServiceTaxCrossRef(
                        serviceId = serviceId,
                        taxId = refs.first().taxId,
                        taxPercentage = totalPercentage,
                        isInclusive = isInclusive,
                        taxName = name
                    )
                }

            return groupedTaxes.map { taxRef ->
                val baseAmount = when {
                    taxRef.isInclusive -> item.computedTotal / (1 + (taxRef.taxPercentage / 100))
                    else -> item.computedNetBase
                }

                val taxAmount = baseAmount * (taxRef.taxPercentage / 100)

                TaxDetail(
                    taxName = taxRef.taxName,
                    isInclusive = taxRef.isInclusive,
                    taxPercentage = taxRef.taxPercentage,
                    taxAmount = taxAmount
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceItemViewHolder {
        val binding = ViewInvoiceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvoiceItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: InvoiceItemEntity) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun updateItem(position: Int, updatedItem: InvoiceItemEntity) {
        if (position in items.indices) {
            items[position] = updatedItem
            notifyItemChanged(position)
        }
    }

    fun deleteItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
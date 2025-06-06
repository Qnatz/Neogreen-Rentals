package com.example.myapplicationx.ui.accounts.invoices

import android.view.LayoutInflater
import android.view.ViewGroup
import java.text.DecimalFormat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.InvoiceItemEntity
import com.example.myapplicationx.databinding.ItemInvoicePdfBinding

class InvoiceItemPdfAdapter(
    private val items: List<InvoiceItemEntity>
) : RecyclerView.Adapter<InvoiceItemPdfAdapter.InvoiceItemViewHolder>() {

    inner class InvoiceItemViewHolder(val binding: ItemInvoicePdfBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InvoiceItemEntity) {
            binding.descriptionItem.text = item.itemDescription
            val decimalFormat = DecimalFormat("#.00")
            binding.itemUnits.text = decimalFormat.format(item.itemUnits).toString()
            binding.unitPrice.text = decimalFormat.format(item.itemUnitPrice).toString()
            binding.rateItem.text = decimalFormat.format(item.computedNetBase).toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceItemViewHolder {
        val binding = ItemInvoicePdfBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InvoiceItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
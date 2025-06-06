package com.example.myapplicationx.ui.accounts.invoices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.InvoiceWithItems
import com.example.myapplicationx.databinding.ItemInvoiceBinding

class InvoicesAdapter(
    private val onEditClick: (InvoiceWithItems) -> Unit,
    private val onDeleteClick: (InvoiceWithItems) -> Unit,
    private val onRowClick: (InvoiceWithItems) -> Unit
) : ListAdapter<InvoiceWithItems, InvoicesAdapter.InvoiceViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<InvoiceWithItems>() {
        override fun areItemsTheSame(oldItem: InvoiceWithItems, newItem: InvoiceWithItems) =
            oldItem.invoice.invoiceNumber == newItem.invoice.invoiceNumber

        override fun areContentsTheSame(oldItem: InvoiceWithItems, newItem: InvoiceWithItems) =
            oldItem == newItem
    }

    inner class InvoiceViewHolder(private val binding: ItemInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InvoiceWithItems) {
            binding.apply {
                textTenantName.text = item.invoice.tenantName
                textInvoiceNumber.text = item.invoice.invoiceNumber
                textInvoiceDate.text = item.invoice.invoiceDate
                textInvoiceAmount.text = "UGx ${item.invoice.invoiceAmount}"
                textInvoiceDue.text = "Due: UGx ${item.invoice.invoiceAmountDue}"

                // Row click
                root.setOnClickListener { onRowClick(item) }
                // Icon clicks
                buttonEdit.setOnClickListener { onEditClick(item) }
                buttonDelete.setOnClickListener { onDeleteClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
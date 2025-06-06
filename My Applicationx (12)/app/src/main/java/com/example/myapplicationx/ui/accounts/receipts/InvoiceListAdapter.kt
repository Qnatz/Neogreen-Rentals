package com.example.myapplicationx.ui.accounts.receipts

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.ReceiptInvoice
import com.example.myapplicationx.databinding.ItemReceiptInvoiceBinding

class InvoiceListAdapter(
    private val onActionClick: (ReceiptInvoice) -> Unit
) : ListAdapter<ReceiptInvoice, InvoiceListAdapter.ViewHolder>(InvoiceDiffCallback()) {

    inner class ViewHolder(val binding: ItemReceiptInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(invoice: ReceiptInvoice) {
            binding.apply {
                tvInvoiceNumber.text = invoice.invoiceNumber
                tvInvoiceDate.text = invoice.invoiceDate
                tvAmountDue.text = "Balance: ${invoice.remainingBalance}"

                // Toggle selection state
                if (invoice.isSelected) {
                    btnAdd.text = "Remove"
                    btnAdd.setBackgroundColor(Color.RED)
                } else {
                    btnAdd.text = "Add"
                    btnAdd.setBackgroundColor(Color.GREEN)
                }

                // Status colors
                when (invoice.invoiceStatus?.lowercase()) {
                    "unpaid" -> tvAmountDue.setTextColor(Color.RED)
                    "partial" -> tvAmountDue.setTextColor(Color.parseColor("#FFA500"))
                    "paid" -> tvAmountDue.setTextColor(Color.GREEN)
                }

                btnAdd.setOnClickListener { onActionClick(invoice) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceiptInvoiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InvoiceDiffCallback : DiffUtil.ItemCallback<ReceiptInvoice>() {
        override fun areItemsTheSame(oldItem: ReceiptInvoice, newItem: ReceiptInvoice): Boolean {
            return oldItem.invoiceNumber == newItem.invoiceNumber
        }

        override fun areContentsTheSame(oldItem: ReceiptInvoice, newItem: ReceiptInvoice): Boolean {
            return oldItem == newItem
        }
    }
}
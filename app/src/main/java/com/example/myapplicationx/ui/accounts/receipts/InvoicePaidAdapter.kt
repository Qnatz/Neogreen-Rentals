package com.example.myapplicationx.ui.accounts.receipts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.ReceiptInvoice
import com.example.myapplicationx.databinding.ItemInvoicePaidBinding

class InvoicePaidAdapter(
    private val onDeleteClick: (ReceiptInvoice) -> Unit,
    private val onItemClick: (ReceiptInvoice) -> Unit,
    private val showDeleteButton: Boolean = true
) : ListAdapter<ReceiptInvoice, InvoicePaidAdapter.PaidViewHolder>(InvoiceDiffCallback()) {

    inner class PaidViewHolder(val binding: ItemInvoicePaidBinding) : 
        RecyclerView.ViewHolder(binding.root) {
            
        fun bind(invoice: ReceiptInvoice) {
            // Updated binding logic to use ReceiptInvoice properties
            binding.tvInvoiceNumber.text = invoice.invoiceNumber
            binding.tvInvoiceDate.text = invoice.invoiceDate
            
            // Make sure this property exists in ReceiptInvoice
            binding.tvAmountPaid.text = invoice.amountPaid.toString()
            
            // Use visibility constants from View instead of ViewGroup
            if (showDeleteButton) {
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDelete.setOnClickListener {
                    onDeleteClick(invoice)
                }
            } else {
                binding.btnDelete.visibility = View.GONE
            }
            
            // Set click listener for the entire item
            binding.root.setOnClickListener {
                onItemClick(invoice)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaidViewHolder {
        val binding = ItemInvoicePaidBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaidViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaidViewHolder, position: Int) {
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
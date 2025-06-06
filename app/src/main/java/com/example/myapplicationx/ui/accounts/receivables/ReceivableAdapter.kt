package com.example.myapplicationx.ui.accounts.receivables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.ReceivableEntity
import com.example.myapplicationx.databinding.ItemReceivableBinding
import java.text.NumberFormat
import java.util.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ReceivableAdapter(
    private val onItemClick: (ReceivableEntity) -> Unit
) : ListAdapter<ReceivableEntity, ReceivableAdapter.ReceivableViewHolder>(ReceivableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivableViewHolder {
        val binding = ItemReceivableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceivableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceivableViewHolder, position: Int) {
        val receivable = getItem(position)
        holder.bind(receivable)
    }

    inner class ReceivableViewHolder(private val binding: ItemReceivableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(receivable: ReceivableEntity) {
            binding.apply {
                tvInvoiceNumber.text = receivable.invoiceNumber
                tvTenantName.text = receivable.tenantName
                tvReceivableDate.text = readableDateString(receivable.dateReceivable)
                tvAmountDue.text = formatCurrency(receivable.amountDue)

                // Determine the text color based on the receivable state
                val textColor = when {
                    receivable.isOverdue -> ContextCompat.getColor(root.context, R.color.red)
                    receivable.isPending -> ContextCompat.getColor(root.context, R.color.blue)
                    else -> ContextCompat.getColor(root.context, R.color.black)
                }

                // Apply the text color to each relevant TextView
                tvInvoiceNumber.setTextColor(textColor)
                tvTenantName.setTextColor(textColor)
                tvReceivableDate.setTextColor(textColor)
                tvAmountDue.setTextColor(textColor)

                root.setOnClickListener { onItemClick(receivable) }
            }
        }

        private fun formatCurrency(amount: Double): String {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return currencyFormat.format(amount)
        }
    }
    
    fun readableDateString(epochMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(epochMillis))
    }

    // DiffUtil callback for optimizing list updates
    class ReceivableDiffCallback : DiffUtil.ItemCallback<ReceivableEntity>() {
        override fun areItemsTheSame(oldItem: ReceivableEntity, newItem: ReceivableEntity): Boolean {
            return oldItem.invoiceNumber == newItem.invoiceNumber // Assuming invoiceNumber is unique
        }

        override fun areContentsTheSame(oldItem: ReceivableEntity, newItem: ReceivableEntity): Boolean {
            return oldItem == newItem
        }
    }
}
package com.example.myapplicationx.ui.accounts.invoicables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.InvoicableEntity
import com.example.myapplicationx.databinding.ItemInvoicableBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class InvoicablesAdapter(
    private var invoicables: List<InvoicableEntity>,
    private val onInvoicableClick: (InvoicableEntity) -> Unit,
) : RecyclerView.Adapter<InvoicablesAdapter.InvoicableViewHolder>() {
    class InvoicableViewHolder(
        val binding: ItemInvoicableBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): InvoicableViewHolder {
        val binding =
            ItemInvoicableBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return InvoicableViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: InvoicableViewHolder,
        position: Int,
    ) {
        val invoicable = invoicables[position]
        with(holder.binding) {
            tenantName.text = invoicable.tenantName
            nextBillingDate.text = convertToReadableDate(invoicable.nextBillingDate.toString())
            root.setOnClickListener {
                onInvoicableClick(invoicable)
            }
        }
    }

    /**
     * Converts an epoch timestamp (in milliseconds) in String format into a human‑readable date.
     */
    fun convertToReadableDate(timestampStr: String): String {
        val timestamp = timestampStr.toLongOrNull() ?: return "Invalid timestamp"
        val localDate =
            Instant
                .ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        return localDate.format(formatter)
    }

    override fun getItemCount() = invoicables.size

    fun updateInvoicables(newInvoicables: List<InvoicableEntity>) {
        invoicables = newInvoicables
        notifyDataSetChanged()
    }
}

package com.example.myapplicationx.ui.accounts.invoices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.databinding.ViewTaxItemBinding
import com.example.myapplicationx.database.model.TaxDetail


class ItemTaxesAdapter(
    private val taxDetails: List<TaxDetail>
) : RecyclerView.Adapter<ItemTaxesAdapter.TaxViewHolder>() {

    inner class TaxViewHolder(val binding: ViewTaxItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tax: TaxDetail) {
            with(binding) {
                // Display tax name with type indicator
                taxNameTextView.text = buildString {
                    append(tax.taxName)
                    if (tax.isInclusive) append(" (Inclusive)")
                }

                // Show tax percentage and amount
                taxPercentageTextView.text = "${tax.taxPercentage}%"
                taxAmountTextView.text = String.format("%.2f", tax.taxAmount)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxViewHolder {
        val binding = ViewTaxItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaxViewHolder, position: Int) {
        val tax = taxDetails[position]
        holder.bind(tax)
    }

    override fun getItemCount(): Int = taxDetails.size
}
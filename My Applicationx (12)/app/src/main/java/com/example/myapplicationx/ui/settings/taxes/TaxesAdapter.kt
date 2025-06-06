package com.example.myapplicationx.ui.settings.taxes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.databinding.ItemTaxBinding

class TaxAdapter(
    private val onEdit: (TaxEntity) -> Unit,
    private val onDelete: (TaxEntity) -> Unit
) : ListAdapter<TaxEntity, TaxAdapter.TaxViewHolder>(TaxDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxViewHolder {
        val binding = ItemTaxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaxViewHolder, position: Int) {
        val tax = getItem(position)
        holder.bind(tax)
    }

    inner class TaxViewHolder(private val binding: ItemTaxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tax: TaxEntity) {
            binding.apply {
                taxName.text = tax.taxName
                taxPercentage.text = "${tax.taxPercentage}%"
                editButton.setOnClickListener { onEdit(tax) }
                deleteButton.setOnClickListener { onDelete(tax) }
            }
        }
    }
}

class TaxDiffCallback : DiffUtil.ItemCallback<TaxEntity>() {
    override fun areItemsTheSame(oldItem: TaxEntity, newItem: TaxEntity): Boolean {
        return oldItem.taxId == newItem.taxId
    }

    override fun areContentsTheSame(oldItem: TaxEntity, newItem: TaxEntity): Boolean {
        return oldItem == newItem
    }
}
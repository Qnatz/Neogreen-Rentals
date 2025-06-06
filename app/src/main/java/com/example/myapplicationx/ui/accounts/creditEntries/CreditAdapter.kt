package com.example.myapplicationx.ui.accounts.creditEntries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.databinding.ItemCreditBinding
import com.example.myapplicationx.database.model.CreditEntryEntity

class CreditAdapter(
    private val onEdit: (CreditEntryEntity) -> Unit,
    private val onDelete: (CreditEntryEntity) -> Unit,
    private val onItemClick: (CreditEntryEntity) -> Unit
) : ListAdapter<CreditEntryEntity, CreditAdapter.CreditViewHolder>(CreditDiffCallback()) {

    inner class CreditViewHolder(private val binding: ItemCreditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CreditEntryEntity) {
            with(binding) {
                tenantName.text = credit.tenantName
                creditDate.text = credit.creditDate
                creditAmount.text = "UGX ${credit.amount}"

                // Button click listeners
                editButton.setOnClickListener { onEdit(credit) }
                deleteButton.setOnClickListener { onDelete(credit) }
                
                // Entire item click listener
                root.setOnClickListener { onItemClick(credit) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val binding = ItemCreditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CreditDiffCallback : DiffUtil.ItemCallback<CreditEntryEntity>() {
        override fun areItemsTheSame(oldItem: CreditEntryEntity, newItem: CreditEntryEntity): Boolean {
            return oldItem.creditEntryId == newItem.creditEntryId
        }

        override fun areContentsTheSame(oldItem: CreditEntryEntity, newItem: CreditEntryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
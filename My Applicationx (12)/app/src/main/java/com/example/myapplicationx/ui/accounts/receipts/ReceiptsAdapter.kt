package com.example.myapplicationx.ui.accounts.receipts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.ReceiptEntity
import com.example.myapplicationx.databinding.ItemReceiptBinding
import com.example.myapplicationx.ui.accounts.receipts.ReceiptsFragmentDirections

class ReceiptsAdapter(
    private val onItemClick: (ReceiptEntity) -> Unit,
    private val onEditClick: (ReceiptEntity) -> Unit,
    private val onDeleteClick: (ReceiptEntity) -> Unit
) : ListAdapter<ReceiptEntity, ReceiptsAdapter.ReceiptViewHolder>(ReceiptDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ItemReceiptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val receipt = getItem(position)
        holder.bind(receipt, onItemClick, onEditClick, onDeleteClick)
    }

    class ReceiptViewHolder(
        private val binding: ItemReceiptBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            receipt: ReceiptEntity,
            onItemClick: (ReceiptEntity) -> Unit,
            onEditClick: (ReceiptEntity) -> Unit,
            onDeleteClick: (ReceiptEntity) -> Unit
        ) {
            binding.textViewTenantName.text = "${receipt.tenantName}"
            binding.textViewReceiptNumber.text = "${receipt.receiptNumber}"
            binding.textViewAmount.text = "UGx ${receipt.amountReceived}"
            binding.textViewDate.text = "${receipt.receiptDate}"
            // Set click listeners
            binding.root.setOnClickListener { onItemClick(receipt) }
            binding.btnEdit.setOnClickListener { onEditClick(receipt) }
            binding.btnDelete.setOnClickListener { onDeleteClick(receipt) }
        }
    }
}

class ReceiptDiffCallback : DiffUtil.ItemCallback<ReceiptEntity>() {
    override fun areItemsTheSame(oldItem: ReceiptEntity, newItem: ReceiptEntity): Boolean {
        return oldItem.receiptId == newItem.receiptId
    }

    override fun areContentsTheSame(oldItem: ReceiptEntity, newItem: ReceiptEntity): Boolean {
        return oldItem == newItem
    }
}
package com.example.myapplicationx.ui.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.ItemAccountCardBinding

class AccountsAdapter(
    private val onCardClick: (AccountsSection) -> Unit
) : RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>() {

    private val sections = listOf(
        AccountsSection("Invoicables", R.drawable.oak_tree_villa, "These are invoices pending or due or overdue"),
        AccountsSection("Receivables", R.drawable.birchwood_cottage, "These are receivables pending or due or overdue"),
        AccountsSection("Invoices", R.drawable.cedar_lodge, "This shows a list of all invoices and can be edited or deleted"),
        AccountsSection("Receipts", R.drawable.maple_house, "This shows a list of all receipts and can be edited or deleted"),
       AccountsSection("Credit", R.drawable.pine_crest, "This shows a list of all credit entries and can be edited or deleted")
    )
    

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(sections[position])
    }

    override fun getItemCount() = sections.size

    inner class AccountViewHolder(private val binding: ItemAccountCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: AccountsSection) {
            binding.sectionTitleTextView.text = section.title
            binding.iconImageView.setImageResource(section.iconRes)
            binding.sectionDescriptionTextView.text = section.desc
            binding.root.setOnClickListener { onCardClick(section) }
        }
    }

    data class AccountsSection(val title: String, val iconRes: Int, val desc: String)
}
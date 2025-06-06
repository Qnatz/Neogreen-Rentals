package com.example.myapplicationx.ui.tenants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.ui.tenants.TenantsFragmentDirections
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat


class TenantsAdapter :
    ListAdapter<TenantEntity, TenantsAdapter.TenantViewHolder>(TenantDiffCallback()) {

    inner class TenantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tenantName: TextView = view.findViewById(R.id.tenant_name)
        val amountDue: TextView = view.findViewById(R.id.amount_due)

        // Method to bind data to the view
        fun bind(tenant: TenantEntity) {
            tenantName.text = tenant.tenantName
        
            val context = itemView.context
        
            when {
                // Case 1: Both balances are zero → "No Balance" in green
                tenant.debitBalance == 0.0 && tenant.creditBalance == 0.0 -> {
                    amountDue.text = "No Balance"
                    amountDue.setTextColor(ContextCompat.getColor(context, R.color.green))
                }
                // Case 2: Debit is zero but credit has a value → Show credit balance with a plus sign in green
                tenant.debitBalance == 0.0 && tenant.creditBalance != 0.0 -> {
                    amountDue.text = "+UGX ${tenant.creditBalance}"
                    amountDue.setTextColor(ContextCompat.getColor(context, R.color.green))
                }
                // Case 3: Credit is zero but debit has a value → Show debit balance with a minus sign in red
                tenant.debitBalance != 0.0 && tenant.creditBalance == 0.0 -> {
                    amountDue.text = "-UGX ${tenant.debitBalance}"
                    amountDue.setTextColor(ContextCompat.getColor(context, R.color.red))
                }
                // Case 4: Both values are non-zero → Display both (e.g. "-UGX 38880 / +UGX 49000")
                tenant.debitBalance != 0.0 && tenant.creditBalance != 0.0 -> {
                    val debitText = "-UGX ${tenant.debitBalance}"
                    val creditText = "+UGX ${tenant.creditBalance}"
                    val combinedText = "$debitText / $creditText"
                    val spannable = SpannableString(combinedText)
        
                    // Color the debit portion in red
                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)),
                        0,
                        debitText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
        
                    // Color the credit portion in green
                    val creditStart = combinedText.indexOf(creditText)
                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.green)),
                        creditStart,
                        combinedText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    amountDue.text = spannable
                }
            }
        
            // Handle click navigation with Safe Args
            itemView.setOnClickListener {
                val action = TenantsFragmentDirections.actionNavigationTenantsToTenantDetailsFragment(tenant)
                it.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tenant, parent, false)
        return TenantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        val tenant = getItem(position)
        holder.bind(tenant)  // Bind the tenant data to the view holder
    }

    // DiffUtil for efficient list updates
    class TenantDiffCallback : DiffUtil.ItemCallback<TenantEntity>() {
        override fun areItemsTheSame(oldItem: TenantEntity, newItem: TenantEntity): Boolean {
            return oldItem.tenantId == newItem.tenantId  // Compare by ID
        }

        override fun areContentsTheSame(oldItem: TenantEntity, newItem: TenantEntity): Boolean {
            return oldItem == newItem  // Compare content equality
        }
    }
}
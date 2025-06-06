package com.example.myapplicationx.ui.settings.services

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.databinding.ItemSelectedTaxBinding

class SelectedTaxesAdapter(
    private val selectedTaxes: MutableList<ServiceTaxCrossRef>,
    private val onDelete: (Int) -> Unit,
    // Callback now expects a non-null Double.
    private val onInclusiveChanged: (Int, Boolean, Double) -> Unit
) : RecyclerView.Adapter<SelectedTaxesAdapter.TaxViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxViewHolder {
        val binding = ItemSelectedTaxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaxViewHolder, position: Int) {
        holder.bind(selectedTaxes[position])
    }

    override fun getItemCount(): Int = selectedTaxes.size

    inner class TaxViewHolder(private val binding: ItemSelectedTaxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Keep a reference to the current TextWatcher so it can be removed when rebinding.
        private var currentTextWatcher: TextWatcher? = null

        fun bind(tax: ServiceTaxCrossRef) {
            binding.taxNameTextView.text = tax.taxName
            binding.taxPercentageTextView.setText(tax.taxPercentage?.toString() ?: "0.0")

            // Determine if taxPercentage is fixed (non-zero).
            val isReadOnly = (tax.taxPercentage ?: 0.0) != 0.0

            // Disable editing if the tax rate is fixed.
            binding.taxPercentageTextView.apply {
                isEnabled = !isReadOnly
                isFocusable = !isReadOnly
                isFocusableInTouchMode = !isReadOnly
            }

            // Set up the inclusive switch.
            binding.inclusiveSwitch.setOnCheckedChangeListener(null)
            binding.inclusiveSwitch.isChecked = tax.isInclusive
            binding.inclusiveSwitch.setOnCheckedChangeListener { _, isChecked ->
                // For fixed taxes, use the non-null tax.taxPercentage (or 0.0 if null).
                val currentVal = if (isReadOnly) (tax.taxPercentage ?: 0.0)
                else binding.taxPercentageTextView.text.toString().toDoubleOrNull() ?: 0.0
                onInclusiveChanged(bindingAdapterPosition, isChecked, currentVal)
            }

            // Set up the delete button.
            binding.deleteButton.setOnClickListener {
                val adapterPos = bindingAdapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    onDelete(adapterPos)
                }
            }

            // Remove any previously attached TextWatcher.
            currentTextWatcher?.let { binding.taxPercentageTextView.removeTextChangedListener(it) }
            currentTextWatcher = null

            if (!isReadOnly) {
                // Update the underlying data silently during text changes.
                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val adapterPos = bindingAdapterPosition
                        if (adapterPos != RecyclerView.NO_POSITION) {
                            val input = s?.toString()
                            val overrideValue = if (input.isNullOrBlank()) 0.0 else input.toDoubleOrNull() ?: 0.0
                            // Update the underlying model without notifying the adapter.
                            selectedTaxes[adapterPos] = selectedTaxes[adapterPos].copy(taxPercentage = overrideValue)
                        }
                    }
                    override fun afterTextChanged(s: Editable?) { }
                }
                currentTextWatcher = watcher
                binding.taxPercentageTextView.addTextChangedListener(watcher)

                // Add a focus change listener so that when editing finishes, we update the view.
                binding.taxPercentageTextView.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        // Editing is complete; refresh the view.
                        this@SelectedTaxesAdapter.notifyItemChanged(bindingAdapterPosition)
                    }
                }
            } else {
                // Remove focus listener for read-only fields.
                binding.taxPercentageTextView.onFocusChangeListener = null
            }
        }
    }
}
package com.example.myapplicationx.ui.accounts.invoicables

import android.os.Bundle
import android.view.*
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.databinding.FragmentInvoicablesBinding
import com.example.myapplicationx.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoicablesFragment : Fragment() {

    private var _binding: FragmentInvoicablesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InvoicablesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoicablesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // RecyclerView for invoicables with status "Dueup" (next billing date in the future)
        val pendingAdapter = InvoicablesAdapter(emptyList()) { invoicable ->
            // Create the Safe Args action to navigate to AddInvoiceFragment.
            val action = InvoicablesFragmentDirections.actionInvoicablesFragmentToAddInvoiceFragment(
                isFromInvoicable = true,
                invoicableId = invoicable.invoicableId ?: -1,
                tenantId = invoicable.tenantId ?: -1,
                tenantName = invoicable.tenantName ?: "",
                nextBillingDate = convertToReadableDate(invoicable.nextBillingDate.toString())
            )

            // Build a debug message to show the parameters and the action ID.
            val debugMessage = """
                Navigating to AddInvoiceFragment with:
                isFromInvoicable: true
                invoicableId: ${invoicable.invoicableId ?: -1}
                tenantId: ${invoicable.tenantId ?: -1}
                tenantName: ${invoicable.tenantName ?: ""}
                nextBillingDate: ${convertToReadableDate(invoicable.nextBillingDate.toString())}
                Action ID: ${action.actionId}
            """.trimIndent()

            // Display an AlertDialog to confirm the navigation values.
            AlertDialog.Builder(requireContext())
                .setTitle("Debug Navigation")
                .setMessage(debugMessage)
                .setPositiveButton("Proceed") { dialog, _ ->
                    dialog.dismiss()
                    findNavController().navigate(action)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.pendingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pendingAdapter
        }

        // RecyclerView for invoicables with status "Pending" (billing date is today or in the past)
        val dueUpAdapter = InvoicablesAdapter(emptyList()) { invoicable ->
            // Optionally, add a click listener for pending items.
        }
        binding.dueUpRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dueUpAdapter
        }

        observeInvoicables(dueUpAdapter, pendingAdapter)
    }

    private fun observeInvoicables(
        dueUpAdapter: InvoicablesAdapter,
        pendingAdapter: InvoicablesAdapter
    ) {
        viewModel.dueUpInvoicables.observe(viewLifecycleOwner) { invoicables ->
            dueUpAdapter.updateInvoicables(invoicables)
        }
        viewModel.pendingInvoicables.observe(viewLifecycleOwner) { invoicables ->
            pendingAdapter.updateInvoicables(invoicables)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_invoicables, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_invoicable -> {
                findNavController().navigate(R.id.action_invoicablesFragment_to_addInvoicableFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Converts an epoch timestamp (in milliseconds) in String format into a human‑readable date.
     */
    fun convertToReadableDate(timestampStr: String): String {
        val timestamp = timestampStr.toLongOrNull() ?: return "Invalid timestamp"
        val localDate = java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd", java.util.Locale.getDefault())
        return localDate.format(formatter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
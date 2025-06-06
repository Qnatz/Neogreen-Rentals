package com.example.myapplicationx.ui.accounts.receivables

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.ReceivableEntity
import com.example.myapplicationx.databinding.FragmentReceivableBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceivableFragment : Fragment() {

    private var _binding: FragmentReceivableBinding? = null
    private val binding get() = _binding!!

    private val receivableViewModel: ReceivableViewModel by viewModels()
    private lateinit var pendingAdapter: ReceivableAdapter
    private lateinit var overdueAdapter: ReceivableAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceivableBinding.inflate(inflater, container, false)
        setupRecyclerViews()
        observeViewModel()
        return binding.root
    }  // <-- onCreateView is now properly closed

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use MenuHost API instead of setHasOptionsMenu and onCreateOptionsMenu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_receivables, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle menu item selections here
                when (menuItem.itemId) {
                    R.id.action_add_receivable -> {
                        Log.d("ReceivableFragment", "Add Receivable clicked")
                        findNavController().navigate(R.id.action_receivableFragment_to_addReceivableFragment)
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerViews() {
        pendingAdapter = ReceivableAdapter { receivable ->
            onReceivableClick(receivable)
        }
        overdueAdapter = ReceivableAdapter { receivable ->
            onReceivableClick(receivable)
        }

        binding.pendingReceivablesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.pendingReceivablesRecyclerView.adapter = pendingAdapter

        binding.overdueReceivablesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.overdueReceivablesRecyclerView.adapter = overdueAdapter

        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.pendingReceivablesRecyclerView.addItemDecoration(divider)
        binding.overdueReceivablesRecyclerView.addItemDecoration(divider)

        Log.d("ReceivableFragment", "RecyclerViews setup complete")
    }

    private fun observeViewModel() {
        receivableViewModel.pendingReceivables.observe(viewLifecycleOwner) { receivables ->
            pendingAdapter.submitList(receivables)
            Log.d("ReceivableFragment", "Pending receivables updated: ${receivables.size}")
        }

        receivableViewModel.overdueReceivables.observe(viewLifecycleOwner) { receivables ->
            overdueAdapter.submitList(receivables)
            Log.d("ReceivableFragment", "Overdue receivables updated: ${receivables.size}")
        }
    }

    private fun onReceivableClick(receivable: ReceivableEntity) {
        Log.d("ReceivableFragment", "Receivable clicked: ${receivable.invoiceNumber}")
        findNavController().navigate(R.id.action_receivableFragment_to_addReceivableFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("ReceivableFragment", "View destroyed")
    }
}
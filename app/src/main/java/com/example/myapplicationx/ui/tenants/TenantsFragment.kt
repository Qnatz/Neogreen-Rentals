package com.example.myapplicationx.ui.tenants

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentTenantsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TenantsFragment : Fragment() {

    private var _binding: FragmentTenantsBinding? = null
    private val binding get() = _binding!!

    private val tenantsViewModel: TenantsViewModel by viewModels() // Hilt ViewModel injection
    private lateinit var adapter: TenantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enable options menu in this fragment
        Log.d("TenantsFragment", "onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        // Observe tenant data from ViewModel and update the adapter
        tenantsViewModel.tenants.observe(viewLifecycleOwner) { tenantList ->
            adapter.submitList(tenantList) // Submit the list using ListAdapter
            Log.d("TenantsFragment", "Tenant list updated with ${tenantList.size} tenants")
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = TenantsAdapter() // No need for an initial empty list
        binding.tenantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tenantsRecyclerView.adapter = adapter

        // Add item decoration to the RecyclerView
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        binding.tenantsRecyclerView.addItemDecoration(dividerItemDecoration)
        Log.d("TenantsFragment", "RecyclerView setup complete")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu) // Inflate toolbar menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_tenant -> {
                Log.d("TenantsFragment", "Add Tenant clicked")
                findNavController().navigate(R.id.addTenantFragment) // Navigate to AddTenantFragment
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("TenantsFragment", "View destroyed")
    }
}
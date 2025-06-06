package com.example.myapplicationx.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentAccountsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = AccountsAdapter { section ->
            when (section.title) {
                "Receivables" -> findNavController().navigate(R.id.action_navigation_accounts_to_receivableFragment)
                "Invoices" -> findNavController().navigate(R.id.action_navigation_accounts_to_invoicesFragment)
                "Receipts" -> findNavController().navigate(R.id.action_navigation_accounts_to_receiptsFragment)
                "Invoicables" -> findNavController().navigate(R.id.action_navigation_accounts_to_invoicablesFragment)
                "Credit" -> findNavController().navigate(R.id.action_navigation_accounts_to_creditListFragment)
            }
        }
        binding.accountsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.accountsRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
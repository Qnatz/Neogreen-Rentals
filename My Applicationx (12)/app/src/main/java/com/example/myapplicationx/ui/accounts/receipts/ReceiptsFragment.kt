package com.example.myapplicationx.ui.accounts.receipts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.databinding.FragmentReceiptsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.myapplicationx.database.model.ReceiptEntity


@AndroidEntryPoint
class ReceiptsFragment : Fragment() {

    private var _binding: FragmentReceiptsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReceiptsViewModel by viewModels()
    private lateinit var adapter: ReceiptsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReceiptsAdapter(
            onItemClick = { receipt: ReceiptEntity ->
                val action = ReceiptsFragmentDirections.actionReceiptsFragmentToReceiptDetailFragment(receipt.receiptId)
                findNavController().navigate(action)
            },
            onEditClick = { receipt: ReceiptEntity ->
                val action = ReceiptsFragmentDirections.actionReceiptsFragmentToEditReceiptFragment(receipt.receiptId)
                findNavController().navigate(action)
            },
            onDeleteClick = { receipt: ReceiptEntity ->
                viewModel.deleteReceiptById(receipt.receiptId)
                Toast.makeText(requireContext(), "Receipt Deleted", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvReceipts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReceipts.adapter = adapter

        // Observe receipt data via Flow
        lifecycleScope.launch {
            viewModel.allReceipts.collect { receipts ->
                adapter.submitList(receipts)
            }
        }

        // Add new receipt button action.
        binding.fabAddReceipt.setOnClickListener {
            findNavController().navigate(
                ReceiptsFragmentDirections.actionReceiptsFragmentToAddReceiptFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
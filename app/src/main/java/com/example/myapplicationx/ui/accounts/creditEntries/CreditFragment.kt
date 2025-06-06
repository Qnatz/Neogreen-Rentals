package com.example.myapplicationx.ui.accounts.creditEntries

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentCreditBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreditFragment : Fragment() {

    private var _binding: FragmentCreditBinding? = null
    private val binding get() = _binding!!

    private val creditViewModel: CreditViewModel by viewModels() // Hilt ViewModel injection
    private lateinit var adapter: CreditAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Options menu removed
        Log.d("CreditFragment", "onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditBinding.inflate(inflater, container, false)

        setupRecyclerView()

        // Observe credit data from ViewModel and update the adapter
        creditViewModel.allCreditEntries.observe(viewLifecycleOwner) { creditList ->
            adapter.submitList(creditList)
            Log.d("CreditFragment", "Credit list updated with ${creditList.size} entries")
        }

        // Set Floating Action Button (FAB) click listener to navigate to AddCreditFragment
        binding.fabAddCredit.setOnClickListener {
            findNavController().navigate(R.id.action_creditFragment_to_addCreditFragment)
            Log.d("CreditFragment", "Navigated to AddCreditFragment via FAB")
        }

        return binding.root
    }

    private fun setupRecyclerView() {
            adapter = CreditAdapter(
                onEdit = { creditEntry -> // Pass CreditEntryEntity
                    val action = CreditFragmentDirections.actionCreditFragmentToEditCreditFragment(creditEntry)
                    findNavController().navigate(action)
                },
                onDelete = { creditEntry -> // Pass CreditEntryEntity
                    creditViewModel.deleteCreditEntry(creditEntry)
                },
                onItemClick = { creditEntry -> // Pass CreditEntryEntity
                    val action = CreditFragmentDirections.actionCreditFragmentToCreditDetailsFragment(creditEntry)
                    findNavController().navigate(action)
                }
            )
        binding.creditEntriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.creditEntriesRecyclerView.adapter = adapter

        // Add item decoration to the RecyclerView
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.creditEntriesRecyclerView.addItemDecoration(dividerItemDecoration)
        Log.d("CreditFragment", "RecyclerView setup complete")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("CreditFragment", "View destroyed")
    }
}
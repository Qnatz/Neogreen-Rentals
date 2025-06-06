package com.example.myapplicationx.ui.accounts.creditEntries

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentCreditDetailsBinding
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class CreditDetailsFragment : Fragment() {

    private var _binding: FragmentCreditDetailsBinding? = null
    private val binding get() = _binding!!

    private val creditViewModel: CreditViewModel by viewModels()
    private val args: CreditDetailsFragmentArgs by navArgs() // Safe Args to receive CreditEntryEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enable options menu
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditDetailsBinding.inflate(inflater, container, false)

        val creditEntry = args.creditEntry

        // Bind data to UI
        binding.tenantName.text = creditEntry.tenantName
        binding.creditDate.text = creditEntry.creditDate
        binding.amount.text = "UGX ${creditEntry.amount}"
        binding.creditNote.text = creditEntry.creditNote

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_credit_details, menu) // Inflate the menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                // Navigate to EditCreditFragment with the current credit entry
                val action = CreditDetailsFragmentDirections
                    .actionCreditDetailsFragmentToEditCreditFragment(args.creditEntry)
                findNavController().navigate(action)
                return true
            }
            R.id.action_delete -> {
                // Delete the credit entry
                creditViewModel.deleteCreditEntry(args.creditEntry)
                findNavController().navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.myapplicationx.ui.settings.companyInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.myapplicationx.databinding.FragmentCompanyInfoBinding
import com.example.myapplicationx.database.model.CompanyInfoEntity
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log


@AndroidEntryPoint
class CompanyInfoFragment : Fragment() {

    private var _binding: FragmentCompanyInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompanyInfoViewModel by activityViewModels()
    private var companyId: Int = 0  // Variable to hold the company ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.companyInfo.observe(viewLifecycleOwner) { info ->
            info?.let {
                companyId = it.companyId  // Save the existing company ID
                binding.companyNameInput.setText(it.companyName)
                binding.emailInput.setText(it.email)
                binding.primaryPhoneInput.setText(it.primaryPhone)
                binding.secondaryPhoneInput.setText(it.secondaryPhone)
                binding.addressInput.setText(it.address)
                binding.websiteInput.setText(it.website)
                binding.signatureNameInput.setText(it.signatureName)
            }
        }

        binding.saveButton.setOnClickListener {
            val updatedInfo = CompanyInfoEntity(
                companyId,  // Use the existing company ID
                binding.companyNameInput.text.toString(),
                binding.emailInput.text.toString(),
                binding.primaryPhoneInput.text.toString(),
                binding.secondaryPhoneInput.text.toString(),
                binding.addressInput.text.toString(),
                binding.websiteInput.text.toString(),
                binding.signatureNameInput.text.toString()
            )
            viewModel.updateCompanyInfo(updatedInfo)
            Toast.makeText(requireContext(), "Company Information is Updated", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
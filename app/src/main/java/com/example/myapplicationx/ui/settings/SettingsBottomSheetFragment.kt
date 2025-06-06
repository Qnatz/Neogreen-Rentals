package com.example.myapplicationx.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.myapplicationx.databinding.FragmentSettingsBottomSheetBinding
import androidx.navigation.fragment.findNavController
import com.example.myapplicationx.R

class SettingsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)

        // Set click listeners for each TextView to navigate to respective fragments
        binding.navigationLogo.setOnClickListener {
            findNavController().navigate(R.id.navigation_logo)
            dismiss()
        }

        binding.navigationCompanyInfo.setOnClickListener {
            findNavController().navigate(R.id.navigation_company_info)
            dismiss()
        }

        binding.navigationCompanySignature.setOnClickListener {
            findNavController().navigate(R.id.navigation_company_signature)
            dismiss()
        }

        binding.navigationLanguage.setOnClickListener {
            findNavController().navigate(R.id.navigation_language)
            dismiss()
        }

        binding.navigationTemplates.setOnClickListener {
            findNavController().navigate(R.id.navigation_templates)
            dismiss()
        }

        binding.navigationTaxes.setOnClickListener {
            findNavController().navigate(R.id.navigation_taxes)
            dismiss()
        }

        binding.navigationBackup.setOnClickListener {
            findNavController().navigate(R.id.navigation_backup)
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.myapplicationx.ui.settings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.recyclerViewSettings
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        // Observe ViewModel data and set adapter
        settingsViewModel.settings.observe(viewLifecycleOwner) { settings ->
            recyclerView.adapter = SettingsAdapter(settings) { settingId ->
                onSettingClicked(settingId) // Handle click
            }
        }
    }

    private fun onSettingClicked(settingId: Int) {
        val action = when (settingId) {
            1 -> SettingsFragmentDirections.actionSettingsToLogo()
            2 -> SettingsFragmentDirections.actionSettingsToCompanyInfo()
            3 -> SettingsFragmentDirections.actionSettingsToSignature()
            4 -> SettingsFragmentDirections.actionSettingsToLanguageCurrency()
            5 -> SettingsFragmentDirections.actionSettingsToTemplates()
            6 -> SettingsFragmentDirections.actionSettingsToTaxes()
            7 -> SettingsFragmentDirections.actionSettingsToServices()
            8 -> SettingsFragmentDirections.actionSettingsToBackup()
            9 -> SettingsFragmentDirections.actionSettingsToServicesTaxes()
            else -> throw IllegalArgumentException("Unknown settingId: $settingId")
        }
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
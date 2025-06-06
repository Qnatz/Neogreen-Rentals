// ServiceFragment.kt
package com.example.myapplicationx.ui.settings.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationx.R
import androidx.navigation.fragment.findNavController
import com.example.myapplicationx.databinding.FragmentServiceBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServiceFragment : Fragment() {

    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ServicesViewModel by viewModels()

    private lateinit var adapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ServiceAdapter(
            emptyList(),
            onEditClick = { service ->
                val action = ServiceFragmentDirections.actionServiceFragmentToEditServiceFragment(service.serviceId)
                findNavController().navigate(action)
            },
            onDeleteClick = { service ->
                viewModel.deleteService(service)
            }
        )

        binding.rvServices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServices.adapter = adapter

        viewModel.allServices.observe(viewLifecycleOwner) { services ->
            adapter.updateServices(services)
        }

        binding.fabAddService.setOnClickListener {
            findNavController().navigate(ServiceFragmentDirections.actionServiceFragmentToAddServiceFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
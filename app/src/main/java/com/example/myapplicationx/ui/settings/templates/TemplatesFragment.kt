package com.example.myapplicationx.ui.settings.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentTemplatesBinding

class TemplatesFragment : Fragment() {

    private lateinit var binding: FragmentTemplatesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the template preview (use an image or any other placeholder)
        binding.templatePreview.setImageResource(R.drawable.ic_invoice_preview)

        // Handle template selection
        binding.selectTemplateButton.setOnClickListener {
            // Navigate to the EditInvoiceFragment to start creating the invoice
            findNavController().navigate(R.id.action_templatesFragment_to_editInvoiceFragment)
        }
    }
}
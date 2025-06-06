package com.example.myapplicationx.ui.settings.companySignature

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplicationx.databinding.FragmentCompanySignatureBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class CompanySignatureFragment : Fragment() {

    private var _binding: FragmentCompanySignatureBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompanySignatureViewModel by viewModels()
    private val fileName = "signature.png"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCompanySignatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the signature data and attempt to load the saved signature if available.
        viewModel.companySignature.observe(viewLifecycleOwner) { signature ->
            signature?.let {
                if (it.signatureUrl.isNotEmpty()) {
                    Log.d("CompanySignatureFrag", "Found saved signature path: ${it.signatureUrl}")
                    loadSignatureFromFile(it.signatureUrl)
                } else {
                    Log.d("CompanySignatureFrag", "No saved signature path found.")
                }
            }
        }

        binding.clearButton.setOnClickListener {
            binding.signatureView.clear()
        }

        binding.saveButton.setOnClickListener {
            saveSignature()
        }
    }

    private fun loadSignatureFromFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            Log.d("CompanySignatureFrag", "Signature file exists at: ${file.absolutePath}")
            try {
                BitmapFactory.decodeFile(file.absolutePath)?.let { bitmap ->
                    binding.signatureView.setSignatureBitmap(bitmap)
                    Log.d("CompanySignatureFrag", "Loaded signature from: $filePath")
                }
            } catch (e: Exception) {
                Log.e("CompanySignatureFrag", "Error loading signature: ${e.message}")
            }
        } else {
            Log.e("CompanySignatureFrag", "Signature file does not exist at: $filePath")
        }
    }

    private fun saveSignature() {
        val bitmap: Bitmap? = binding.signatureView.getSignatureBitmap()
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
            Toast.makeText(requireContext(), "Please draw a valid signature first", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(requireContext().filesDir, fileName)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                // Update and persist the signature path via ViewModel
                val updatedSignature = CompanySignature(signatureUrl = file.absolutePath)
                viewModel.updateCompanySignature(updatedSignature)

                Log.d("CompanySignatureFrag", "Saved signature to: ${file.absolutePath}")
                Toast.makeText(requireContext(), "Signature saved", Toast.LENGTH_SHORT).show()
                if (isAdded) findNavController().navigateUp()
            }
        } catch (e: IOException) {
            Log.e("CompanySignatureFrag", "Save failed: ${e.message}")
            Toast.makeText(requireContext(), "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
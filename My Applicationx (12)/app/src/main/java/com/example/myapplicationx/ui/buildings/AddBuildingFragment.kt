package com.example.myapplicationx.ui.buildings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.myapplicationx.databinding.FragmentAddBuildingBinding
import com.example.myapplicationx.database.model.BuildingEntity
import com.example.myapplicationx.ui.buildings.BuildingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class AddBuildingFragment : Fragment() {

    private var _binding: FragmentAddBuildingBinding? = null
    private val binding get() = _binding!!

    private val buildingsViewModel: BuildingsViewModel by viewModels()
    private var imageUri: Uri? = null

    // For selecting an image from the gallery
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            loadImage(imageUri)
        }
    }

    // For capturing an image with the camera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imageUri = saveImageToExternalStorage(bitmap)
            loadImage(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle Save Button click
        binding.addSaveButton.setOnClickListener {
            val buildingName = binding.addBuildingName.text?.toString() ?: ""
            val buildingLocation = binding.addLocation.text?.toString() ?: ""

            if (buildingName.isBlank() || buildingLocation.isBlank()) {
                // Show error message (e.g., Toast or Snackbar)
                return@setOnClickListener
            }

            val imageUrl = imageUri?.toString() ?: ""

            val newBuilding = BuildingEntity(
                buildingId = 0,
                buildingName = buildingName,
                buildingLocation = buildingLocation,
                occupiedUnits = 0,
                vacantUnits = 0,
                imageUrl = imageUrl
            )

            // Add building to ViewModel
            buildingsViewModel.addBuilding(newBuilding)

            // Clear the input fields
            binding.addBuildingName.text?.clear()
            binding.addLocation.text?.clear()
            binding.addImageView.setImageDrawable(null)
            imageUri = null
            
           findNavController().navigateUp()
        }

        // Handle Image Button click (to pick from gallery)
        binding.addImageButton.setOnClickListener {
            showImagePicker()
        }

        // Handle Capture Image Button click (to capture from camera)
        binding.captureImageButton.setOnClickListener {
            captureImage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Launch image picker (gallery)
    private fun showImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // Launch camera to capture image
    private fun captureImage() {
        cameraLauncher.launch(null)
    }

    // Save bitmap to external storage using FileProvider
    private fun saveImageToExternalStorage(bitmap: Bitmap): Uri? {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val imagesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, filename)

        try {
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        // Generate URI using FileProvider
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
    }

    // Load image into ImageView using Glide
    private fun loadImage(uri: Uri?) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.addImageView)
    }
}
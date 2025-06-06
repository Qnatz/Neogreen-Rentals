package com.example.myapplicationx.ui.buildings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplicationx.databinding.FragmentEditBuildingBinding
import com.example.myapplicationx.database.model.BuildingEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditBuildingFragment : Fragment() {

    private var _binding: FragmentEditBuildingBinding? = null
    private val binding get() = _binding!!

    private val buildingsViewModel: BuildingsViewModel by viewModels()
    private var imageUri: Uri? = null
    private val args: EditBuildingFragmentArgs by navArgs() // Safe Args for buildingId

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
            imageUri = saveImageToInternalStorage(bitmap)
            loadImage(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBuildingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the existing building details into the UI
        buildingsViewModel.getBuildingDetails(args.buildingId).observe(viewLifecycleOwner) { building ->
            if (building != null) {
                binding.editBuildingName.setText(building.buildingName)
                binding.editLocation.setText(building.buildingLocation)
                val occpiedUnits = building.occupiedUnits
                val vacantUnits = building.vacantUnits
                imageUri = Uri.parse(building.imageUrl)
                loadImage(imageUri)
            }
        }

        // Handle Save Button click
binding.editSaveButton.setOnClickListener {
    val buildingName = binding.editBuildingName.text?.toString() ?: ""
    val buildingLocation = binding.editLocation.text?.toString() ?: ""

    if (buildingName.isBlank() || buildingLocation.isBlank()) {
        // Show error message (e.g., Toast or Snackbar)
        return@setOnClickListener
    }

    val imageUrl = imageUri.toString()

    // Get the existing values of occupied and vacant units from the ViewModel or building object
    val occupiedUnits = buildingsViewModel.getBuildingDetails(args.buildingId).value?.occupiedUnits ?: 0
    val vacantUnits = buildingsViewModel.getBuildingDetails(args.buildingId).value?.vacantUnits ?: 0

    val updatedBuilding = BuildingEntity(
        buildingId = args.buildingId,
        buildingName = buildingName,
        buildingLocation = buildingLocation,
        occupiedUnits = occupiedUnits,  // Preserve the original value
        vacantUnits = vacantUnits,      // Preserve the original value
        imageUrl = imageUrl
    )

    // Update building in ViewModel
    buildingsViewModel.updateBuilding(updatedBuilding)

    // Clear the input fields
    binding.editBuildingName.text?.clear()
    binding.editLocation.text?.clear()
    binding.editImageView.setImageDrawable(null) // Clear the image view
}

        // Handle Image Button click (to pick from gallery)
        binding.editImageButton.setOnClickListener {
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
        cameraLauncher.launch(null) // Using TakePicturePreview to capture directly as Bitmap
    }

    // Save bitmap to internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: FileOutputStream? = null
        try {
            fos = requireContext().openFileOutput(filename, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fos?.close()
        }
        val file = File(requireContext().filesDir, filename)
        return Uri.fromFile(file)
    }

    // Load image into ImageView using Glide
    private fun loadImage(uri: Uri?) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.editImageView)
    }
}
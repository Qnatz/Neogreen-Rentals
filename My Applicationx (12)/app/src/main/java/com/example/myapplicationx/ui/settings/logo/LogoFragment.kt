package com.example.myapplicationx.ui.settings.logo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.content.Context
import android.graphics.Canvas
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentLogoBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min
import kotlin.math.max

class LogoFragment : Fragment() {

    private var _binding: FragmentLogoBinding? = null
    private val binding get() = _binding!!
    private var selectedLogoUri: Uri? = null
    private var tempExtension: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedLogoUri = uri
                tempExtension = getFileExtension(uri)
                // Just load the preview without saving or navigating
                loadLogo(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Update logoImageView to match PDF behavior
        binding.logoImageView.apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            maxWidth = 300.dpToPx(requireContext())
            maxHeight = 200.dpToPx(requireContext())
        }
        
        val extension = getSavedLogoExtension()
        loadLogo(getSavedLogoUri(extension))

        binding.selectLogoButton.setOnClickListener { selectLogoFromGallery() }
        binding.deleteLogoButton.setOnClickListener { deleteLogo() }
        binding.editSaveButton.setOnClickListener { saveAndNavigateUp() }
    }

    private fun saveAndNavigateUp() {
        // If a new logo was selected, save it
        selectedLogoUri?.let { uri ->
            saveLogoToInternalStorage(uri)
        }
        
        // Navigate up after saving
        if (isAdded) {
            findNavController().navigateUp()
        }
    }

    private fun selectLogoFromGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            imagePickerLauncher.launch(it)
        }
    }

    private fun deleteLogo() {
        deleteAllLogoFiles()
        selectedLogoUri = null
        tempExtension = null
        loadLogo(null)
    }

    private fun saveLogoToInternalStorage(uri: Uri) {
        try {
            deleteAllLogoFiles()
            
            // Calculate dimensions from template - exactly match the first implementation
            val targetWidth = 300.dpToPx(requireContext())
            val targetHeight = 200.dpToPx(requireContext())
    
            // Load and scale bitmap to exact template requirements
            val scaledBitmap = createScaledBitmap(
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri),
                targetWidth,
                targetHeight,
                ScaleType.FIT_CENTER
            )
    
            // Save as PNG - exactly as in the first implementation
            File(requireContext().filesDir, "user_logo.png").apply {
                FileOutputStream(this).use { stream ->
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Add back the enum that was in the first implementation
    private enum class ScaleType {
        CENTER_CROP,
        FIT_CENTER
    }

    // Replicate the exact function from the first implementation
    private fun createScaledBitmap(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        scaleType: ScaleType = ScaleType.FIT_CENTER
    ): Bitmap {
        // Create a blank bitmap with the target dimensions and transparent background
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        
        // Calculate the scale factor based on the scale type
        val widthRatio = targetWidth.toFloat() / source.width
        val heightRatio = targetHeight.toFloat() / source.height
        
        val scaleFactor = when (scaleType) {
            ScaleType.CENTER_CROP -> max(widthRatio, heightRatio)
            ScaleType.FIT_CENTER -> min(widthRatio, heightRatio)
        }
        
        // Calculate the scaled dimensions of the source bitmap
        val scaledWidth = (source.width * scaleFactor).toInt()
        val scaledHeight = (source.height * scaleFactor).toInt()
        
        // Create the scaled bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        
        // Calculate the position to draw the scaled bitmap centered in the result
        val left = (targetWidth - scaledWidth) / 2
        val top = (targetHeight - scaledHeight) / 2
        
        // Draw the scaled bitmap onto the result bitmap
        val canvas = android.graphics.Canvas(result)
        canvas.drawBitmap(scaledBitmap, left.toFloat(), top.toFloat(), null)
        
        return result
    }
    
    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
        
    private fun deleteAllLogoFiles() {
        listOf("jpg", "png", "bmp").forEach { ext ->
            File(requireContext().filesDir, "user_logo.$ext").delete()
        }
    }

    private fun getFileExtension(uri: Uri): String {
        return when (requireContext().contentResolver.getType(uri)) {
            "image/png" -> "png"
            "image/bmp" -> "bmp"
            else -> "jpg"
        }
    }

    private fun getSavedLogoUri(extension: String?): Uri? {
        return extension?.let {
            File(requireContext().filesDir, "user_logo.$it").takeIf { it.exists() }?.let { file ->
                Uri.fromFile(file)
            }
        }
    }

    private fun getSavedLogoExtension(): String? {
        return listOf("jpg", "png", "bmp").firstOrNull { ext ->
            File(requireContext().filesDir, "user_logo.$ext").exists()
        }
    }

    private fun loadLogo(uri: Uri?) {
        Glide.with(this)
            .load(uri ?: R.drawable.placeholder_logo)
            .fitCenter()
            .into(binding.logoImageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
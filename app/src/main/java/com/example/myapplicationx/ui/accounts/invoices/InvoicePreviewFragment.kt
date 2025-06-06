package com.example.myapplicationx.ui.accounts.invoices

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.CompanyInfoEntity
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.InvoiceItem
import com.example.myapplicationx.database.model.InvoicePdf
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.databinding.FragmentInvoicePreviewBinding
import com.example.myapplicationx.databinding.InvoiceTemplateDefaultBinding
import com.example.myapplicationx.ui.settings.companyInfo.CompanyInfoViewModel
import com.example.myapplicationx.ui.tenants.TenantsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class InvoicePreviewFragment : Fragment() {

    private var _binding: FragmentInvoicePreviewBinding? = null
    private val binding get() = _binding!!

    private val args: InvoicePreviewFragmentArgs by navArgs()
    private val invoiceViewModel: InvoicesViewModel by viewModels()
    private val tenantsViewModel: TenantsViewModel by viewModels()
    private val companyInfoViewModel: CompanyInfoViewModel by viewModels()

    private lateinit var invoice: InvoiceEntity
    private var tenant: TenantEntity? = null
    private var companyInfo: CompanyInfoEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoicePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invoice = args.invoice
        binding.previewStatusText.text = if (args.isPreviewOnly) {
            "Preview Mode - Invoice not yet saved"
        } else {
            "Invoice PDF"
        }

        tenantsViewModel.getTenantByIdLive(invoice.tenantId).observe(viewLifecycleOwner) { tenantResult ->
            tenant = tenantResult
            // Now fetch company info
            companyInfoViewModel.getCompanyInfo { companyResult ->
                companyInfo = companyResult
                if (tenant == null || companyInfo == null) {
                    showMissingInfoDialog()
                } else {
                    renderInvoicePreview()
                }
            }
        }

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnSavePdf.setOnClickListener {
            generateAndSavePdf()
        }
        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun renderInvoicePreview() {
    // Inflate template using hidden container
    val templateBinding: InvoiceTemplateDefaultBinding = DataBindingUtil.inflate(
        LayoutInflater.from(requireContext()),
        R.layout.invoice_template_default,
        binding.hiddenContainer,
        false
    )

    // Remove template padding for full-width rendering
    templateBinding.root.apply {
        setPadding(0, 0, 0, 0) // Remove all padding from root layout
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    binding.hiddenContainer.apply {
        removeAllViews()
        addView(templateBinding.root)
        visibility = View.VISIBLE
        alpha = 0f
    }

    bindTemplateData(templateBinding)

    // Configure RecyclerView
    templateBinding.recyclerViewInvoiceItems.apply {
        adapter = InvoiceItemPdfAdapter(args.items.toList())
        layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically() = false // Disable scrolling
        }
        isNestedScrollingEnabled = false
    }

    // Get full display dimensions
    val displayWidth = resources.displayMetrics.widthPixels
    val displayHeight = resources.displayMetrics.heightPixels

    templateBinding.root.postDelayed({
        // Force full-width measurement
        templateBinding.root.measure(
            View.MeasureSpec.makeMeasureSpec(displayWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(displayHeight, View.MeasureSpec.AT_MOST)
        )

        // Calculate total height including all sections
        val totalHeight = templateBinding.root.measuredHeight +
                templateBinding.paymentNsignature.measuredHeight +
                templateBinding.footer.measuredHeight

        // Layout with exact dimensions
        templateBinding.root.layout(0, 0, displayWidth, totalHeight)

        // Create full-width bitmap
        val bitmap = Bitmap.createBitmap(
            displayWidth,
            totalHeight,
            Bitmap.Config.ARGB_8888
        ).apply {
            Canvas(this).apply {
                // Draw entire template hierarchy
                templateBinding.root.draw(this)
            }
        }

        // Display in PhotoView with proper scaling
        binding.photoView.apply {
            setImageBitmap(bitmap)
            //scaleType = PhotoView.ScaleType.FIT_CENTER
            maximumScale = 5f
            minimumScale = 1f
        }
        
        // Force redraw
    //invalidate()

    }, 500) // Allow layout to complete
}

    private fun generateAndSavePdf() {
        val fileName = "${invoice.invoiceNumber}.pdf"
        val pdfFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        val currentTenant = tenant ?: return.also {
            Toast.makeText(requireContext(), "Tenant information missing", Toast.LENGTH_SHORT).show()
        }
        val currentCompany = companyInfo ?: return.also {
            Toast.makeText(requireContext(), "Company information missing", Toast.LENGTH_SHORT).show()
        }

        // Debug file paths for logo and signature
        val logoPath = getLogoPath()
        val signaturePath = getSignaturePath()
        Log.d("InvoicePDF", "Logo path: $logoPath, exists: ${File(logoPath).exists()}")
        Log.d("InvoicePDF", "Signature path: $signaturePath, exists: ${File(signaturePath).exists()}")

        val invoicePdfData = InvoicePdf(
            invoiceTitle = "INVOICE",
            invoiceNumber = invoice.invoiceNumber,
            invoiceDate = invoice.invoiceDate,
            receivableDate = invoice.receivableDate,
            companyLogo = logoPath,
            companyName = currentCompany.companyName ?: "",
            companyAddress = currentCompany.address ?: "",
            companyContact = currentCompany.email ?: "",
            companyTel1 = currentCompany.primaryPhone ?: "",
            companyTel2 = currentCompany.secondaryPhone ?: "",
            customerName = currentTenant.tenantName ?: "",
            customerAddress = currentTenant.houseName ?: "",
            customerContact = currentTenant.email ?: "",
            customerTel1 = currentTenant.primaryPhone ?: "",
            customerTel2 = currentTenant.secondaryPhone ?: "",
            invoiceItems = args.items.toList().map { item ->
                InvoiceItem(
                    itemDescription = item.itemDescription,
                    itemUnits = String.format("%.2f", item.itemUnits),
                    itemUnitPrice = String.format("%.2f", item.itemUnitPrice),
                    itemRate = String.format("%.2f", item.computedNetBase)
                )
            },
            subtotal = String.format("%.2f", args.items.toList().sumOf { it.computedNetBase }),
            tax = String.format("%.2f", args.items.toList().sumOf { it.computedTax }),
            total = String.format("%.2f", args.items.toList().sumOf { it.computedTotal }),
            paymentInstructions = "PAYMENT DETAILS:",
            paymentBankDetails = invoice.invoiceNotes ?: "",
            signature = signaturePath,
            signatureName = currentCompany.signatureName ?: "Authorized Signature"
        )

        try {
            if (createInvoicePdf(invoicePdfData, pdfFile, requireContext())) {
                sharePdfFile(pdfFile)
            } else {
                Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdfFile(pdfFile: File) {
        val contentUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            pdfFile
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Invoice PDF"))
    }

    private fun bindTemplateData(templateBinding: InvoiceTemplateDefaultBinding) {
        val currentTenant = tenant ?: return
        val currentCompany = companyInfo ?: return

        templateBinding.apply {
            invoiceNumber.text = invoice.invoiceNumber
            invoiceDate.text = "Invoice Date: ${invoice.invoiceDate}"
            receivableDate.text = "Receivable Date: ${invoice.receivableDate}"

            companyName.text = currentCompany.companyName
            companyAddress.text = currentCompany.address
            companyContact.text = currentCompany.email
            companyTel1.text = currentCompany.primaryPhone
            companyTel2.text = currentCompany.secondaryPhone

            customerName.text = currentTenant.tenantName
            customerAddress.text = currentTenant.houseName
            customerContact.text = currentTenant.email
            customerTel1.text = currentTenant.primaryPhone
            customerTel2.text = currentTenant.secondaryPhone

            subtotalValue.text = String.format("%.2f", args.items.toList().sumOf { it.computedNetBase })
            taxValue.text = String.format("%.2f", args.items.toList().sumOf { it.computedTax })
            totalValue.text = String.format("%.2f", args.items.toList().sumOf { it.computedTotal })
            paymentBankDetails.text = invoice.invoiceNotes ?: ""
            signatureName.text = currentCompany.signatureName

            // Load company logo using Glide
            val logoFile = getLogoFile()
Log.d("InvoicePreview", "Logo file path: ${logoFile.absolutePath}, exists: ${logoFile.exists()}")
            if (logoFile.exists()) {
                Glide.with(requireContext())
                    .load(logoFile)
                    .into(companyLogo)
                companyLogo.visibility = View.VISIBLE
            } else {
                companyLogo.visibility = View.GONE
            }

            // Load signature image using Glide
            val signatureFile = getSignatureFile()
            if (signatureFile.exists()) {
                Glide.with(requireContext())
                    .load(signatureFile)
                    .into(signatureImage)
                signatureImage.visibility = View.VISIBLE
            } else {
                signatureImage.visibility = View.GONE
            }
        }
    }

    private fun getLogoPath(): String {
        val logoFile = getLogoFile()
        return if (logoFile.exists()) logoFile.absolutePath else ""
    }

    private fun getLogoFile(): File {
        val extension = getSavedLogoExtension()
        return if (extension != null) {
            File(requireContext().filesDir, "user_logo.$extension")
        } else {
            File("")
        }
    }

    private fun getSavedLogoExtension(): String? {
        return listOf("png", "jpg", "bmp").firstOrNull { ext ->
            File(requireContext().filesDir, "user_logo.$ext").exists()
        }
    }

    private fun getSignaturePath(): String {
        val signatureFile = getSignatureFile()
        return if (signatureFile.exists()) signatureFile.absolutePath else ""
    }

    private fun getSignatureFile(): File {
        return File(requireContext().filesDir, "signature.png")
    }

    private fun showMissingInfoDialog() {
        val missingInfo = when {
            tenant == null && companyInfo == null -> "Tenant and Company Information"
            tenant == null -> "tenant information"
            else -> "company information"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Missing Information")
            .setMessage("The $missingInfo is missing. Please update it in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                findNavController().navigate(
                    when {
                        tenant == null -> R.id.action_invoicePreviewFragment_to_addTenantFragment
                        else -> R.id.action_invoicePreviewFragment_to_companyInfoFragment
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
}
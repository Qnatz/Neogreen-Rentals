package com.example.myapplicationx.ui.accounts.invoices

import android.content.Context
import android.graphics.BitmapFactory
import com.example.myapplicationx.database.model.InvoicePdf
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.io.FileOutputStream

fun createInvoicePdf(invoice: InvoicePdf, outputFile: File, context: Context): Boolean {
    try {
        FileOutputStream(outputFile).use { fos ->
            val writer = PdfWriter(fos)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc, PageSize.A4)

            // ========= HEADER: Logo + Invoice Details =========
            // Two-column table: left cell for logo, right cell for invoice details (right-aligned)
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                .setWidth(UnitValue.createPercentValue(100f))
            // Left cell: Company Logo
            val logoCell = Cell().setBorder(null)
            if (invoice.companyLogo.isNotEmpty()) {
                val logoFile = File(invoice.companyLogo)
                if (logoFile.exists()) {
                    val logoBytes = logoFile.readBytes()
                    val logoImage = Image(ImageDataFactory.create(logoBytes))
                        .scaleToFit(150f, 100f) // adjust to match your XML dimensions
                    logoCell.add(logoImage)
                }
            }
            headerTable.addCell(logoCell)
            // Right cell: Invoice details (right aligned)
            val detailsCell = Cell().setBorder(null)
            detailsCell.setTextAlignment(TextAlignment.RIGHT)
            detailsCell.add(
                Paragraph(invoice.invoiceTitle)
                    .setFontSize(22f)
                    .setBold()
            )
            detailsCell.add(
                Paragraph(invoice.invoiceNumber)
                    .setFontSize(14f)
            )
            detailsCell.add(
                Paragraph("Invoice Date: ${invoice.invoiceDate}")
                    .setFontSize(14f)
            )
            detailsCell.add(
                Paragraph("Receivable Date: ${invoice.receivableDate}")
                    .setFontSize(14f)
            )
            headerTable.addCell(detailsCell)
            document.add(headerTable)
            document.add(Paragraph("\n"))

            // ========= Company and Customer Details =========
            val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                .setWidth(UnitValue.createPercentValue(100f))
            // Company details (left)
            val companyCell = Cell().setBorder(null)
            companyCell.add(Paragraph(invoice.companyName).setFontSize(16f).setBold())
            companyCell.add(Paragraph(invoice.companyAddress).setFontSize(14f))
            companyCell.add(Paragraph(invoice.companyContact).setFontSize(14f))
            companyCell.add(Paragraph("Tel: ${invoice.companyTel1}").setFontSize(14f))
            if (invoice.companyTel2.isNotEmpty()) {
                companyCell.add(Paragraph("Alt Tel: ${invoice.companyTel2}").setFontSize(14f))
            }
            detailsTable.addCell(companyCell)
            // Customer details (right)
            val customerCell = Cell().setBorder(null)
            customerCell.add(Paragraph(invoice.customerName).setFontSize(16f).setBold())
            customerCell.add(Paragraph(invoice.customerAddress).setFontSize(14f))
            customerCell.add(Paragraph(invoice.customerContact).setFontSize(14f))
            customerCell.add(Paragraph("Tel: ${invoice.customerTel1}").setFontSize(14f))
            if (invoice.customerTel2.isNotEmpty()) {
                customerCell.add(Paragraph("Alt Tel: ${invoice.customerTel2}").setFontSize(14f))
            }
            detailsTable.addCell(customerCell)
            document.add(detailsTable)
            
            // ========= Divider =========
            document.add(Paragraph("────────────────────────────────────────────────────────────────────────")
                .setTextAlignment(TextAlignment.CENTER))
            
            // ========= Invoice Items Table =========
            // 4 columns table: DESCRIPTION, UNITS, UNIT PRICE, RATE
            val itemsTable = Table(UnitValue.createPercentArray(floatArrayOf(25f, 25f, 25f, 25f)))
                .setWidth(UnitValue.createPercentValue(100f))
            // Header row
            itemsTable.addHeaderCell(Cell().add(Paragraph("DESCRIPTION").setBold().setFontSize(10f)))
            itemsTable.addHeaderCell(Cell().add(Paragraph("UNITS").setBold().setFontSize(10f)))
            itemsTable.addHeaderCell(Cell().add(Paragraph("UNIT PRICE").setBold().setFontSize(10f)))
            itemsTable.addHeaderCell(Cell().add(Paragraph("RATE").setBold().setFontSize(10f)))
            // Data rows for each invoice item
            invoice.invoiceItems.forEach { item ->
                itemsTable.addCell(Cell().add(Paragraph(item.itemDescription).setFontSize(10f)))
                itemsTable.addCell(Cell().add(Paragraph(item.itemUnits).setFontSize(10f)))
                itemsTable.addCell(Cell().add(Paragraph(item.itemUnitPrice).setFontSize(10f)))
                itemsTable.addCell(Cell().add(Paragraph(item.itemRate).setFontSize(10f)))
            }
            // ========= Summation Row within Items Table =========
            // Create a row where the first two columns are empty and the last two contain a nested table with summations.
            itemsTable.addCell(Cell(1, 2).setBorder(null))
            // Create a nested table for the summations (2 columns: label and value)
            val summationNestedTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                .setWidth(UnitValue.createPercentValue(100f))
            // Subtotal row
            summationNestedTable.addCell(
                Cell().add(Paragraph("Subtotal").setFontSize(14f))
                    .setBorder(null)
            )
            summationNestedTable.addCell(
                Cell().add(Paragraph(invoice.subtotal).setFontSize(14f))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
            // Taxes row
            summationNestedTable.addCell(
                Cell().add(Paragraph("Taxes").setFontSize(14f))
                    .setBorder(null)
            )
            summationNestedTable.addCell(
                Cell().add(Paragraph(invoice.tax).setFontSize(14f))
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
            // Total row (bold)
            summationNestedTable.addCell(
                Cell().add(Paragraph("TOTAL").setFontSize(18f).setBold())
                    .setBorder(null)
            )
            summationNestedTable.addCell(
                Cell().add(Paragraph(invoice.total).setFontSize(18f).setBold())
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.RIGHT)
            )
            // Divider row below summations: a cell spanning both columns with a top border
            summationNestedTable.addCell(
                Cell(1, 2)
                    .setBorderTop(SolidBorder(1f))
                    .setBorderBottom(null)
                    .setBorderLeft(null)
                    .setBorderRight(null)
            )
            // Add the nested summation table to a cell spanning 2 columns (columns 3 & 4)
            itemsTable.addCell(Cell(1, 2).setBorder(null).add(summationNestedTable))
            
            document.add(itemsTable)
            
            // ========= Divider =========
            document.add(Paragraph("────────────────────────────────────────────────────────────────────────")
                .setTextAlignment(TextAlignment.CENTER))
            
            // ========= Payment Details and Signature =========
            val paymentTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                .setWidth(UnitValue.createPercentValue(100f))
            // Payment details (left)
            val paymentCell = Cell().setBorder(null)
            paymentCell.add(Paragraph("PAYMENT DETAILS:").setBold().setFontSize(10f))
            paymentCell.add(Paragraph(invoice.paymentBankDetails).setFontSize(14f))
            paymentTable.addCell(paymentCell)
            // Signature (right)
            val signatureCell = Cell().setBorder(null)
            if (invoice.signature?.isNotEmpty() == true) {
                val signatureFile = File(invoice.signature)
                if (signatureFile.exists()) {
                    val sigBytes = signatureFile.readBytes()
                    val sigImage = Image(ImageDataFactory.create(sigBytes))
                        .scaleToFit(100f, 50f)
                    signatureCell.add(sigImage)
                }
            }
            // Add a label below the signature (for example, the signature owner's name)
            signatureCell.add(Paragraph(invoice.signatureName).setFontSize(14f))
            paymentTable.addCell(signatureCell)
            document.add(paymentTable)
            
            // ========= Footer =========
            document.add(
                Paragraph("Thank you for doing business with us!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14f)
            )
            
            document.close()
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
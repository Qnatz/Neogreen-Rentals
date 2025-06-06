package com.example.myapplicationx.database

import android.util.Log
import androidx.room.TypeConverter
import com.example.myapplicationx.database.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class Converters {
    private val gson = Gson()

    // --- Converters for List<String> ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    // --- Converters for LocalDate ---
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val customFormats =
        listOf(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"), // ISO format
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
        )

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(isoFormatter)

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let {
            if (isIsoFormat(it)) {
                LocalDate.parse(it, isoFormatter)
            } else {
                parseCustomFormats(it)
            }
        }

    private fun isIsoFormat(dateString: String): Boolean =
        try {
            isoFormatter.parse(dateString)
            true
        } catch (e: DateTimeParseException) {
            false
        }

    private fun parseCustomFormats(dateString: String): LocalDate? {
        for (formatter in customFormats) {
            try {
                return LocalDate.parse(dateString, formatter)
            } catch (e: DateTimeParseException) {
                Log.d("Converters", "Failed to parse date with format: $formatter")
            }
        }
        Log.e("Converters", "Unable to parse date string: $dateString with any known format")
        return null
    }

    // --- Converters for Instant ---
    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    // --- Converters for List<ReceiptInvoice> ---
    @TypeConverter
    fun fromReceiptInvoiceList(value: List<ReceiptInvoice>?): String? = gson.toJson(value)

    @TypeConverter
    fun toReceiptInvoiceList(value: String?): List<ReceiptInvoice>? {
        val listType = object : TypeToken<List<ReceiptInvoice>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromServiceTaxCrossRefList(value: List<ServiceTaxCrossRef>?): String? = gson.toJson(value)

    @TypeConverter
    fun toServiceTaxCrossRefList(value: String?): List<ServiceTaxCrossRef>? {
        val listType = object : TypeToken<List<ServiceTaxCrossRef>>() {}.type
        return gson.fromJson(value, listType)
    }
}

package com.example.myapplicationx.ui.settings.languageCurrencyDateFormat

object Constants {
    val DATE_FORMATS = listOf(
        "dd MMM yyyy",
        "dd/MM/yyyy",
        "MM-dd-yyyy",
        "yyyy-MM-dd", // internal standard format
        "EEE, dd MMM yyyy"
    )

    // Standard countries to populate dropdowns
    val STANDARD_COUNTRIES = listOf(
        Country(
            name = "United States",
            languages = listOf("English"),
            currencies = listOf("USD"),
            timezones = listOf("America/New_York")
        ),
        Country(
            name = "United Kingdom",
            languages = listOf("English"),
            currencies = listOf("GBP"),
            timezones = listOf("Europe/London")
        ),
        Country(
            name = "Canada",
            languages = listOf("English", "French"),
            currencies = listOf("CAD"),
            timezones = listOf("America/Toronto")
        ),
        Country(
            name = "Kenya",
            languages = listOf("English", "Swahili"),
            currencies = listOf("KES"),
            timezones = listOf("Africa/Nairobi")
        ),
        Country(
            name = "South Africa",
            languages = listOf("English"),
            currencies = listOf("ZAR"),
            timezones = listOf("Africa/Johannesburg")
        ),
        Country(
            name = "Senegal",
            languages = listOf("French"),
            currencies = listOf("XOF"),
            timezones = listOf("Africa/Dakar")
        ),
        Country(
            name = "Rwanda",
            languages = listOf("Kinyarwanda", "English", "French"),
            currencies = listOf("RWF"),
            timezones = listOf("Africa/Kigali")
        ),
        Country(
            name = "India",
            languages = listOf("Hindi", "English"),
            currencies = listOf("INR"),
            timezones = listOf("Asia/Kolkata")
        ),
        Country(
            name = "China",
            languages = listOf("Chinese"),
            currencies = listOf("CNY"),
            timezones = listOf("Asia/Shanghai")
        ),
        Country(
            name = "Nepal",
            languages = listOf("Nepali"),
            currencies = listOf("NPR"),
            timezones = listOf("Asia/Kathmandu")
        )
    )
}
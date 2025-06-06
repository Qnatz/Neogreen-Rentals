package com.example.myapplicationx.ui.settings.languageCurrencyDateFormat

import android.content.Context
import android.location.Geocoder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.Locale

// --- Data Models matching the REST API response ---
data class CountryResponse(
    val name: String,
    val languages: List<Language>?,
    val currencies: List<Currency>?,
    val timezones: List<String>?
)

data class Language(
    val name: String?
)

data class Currency(
    val code: String?,
    val name: String?,
    val symbol: String?
)

// A simplified Country model for our use.
data class Country(
    val name: String,
    val languages: List<String>,
    val currencies: List<String>,
    val timezones: List<String>
)

// --- Retrofit API interface ---
interface RestCountriesApi {
    @GET("name/{name}")
    suspend fun getCountryByName(
        @Path("name") countryName: String,
        @Query("fullText") fullText: Boolean = true
    ): retrofit2.Response<List<CountryResponse>>
}

// --- Helper object that combines reverse-geocoding and API call ---
object CountryHelper {

    // Build the Retrofit instance (using the v2 API endpoint)
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://restcountries.com/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: RestCountriesApi = retrofit.create(RestCountriesApi::class.java)

    /**
     * Uses the device’s lat/lon to reverse-geocode the country name,
     * then fetches the country details (languages, currencies, timezones) from the API.
     */
    suspend fun getCountryDetails(context: Context, latitude: Double, longitude: Double): Country? {
        // 1. Reverse geocode the location with error handling.
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = try {
            geocoder.getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        if (addresses.isNullOrEmpty()) return null

        val countryName = addresses[0].countryName ?: return null

        // 2. Retrieve country details via REST API.
        return try {
            val response = api.getCountryByName(countryName)
            if (response.isSuccessful) {
                val countryResponseList = response.body()
                if (!countryResponseList.isNullOrEmpty()) {
                    val countryResponse = countryResponseList[0]
                    val languages = countryResponse.languages?.mapNotNull { it.name } ?: emptyList()
                    val currencies = countryResponse.currencies?.mapNotNull { it.symbol } ?: emptyList()
                    val timezones = countryResponse.timezones ?: emptyList()
                    Country(
                        name = countryResponse.name,
                        languages = languages,
                        currencies = currencies,
                        timezones = timezones
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
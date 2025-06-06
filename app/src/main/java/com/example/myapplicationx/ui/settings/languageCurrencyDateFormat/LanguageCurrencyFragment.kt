package com.example.myapplicationx.ui.settings.languageCurrencyDateFormat

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.net.ConnectivityManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplicationx.R
import com.example.myapplicationx.databinding.FragmentLanguageCurrencyBinding
import com.google.android.gms.location.LocationServices

class LanguageCurrencyFragment : Fragment() {

    private lateinit var binding: FragmentLanguageCurrencyBinding
    private lateinit var viewModel: LanguageCurrencyViewModel

    // Arbitrary request code for permissions.
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_language_currency,
            container,
            false
        )
        viewModel = ViewModelProvider(this).get(LanguageCurrencyViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up the date format dropdown with our static list.
        val dateFormatsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            Constants.DATE_FORMATS
        )
        binding.spinnerDateFormat.setAdapter(dateFormatsAdapter)

        // Set up the country dropdown with our standard countries.
        val countryNames = Constants.STANDARD_COUNTRIES.map { it.name }
        val countryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            countryNames
        )
        binding.dropdownCountry.setAdapter(countryAdapter)

        // When a country is selected, update the other dropdowns.
        binding.dropdownCountry.setOnItemClickListener { _, _, position, _ ->
            val selectedCountryName = countryNames[position]
            val selectedCountry = Constants.STANDARD_COUNTRIES.firstOrNull { it.name == selectedCountryName }
            selectedCountry?.let { country ->
                // Populate language dropdown.
                val languageAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    country.languages
                )
                binding.dropdownLanguage.setAdapter(languageAdapter)
                // Populate currency dropdown.
                val currencyAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    country.currencies
                )
                binding.dropdownCurrency.setAdapter(currencyAdapter)
                // Populate timezone dropdown.
                val timezoneAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    country.timezones
                )
                binding.spinnerTimezone.setAdapter(timezoneAdapter)
                // Optionally set default selections.
                if (country.languages.isNotEmpty()) {
                    binding.dropdownLanguage.setText(country.languages[0], false)
                }
                if (country.currencies.isNotEmpty()) {
                    binding.dropdownCurrency.setText(country.currencies[0], false)
                }
                if (country.timezones.isNotEmpty()) {
                    binding.spinnerTimezone.setText(country.timezones[0], false)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load any previously saved settings into the UI.
        loadSettingsIntoUI()

        // Observe country details from the ViewModel.
        viewModel.countryDetails.observe(viewLifecycleOwner) { country ->
            country?.let {
                binding.dropdownCountry.setText(it.name, false)
                binding.dropdownLanguage.setText(it.languages.firstOrNull() ?: "", false)
                binding.dropdownCurrency.setText(it.currencies.firstOrNull() ?: "", false)
                binding.spinnerTimezone.setText(it.timezones.firstOrNull() ?: "", false)
                val savedDateFormat = viewModel.getSavedDateFormat() ?: Constants.DATE_FORMATS[0]
                binding.spinnerDateFormat.setText(savedDateFormat, false)
            }
        }

        // When the user clicks "Reload", first check permissions and services.
        binding.buttonReload.setOnClickListener {
            checkPermissionsAndServices {
                // All checks passed; obtain the current location.
                getCurrentLocation { latitude, longitude ->
                    viewModel.loadCountryData(latitude, longitude)
                }
            }
        }

        // When the user clicks "Save", persist the current settings.
        binding.buttonSave.setOnClickListener {
            val selectedCountry = binding.dropdownCountry.text.toString()
            val selectedLanguage = binding.dropdownLanguage.text.toString()
            val selectedCurrency = binding.dropdownCurrency.text.toString()
            val selectedTimezone = binding.spinnerTimezone.text.toString()
            val selectedDateFormat = binding.spinnerDateFormat.text.toString()

            viewModel.saveSettings(
                country = selectedCountry,
                language = selectedLanguage,
                currency = selectedCurrency,
                timezone = selectedTimezone,
                dateFormat = selectedDateFormat
            )
            // Optionally, provide user feedback (for example, a Toast).
        }
    }

    /**
     * Load any saved settings from SharedPreferences into the UI dropdowns.
     */
    private fun loadSettingsIntoUI() {
        viewModel.getSavedCountry()?.let { binding.dropdownCountry.setText(it, false) }
        viewModel.getSavedLanguage()?.let { binding.dropdownLanguage.setText(it, false) }
        viewModel.getSavedCurrency()?.let { binding.dropdownCurrency.setText(it, false) }
        viewModel.getSavedTimezone()?.let { binding.spinnerTimezone.setText(it, false) }
        viewModel.getSavedDateFormat()?.let { binding.spinnerDateFormat.setText(it, false) }
    }

    /**
     * Check location permission, location services, and Internet connectivity.
     * If all are okay, call onSuccess; otherwise, prompt the user accordingly.
     */
    private fun checkPermissionsAndServices(onSuccess: () -> Unit) {
        // Check for location permission.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
            return
        }

        // Check if location services are enabled.
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showLocationServicesDialog()
            return
        }

        // Check Internet connectivity.
        if (!isInternetAvailable()) {
            showInternetDialog()
            return
        }

        // If all checks are passed, proceed.
        onSuccess()
    }

    /**
     * Helper method to obtain the current location using FusedLocationProviderClient.
     */
    private fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location.latitude, location.longitude)
            } else {
                // Optionally, notify the user that location data is temporarily unavailable.
            }
        }
    }

    /**
     * Check if the device has an active Internet connection.
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Show an alert dialog prompting the user to enable location services.
     */
    private fun showLocationServicesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Services Disabled")
            .setMessage("Location services are turned off. Please enable them in Settings for the app to function correctly.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showProceedWithoutPermissionsMessage("Location services are disabled. The app may work incorrectly using standard settings.")
            }
            .show()
    }

    /**
     * Show an alert dialog prompting the user to connect to the Internet.
     */
    private fun showInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("No Internet Connection")
            .setMessage("Internet is not available. Please connect to the Internet to fetch the latest data. You can continue with standard settings, but the app may encounter errors.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showProceedWithoutPermissionsMessage("Internet is disabled. The app may work incorrectly using standard settings.")
            }
            .show()
    }

    /**
     * Show an alert dialog when permissions are denied.
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permissions Denied")
            .setMessage("Location permission is required for the app to work effectively. Without it, you can still continue with standard settings, but the app may encounter errors.")
            .setPositiveButton("Settings") { _, _ ->
                // Open app settings so user can grant permissions.
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:" + requireContext().packageName)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                showProceedWithoutPermissionsMessage("Location permission was denied. The app may work incorrectly using standard settings.")
            }
            .show()
    }

    /**
     * Display a simple message indicating that the app may not function properly.
     */
    private fun showProceedWithoutPermissionsMessage(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Warning")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Handle the result from the permission request.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted; you might try to obtain location again.
            } else {
                showPermissionDeniedDialog()
            }
        }
    }
}
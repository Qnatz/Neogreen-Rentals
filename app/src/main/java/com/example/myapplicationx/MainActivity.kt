package com.example.myapplicationx

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplicationx.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.libraries.places.api.Places
import javax.inject.Inject
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(applicationContext, "AIzaSyBqrxPwuWyejd6iWIYtX8qOMjIq-jwrtSU")
        
        // Set up the custom toolbar as ActionBar
        setSupportActionBar(binding.customToolbar)
        binding.customToolbar.setTitleTextAppearance(this, R.style.CustomToolbarTitle)
        
       window.statusBarColor = ContextCompat.getColor(this, R.color.blue_gradient_center)

        // Initialize NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_buildings,
                R.id.navigation_tenants,
                R.id.navigation_dashboard,
                R.id.navigation_accounts,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        handleBackPress(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = appBarConfiguration.topLevelDestinations.contains(destination.id)
            supportActionBar?.setDisplayHomeAsUpEnabled(!isTopLevel)
            binding.customToolbar.title = destination.label
        }
    
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun handleBackPress(navController: NavController) {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navController.navigateUp()) {
                    finish()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
}
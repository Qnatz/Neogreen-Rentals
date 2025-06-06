package com.example.myapplicationx.ui.settings

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplicationx.ui.settings.services.ServiceFragment
import com.example.myapplicationx.ui.settings.taxes.TaxesFragment

class ServicesTaxesPager(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ServiceFragment() // Fragment displaying the list of services
            1 -> TaxesFragment()   // Fragment displaying the list of taxes
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
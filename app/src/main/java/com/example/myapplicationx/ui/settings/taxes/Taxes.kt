package com.example.myapplicationx.ui.settings.taxes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Taxes(
    val name: String,
    val percentage: Double,
    val isInclusive: Boolean
) : Parcelable
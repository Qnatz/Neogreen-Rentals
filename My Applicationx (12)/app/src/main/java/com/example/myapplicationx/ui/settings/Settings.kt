package com.example.myapplicationx.ui.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Settings(
    val settingId: Int,
    val settingName: String
) : Parcelable
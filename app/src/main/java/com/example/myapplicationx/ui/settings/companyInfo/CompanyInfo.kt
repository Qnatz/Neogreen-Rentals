package com.example.myapplicationx.ui.settings.companyInfo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompanyInfo(
    var companyId: Int = 0,
    var companyName: String = "",
    var email: String = "",
    var primaryPhone: String = "",
    var secondaryPhone: String = "",
    var address: String = "",
    var website: String = ""
) : Parcelable
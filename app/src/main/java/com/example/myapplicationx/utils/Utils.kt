package com.example.myapplicationx.utils

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    action: (() -> Unit)? = null,
) {
    val snackbar = Snackbar.make(this, message, duration)
    actionText?.let {
        snackbar.setAction(actionText) { action?.invoke() }
    }
    snackbar.show()
}

fun View.showSnackbar(
    @StringRes messageRes: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @StringRes actionTextRes: Int? = null,
    action: (() -> Unit)? = null,
) {
    val snackbar = Snackbar.make(this, messageRes, duration)
    actionTextRes?.let {
        snackbar.setAction(it) { action?.invoke() }
    }
    snackbar.show()
}

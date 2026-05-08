package com.mybrain.playlistmaker.presentation.navigation

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mybrain.playlistmaker.R

fun showStyledSnackbar(
    activity: Activity,
    message: String,
    onShow: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val rootView = activity.findViewById<android.view.View>(android.R.id.content)
    val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
    val snackbarView = snackbar.view
    snackbarView.setBackgroundResource(R.drawable.snackbar_black_rounded)
    snackbar.setTextColor(ContextCompat.getColor(activity, R.color.second_background))
    val snackbarHeight =
        activity.resources.getDimensionPixelSize(R.dimen.snackbar_max_height)
    snackbarView.layoutParams.height = snackbarHeight
    val textView =
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView?.apply {
        setTextSize(
            android.util.TypedValue.COMPLEX_UNIT_PX,
            activity.resources.getDimension(R.dimen.text_size_14),
        )
        gravity = Gravity.CENTER
    }
    val params = snackbarView.layoutParams as? ViewGroup.MarginLayoutParams
    if (params != null) {
        val horizontal =
            activity.resources.getDimensionPixelSize(R.dimen.snackbar_margin_horizontal)
        val bottom = activity.resources.getDimensionPixelSize(R.dimen.snackbar_margin_bottom)
        params.setMargins(horizontal, params.topMargin, horizontal, bottom)
        snackbarView.layoutParams = params
    }
    onShow()
    snackbar.addCallback(
        object : Snackbar.Callback() {
            override fun onDismissed(
                transientBottomBar: Snackbar?,
                event: Int,
            ) {
                onDismiss()
            }
        },
    )
    snackbar.show()
}

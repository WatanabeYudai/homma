package com.example.bookmanager.utils

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class ViewUtil {
    companion object {
        fun showSnackBarLong(view: View, msg: String) {
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
        }

        fun showToastLong(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

        fun getDisplayWidth(context: Context): Int {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val point = Point().also {
                display.getRealSize(it)
            }
            return point.x
        }

        fun dpToPx(context: Context, dp: Int): Int {
            val metrics = context.resources.displayMetrics
            return (dp * metrics.density).toInt()
        }

        fun pxToDp(context: Context, px: Int): Int {
            val metrics = context.resources.displayMetrics
            return (px / metrics.density).toInt()
        }
    }
}
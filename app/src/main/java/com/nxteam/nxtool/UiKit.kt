package com.nxteam.nxtool

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat

object UiKit {

    fun Context.dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    fun Context.color(id: Int): Int = ContextCompat.getColor(this, id)

    fun card(context: Context, radiusDp: Int = 14): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * context.resources.displayMetrics.density
            setColor(ContextCompat.getColor(context, R.color.nx_surface))
            setStroke(
                (1 * context.resources.displayMetrics.density).toInt(),
                ContextCompat.getColor(context, R.color.nx_outline)
            )
        }
    }

    fun accentButton(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18 * context.resources.displayMetrics.density
            colors = intArrayOf(
                ContextCompat.getColor(context, R.color.nx_accent),
                ContextCompat.getColor(context, R.color.nx_accent_dim)
            )
            orientation = GradientDrawable.Orientation.TL_BR
        }
    }

    fun pill(context: Context, filled: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 999f
            if (filled) {
                setColor(ContextCompat.getColor(context, R.color.nx_accent_dim))
            } else {
                setColor(Color.TRANSPARENT)
                setStroke(
                    (1 * context.resources.displayMetrics.density).toInt(),
                    ContextCompat.getColor(context, R.color.nx_outline)
                )
            }
        }
    }
}

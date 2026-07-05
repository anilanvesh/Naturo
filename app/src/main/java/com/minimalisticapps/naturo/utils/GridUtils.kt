package com.minimalisticapps.naturo.utils

import android.content.Context

object GridUtils {
    /**
     * Calculates the optimal number of columns for the grid based on screen width.
     */
    fun calculateOptimalSpanCount(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        
        // Assume a target column width of 110dp
        val columnWidthDp = 110
        val spanCount = (screenWidthDp / columnWidthDp).toInt()
        
        // Return at least 2 columns for a better look on small screens, or 1 if it's really tiny
        return if (spanCount > 1) spanCount else 2
    }
}

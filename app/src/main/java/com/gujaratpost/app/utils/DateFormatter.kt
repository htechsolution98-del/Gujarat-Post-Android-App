package com.gujaratpost.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("gu", "IN"))

    fun formatIsoDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = try {
                isoFormat.parse(isoDate)
            } catch (e: Exception) {
                // Fallback format without milliseconds
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(isoDate)
            } ?: return ""

            val now = System.currentTimeMillis()
            val diff = now - date.time

            when {
                diff < 60 * 1000 -> "હમણાં જ" // Just now
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} મિનિટ પહેલા" // X minutes ago
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} કલાક પહેલા" // X hours ago
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} દિવસ પહેલા" // X days ago
                else -> displayDateFormat.format(date)
            }
        } catch (e: Exception) {
            isoDate
        }
    }
}

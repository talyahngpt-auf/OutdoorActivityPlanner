package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val monthDayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getTodayDate(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentDateFormatted(): String {
        return fullDateFormat.format(Date())
    }

    fun formatToDayOfWeek(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString) ?: Date()
            dayFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatToShortDay(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString) ?: Date()
            shortDayFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatToMonthDay(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString) ?: Date()
            monthDayFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }
}
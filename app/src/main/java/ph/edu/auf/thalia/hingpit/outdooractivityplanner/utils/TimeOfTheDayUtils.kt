package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity

object TimeOfDayUtils {
    fun getTimeOfDay(hour: Int): String {
        return when (hour) {
            in 5..11 -> "🌅 Morning"
            in 12..17 -> "☀️ Afternoon"
            in 18..21 -> "🌆 Evening"
            else -> "🌙 Night"



        }
    }
    fun parseHourFromTime(time: String): Int {
        return try {
            time.split(":").firstOrNull()?.toIntOrNull() ?: 12
        } catch (e: Exception) {
            12
        }
    }

    fun groupActivitiesByTimeOfDay(activities: List<ActivityEntity>): Map<String, List<ActivityEntity>> {
        return activities.groupBy { activity ->
            val hour = parseHourFromTime(activity.time)
            getTimeOfDay(hour)
        }.toSortedMap(compareBy { timeOfDay ->
            when {
                timeOfDay.contains("Morning") -> 1
                timeOfDay.contains("Afternoon") -> 2
                timeOfDay.contains("Evening") -> 3
                else -> 4
            }
        })
    }

    fun getSuggestedTime(timeOfDay: String): String {
        return when (timeOfDay.lowercase()) {
            "morning" -> "08:00"
            "afternoon" -> "14:00"
            "evening" -> "18:00"
            "night" -> "20:00"
            else -> "12:00"
        }
    }
}
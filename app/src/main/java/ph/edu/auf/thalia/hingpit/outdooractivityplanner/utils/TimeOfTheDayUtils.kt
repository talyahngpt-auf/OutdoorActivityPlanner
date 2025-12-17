package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity


object TimeOfDayUtils {


    fun getTimeOfDay(hour: Int): String {
        return TimeOfDay.fromHour(hour).label
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
        }.toSortedMap(compareBy { label ->
            TimeOfDay.values().indexOfFirst { it.label == label }
        })
    }

    fun getSuggestedTime(timeOfDayLabel: String): String {
        val timeOfDay = TimeOfDay.values().find {
            it.label.equals(timeOfDayLabel, ignoreCase = true) ||
                    it.name.equals(timeOfDayLabel, ignoreCase = true)
        } ?: TimeOfDay.NOON

        return when (timeOfDay) {
            TimeOfDay.MIDNIGHT -> "02:00"
            TimeOfDay.MORNING -> "08:00"
            TimeOfDay.NOON -> "12:00"
            TimeOfDay.AFTERNOON -> "15:00"
            TimeOfDay.EVENING -> "18:30"
            TimeOfDay.ANYTIME -> "12:00"
        }
    }
}
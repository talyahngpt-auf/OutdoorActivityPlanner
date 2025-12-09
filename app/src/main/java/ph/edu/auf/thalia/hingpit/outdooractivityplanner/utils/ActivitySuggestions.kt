package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

enum class WeatherCondition {
    CLEAR, CLOUDS, RAIN, DRIZZLE, THUNDERSTORM,
    MIST, FOG, HAZE, SMOKE;

    companion object {
        fun fromString(condition: String): WeatherCondition {
            return values().find {
                it.name.equals(condition, ignoreCase = true)
            } ?: CLEAR
        }
    }
}

enum class TempCategory(val range: String, val description: String) {
    COOL("≤23°C", "Cool and comfortable"),
    COMFORTABLE("24-28°C", "Pleasant temperature"),
    WARM("29-31°C", "Warm but manageable"),
    HOT("32-35°C", "Hot, seek shade and hydration"),
    SCORCHING(">35°C", "Extreme heat, stay indoors if possible");

    companion object {
        fun fromTemp(celsius: Double): TempCategory {
            return when {
                celsius <= 23.0 -> COOL
                celsius <= 28.0 -> COMFORTABLE
                celsius <= 31.0 -> WARM
                celsius <= 35.0 -> HOT
                else -> SCORCHING
            }
        }
    }
}

enum class TimeOfDay(val label: String, val hourRange: IntRange) {
    MIDNIGHT("🌃 Midnight", 0..4),
    MORNING("🌅 Morning", 5..11),
    NOON("☀️ Noon", 12..12),
    AFTERNOON("☀️ Afternoon", 13..17),
    EVENING("🌆 Evening", 18..23),
    ANYTIME("Anytime", 0..23);

    companion object {
        fun fromHour(hour: Int): TimeOfDay {
            return values().find { hour in it.hourRange } ?: ANYTIME
        }

        fun getCurrentTimeOfDay(): TimeOfDay {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return fromHour(hour)
        }
    }
}

enum class WeatherSuitability(val score: Int) {
    PERFECT(4),
    GOOD(3),
    ACCEPTABLE(2),
    AVOID(0);
}

enum class LocationType {
    INDOOR,
    OUTDOOR,
    FLEXIBLE;

    fun matches(required: LocationType): Boolean {
        return this == required || this == FLEXIBLE || required == FLEXIBLE
    }

    fun toDisplayString(): String {
        return when (this) {
            INDOOR -> "Indoor"
            OUTDOOR -> "Outdoor"
            FLEXIBLE -> "Outdoor and Indoor"
        }
    }
}

enum class IntensityLevel {
    LIGHT,
    MODERATE,
    HEAVY;
}


enum class ActivityCategory {
    FOOD, FITNESS, LEISURE, CULTURAL, SHOPPING,
    NATURE, SOCIAL, WELLNESS, ENTERTAINMENT, EDUCATIONAL,
    FAMILY;

    fun toDisplayString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

// ============================================================
// PRE-DEFINED WEATHER SUITABILITY MAPS
// ============================================================

val OUTDOOR_CLEAR = mapOf(
    WeatherCondition.CLEAR to WeatherSuitability.PERFECT,
    WeatherCondition.CLOUDS to WeatherSuitability.PERFECT,
    WeatherCondition.MIST to WeatherSuitability.GOOD,
    WeatherCondition.FOG to WeatherSuitability.GOOD,
    WeatherCondition.HAZE to WeatherSuitability.ACCEPTABLE,
    WeatherCondition.DRIZZLE to WeatherSuitability.ACCEPTABLE,
    WeatherCondition.RAIN to WeatherSuitability.AVOID,
    WeatherCondition.THUNDERSTORM to WeatherSuitability.AVOID
)

val INDOOR_ANY = mapOf(
    WeatherCondition.CLEAR to WeatherSuitability.GOOD,
    WeatherCondition.CLOUDS to WeatherSuitability.GOOD,
    WeatherCondition.MIST to WeatherSuitability.GOOD,
    WeatherCondition.FOG to WeatherSuitability.GOOD,
    WeatherCondition.HAZE to WeatherSuitability.GOOD,
    WeatherCondition.DRIZZLE to WeatherSuitability.PERFECT,
    WeatherCondition.RAIN to WeatherSuitability.PERFECT,
    WeatherCondition.THUNDERSTORM to WeatherSuitability.PERFECT
)

val WATER_ACTIVITY = mapOf(
    WeatherCondition.CLEAR to WeatherSuitability.PERFECT,
    WeatherCondition.CLOUDS to WeatherSuitability.PERFECT,
    WeatherCondition.DRIZZLE to WeatherSuitability.GOOD,
    WeatherCondition.RAIN to WeatherSuitability.ACCEPTABLE,
    WeatherCondition.THUNDERSTORM to WeatherSuitability.AVOID
)

val COVERED_OUTDOOR = mapOf(
    WeatherCondition.CLEAR to WeatherSuitability.PERFECT,
    WeatherCondition.CLOUDS to WeatherSuitability.PERFECT,
    WeatherCondition.MIST to WeatherSuitability.GOOD,
    WeatherCondition.DRIZZLE to WeatherSuitability.GOOD,
    WeatherCondition.RAIN to WeatherSuitability.ACCEPTABLE,
    WeatherCondition.THUNDERSTORM to WeatherSuitability.AVOID
)

// ============================================================
// ENHANCED ACTIVITY DATA CLASS
// ============================================================

data class Activity(
    val title: String,
    val description: String,
    val icon: String,
    val weatherSuitability: Map<WeatherCondition, WeatherSuitability>,
    val tempCategories: List<TempCategory>,
    val timesOfDay: List<TimeOfDay>,
    val locationType: LocationType,
    val intensity: IntensityLevel,
    val category: ActivityCategory
) {
    fun isSuitableFor(
        weather: WeatherCondition,
        acceptableLevels: List<WeatherSuitability> = listOf(
            WeatherSuitability.PERFECT,
            WeatherSuitability.GOOD
        )
    ): Boolean {
        val suitability = weatherSuitability[weather] ?: return false
        return suitability in acceptableLevels
    }

    fun getSuitability(weather: WeatherCondition): WeatherSuitability? {
        return weatherSuitability[weather]
    }

    fun getScore(weather: WeatherCondition): Int {
        return getSuitability(weather)?.score ?: 0
    }
}

fun createActivity(
    title: String,
    description: String,
    icon: String,
    weather: Map<WeatherCondition, WeatherSuitability>,
    temps: List<TempCategory>,
    times: List<TimeOfDay>,
    location: LocationType,
    intensity: IntensityLevel,
    category: ActivityCategory
) = Activity(title, description, icon, weather, temps, times, location, intensity, category)

// ============================================================
// ACTIVITY MASTER LIST
// ============================================================

object ActivityMasterList {

    val activities = buildList {
        // CLEAR WEATHER ACTIVITIES
        add(createActivity(
            title = "Early Morning Jog",
            description = "Perfect cool weather for running around the village",
            icon = "🏃",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.MORNING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Morning Coffee at Café",
            description = "Hot drinks in cozy atmosphere",
            icon = "☕",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Park Stroll",
            description = "Relaxing walk in cool breeze",
            icon = "🌳",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.NATURE
        ))

        add(createActivity(
            title = "Buy Taho",
            description = "Catch the taho vendor for warm sweet snack",
            icon = "🥛",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM, TempCategory.COOL),
            times = listOf(TimeOfDay.MORNING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Palengke Run",
            description = "Visit the local market for fresh ingredients",
            icon = "🛒",
            weather = COVERED_OUTDOOR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SHOPPING
        ))

        add(createActivity(
            title = "Bike Around the Barangay",
            description = "Cycle through quiet village streets",
            icon = "🚴",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Basketball at the Court",
            description = "Play hoops at the barangay covered court",
            icon = "🏀",
            weather = COVERED_OUTDOOR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.HEAVY,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Ukay-Ukay Shopping",
            description = "Hunt for bargain clothes at thrift shops",
            icon = "👕",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.WARM, TempCategory.HOT, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SHOPPING
        ))

        add(createActivity(
            title = "Karinderya Food Trip",
            description = "Try affordable home-cooked meals",
            icon = "🍜",
            weather = COVERED_OUTDOOR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM, TempCategory.HOT),
            times = listOf(TimeOfDay.NOON, TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.FLEXIBLE,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Halo-Halo Break",
            description = "Cool down with Philippines' iconic dessert",
            icon = "🍧",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.HOT, TempCategory.SCORCHING),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Mall Strolling",
            description = "Escape the heat and window shop",
            icon = "🛍️",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.HOT, TempCategory.SCORCHING),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SHOPPING
        ))

        add(createActivity(
            title = "Swimming at Resort",
            description = "Cool off at local swimming pool",
            icon = "🏊",
            weather = WATER_ACTIVITY,
            temps = listOf(TempCategory.HOT, TempCategory.SCORCHING),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Dirty Ice Cream",
            description = "Buy sorbetes from the street vendor",
            icon = "🍦",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.HOT, TempCategory.SCORCHING),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        // RAINY WEATHER ACTIVITIES
        add(createActivity(
            title = "Movie Marathon at Home",
            description = "Binge-watch shows while it rains",
            icon = "🎬",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.ENTERTAINMENT
        ))

        add(createActivity(
            title = "Lugaw/Goto Break",
            description = "Hot rice porridge perfect for rainy weather",
            icon = "🍲",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.EVENING),
            location = LocationType.FLEXIBLE,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Home Workout",
            description = "Bodyweight exercises indoors",
            icon = "💪",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Cozy Café Time",
            description = "Warm drinks watching the rain",
            icon = "☕",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Indoor Badminton",
            description = "Play at covered court despite rain",
            icon = "🏸",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Tambay sa Tindahan",
            description = "Chill at the sari-sari store",
            icon = "🏪",
            weather = COVERED_OUTDOOR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.EVENING, TimeOfDay.MIDNIGHT),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SOCIAL
        ))

        add(createActivity(
            title = "Videoke/Karaoke",
            description = "Sing your heart out with friends",
            icon = "🎤",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM, TempCategory.HOT),
            times = listOf(TimeOfDay.EVENING),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.ENTERTAINMENT
        ))

        add(createActivity(
            title = "Night Market Walk",
            description = "Browse tiangge stalls",
            icon = "🌃",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SHOPPING
        ))

        add(createActivity(
            title = "Midnight Mami Run",
            description = "Late-night noodle soup adventure",
            icon = "🍜",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.COOL),
            times = listOf(TimeOfDay.MIDNIGHT, TimeOfDay.EVENING),
            location = LocationType.FLEXIBLE,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FOOD
        ))

        // ANYTIME ACTIVITIES
        add(createActivity(
            title = "Reading",
            description = "Books, comics, or online articles",
            icon = "📖",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.FLEXIBLE,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.EDUCATIONAL
        ))

        add(createActivity(
            title = "Mobile Gaming",
            description = "Mobile Legends, COD Mobile, or other games",
            icon = "🎮",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM, TempCategory.HOT),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.FLEXIBLE,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.ENTERTAINMENT
        ))

        add(createActivity(
            title = "Picnic at the Park",
            description = "Enjoy homemade snacks outdoors in a relaxing setting",
            icon = "🧺",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.LEISURE
        ))

        add(createActivity(
            title = "Sketch or Draw",
            description = "Bring a sketchbook and enjoy some creative time",
            icon = "✏️",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.FLEXIBLE,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.EDUCATIONAL
        ))

        add(createActivity(
            title = "Game Night with Friends and Family",
            description = "Play card games or console games at home",
            icon = "🎲",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM, TempCategory.HOT),
            times = listOf(TimeOfDay.EVENING, TimeOfDay.MIDNIGHT),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SOCIAL
        ))

        add(createActivity(
            title = "Cook or Bake at Home",
            description = "Try new recipes and enjoy homemade food",
            icon = "🍳",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FOOD
        ))

        add(createActivity(
            title = "Art Workshop",
            description = "Attend a class to learn painting or crafts",
            icon = "🎨",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.AFTERNOON),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.CULTURAL
        ))

        add(createActivity(
            title = "Play Board Games with Family",
            description = "Bond over classic board games or puzzles",
            icon = "♟️",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.FAMILY
        ))

        add(createActivity(
            title = "Grocery Shopping",
            description = "Stock up on essentials at the local store",
            icon = "🛒",
            weather = COVERED_OUTDOOR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.SHOPPING
        ))

        add(createActivity(
            title = "Hike a Nearby Trail",
            description = "Enjoy nature and get a good workout on local trails",
            icon = "🥾",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.HEAVY,
            category = ActivityCategory.NATURE
        ))

        add(createActivity(
            title = "Swimming",
            description = "Cool off under the sun",
            icon = "🏖️",
            weather = WATER_ACTIVITY,
            temps = listOf(TempCategory.HOT, TempCategory.SCORCHING),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.FITNESS
        ))

        add(createActivity(
            title = "Evening Walk in the Park",
            description = "Relaxing stroll to enjoy sunset",
            icon = "🌇",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.LEISURE
        ))

        add(createActivity(
            title = "Gardening",
            description = "Plant flowers or vegetables at home or community garden",
            icon = "🌱",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.MODERATE,
            category = ActivityCategory.NATURE
        ))

        add(createActivity(
            title = "Neighborhood Volleyball",
            description = "Friendly volleyball game with neighbors",
            icon = "🏐",
            weather = COVERED_OUTDOOR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.HEAVY,
            category = ActivityCategory.SOCIAL
        ))

        add(createActivity(
            title = "Meditation Session",
            description = "Relax and focus your mind indoors",
            icon = "🪷",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.ANYTIME),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.WELLNESS
        ))

        add(createActivity(
            title = "Concert or Live Music",
            description = "Enjoy a live performance at a local venue",
            icon = "🎵",
            weather = OUTDOOR_CLEAR,
            temps = listOf(TempCategory.COMFORTABLE, TempCategory.WARM),
            times = listOf(TimeOfDay.EVENING),
            location = LocationType.OUTDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.ENTERTAINMENT
        ))

        add(createActivity(
            title = "Visit Museum or Exhibit",
            description = "Learn about history, art, or science",
            icon = "🏛️",
            weather = INDOOR_ANY,
            temps = listOf(TempCategory.COOL, TempCategory.COMFORTABLE),
            times = listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON),
            location = LocationType.INDOOR,
            intensity = IntensityLevel.LIGHT,
            category = ActivityCategory.EDUCATIONAL
        ))
    }

    fun getSuggestions(
        weatherCondition: WeatherCondition,
        temperature: Double,
        currentTime: TimeOfDay = TimeOfDay.getCurrentTimeOfDay(),
        limit: Int = 5,
        preferCurrentTime: Boolean = true,
        excludeCategories: List<ActivityCategory> = emptyList(),
        excludeIntensity: List<IntensityLevel> = emptyList()
    ): List<Activity> {
        val tempCategory = TempCategory.fromTemp(temperature)

        val filtered = activities.filter { activity ->
            val weatherOK = activity.isSuitableFor(
                weatherCondition,
                listOf(WeatherSuitability.PERFECT, WeatherSuitability.GOOD, WeatherSuitability.ACCEPTABLE)
            )
            val tempOK = tempCategory in activity.tempCategories
            val categoryOK = activity.category !in excludeCategories
            val intensityOK = activity.intensity !in excludeIntensity

            weatherOK && tempOK && categoryOK && intensityOK
        }

        val timeMatching = filtered.filter { activity ->
            currentTime in activity.timesOfDay || TimeOfDay.ANYTIME in activity.timesOfDay
        }

        val timeNotMatching = filtered.filter { activity ->
            currentTime !in activity.timesOfDay && TimeOfDay.ANYTIME !in activity.timesOfDay
        }

        val sortedTimeMatching = timeMatching.sortedByDescending { it.getScore(weatherCondition) }
        val sortedTimeNotMatching = timeNotMatching.sortedByDescending { it.getScore(weatherCondition) }

        val prioritized = if (preferCurrentTime) {
            sortedTimeMatching + sortedTimeNotMatching
        } else {
            (sortedTimeMatching + sortedTimeNotMatching).shuffled()
        }

        return prioritized.take(limit * 2).shuffled().take(limit)
    }

    fun getSuggestionsLegacy(
        condition: String,
        temperature: Double,
        limit: Int = 5
    ): List<ActivitySuggestion> {
        val weatherCondition = WeatherCondition.fromString(condition)
        val activities = getSuggestions(weatherCondition, temperature, limit = limit)

        return activities.map { activity ->
            ActivitySuggestion(
                title = activity.title,
                description = activity.description,
                category = activity.category.toDisplayString(),
                icon = activity.icon,
                timeOfDay = activity.timesOfDay.firstOrNull()?.name?.lowercase(),
                location = activity.locationType.toDisplayString()
            )
        }
    }
}

/**
 * Legacy data class for backward compatibility
 */
data class ActivitySuggestion(
    val title: String,
    val description: String,
    val category: String,
    val icon: String,
    val timeOfDay: String? = null,
    val location: String? = null
)

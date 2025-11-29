package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

import java.util.Calendar

data class ActivitySuggestion(
    val title: String,
    val description: String,
    val category: String,
    val icon: String,
    val timeOfDay: String? = null // morning, afternoon, evening, night, anytime
)

object ActivitySuggestions {

    // Get time-appropriate activity suggestions
    fun getSuggestions(condition: String, temperature: Double, limit: Int = 5): List<ActivitySuggestion> {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (currentHour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
        }

        // Get all possible activities
        val allActivities = when {
            temperature > 30 -> getHotWeatherActivities(condition)
            temperature < 15 -> getColdWeatherActivities(condition)
            else -> getPleasantWeatherActivities(condition)
        }

        // Filter by time of day
        val timeAppropriate = allActivities.filter { activity ->
            activity.timeOfDay == null || activity.timeOfDay == "anytime" || activity.timeOfDay == timeOfDay
        }

        // If not enough time-appropriate activities, add "anytime" activities
        return if (timeAppropriate.size >= limit) {
            timeAppropriate.shuffled().take(limit)
        } else {
            (timeAppropriate + allActivities.filter { it.timeOfDay == "anytime" })
                .distinct()
                .shuffled()
                .take(limit)
        }
    }

    private fun getHotWeatherActivities(condition: String): List<ActivitySuggestion> {
        return when (condition.lowercase()) {
            "clear", "sunny" -> listOf(
                ActivitySuggestion("Early Morning Walk", "Take a walk before it gets too hot (5-7 AM)", "outdoor", "🌅", "morning"),
                ActivitySuggestion("Swimming at the Pool", "Cool off with a refreshing swim", "outdoor", "🏊", "afternoon"),
                ActivitySuggestion("Visit an Ice Cream Shop", "Treat yourself to your favorite flavor", "leisure", "🍦", "afternoon"),
                ActivitySuggestion("Indoor Mall Shopping", "Stay cool while browsing stores", "indoor", "🛍️", "anytime"),
                ActivitySuggestion("Movie Marathon at Home", "Watch movies in air-conditioned comfort", "indoor", "🎬", "anytime"),
                ActivitySuggestion("Visit a Water Park", "Enjoy water slides and wave pools", "outdoor", "🏄", "afternoon"),
                ActivitySuggestion("Indoor Bowling", "Fun activity in air-conditioned space", "indoor", "🎳", "anytime"),
                ActivitySuggestion("Smoothie Making", "Blend refreshing cold drinks", "indoor", "🥤", "anytime"),
                ActivitySuggestion("Evening Beach Walk", "Enjoy the sunset by the water", "outdoor", "🌊", "evening"),
                ActivitySuggestion("Indoor Gym Session", "Work out in air-conditioned comfort", "fitness", "💪", "anytime")
            )
            "rain", "drizzle", "thunderstorm" -> getIndoorActivities()
            "clouds", "cloudy" -> listOf(
                ActivitySuggestion("Indoor Exercise", "Work out at an air-conditioned gym", "fitness", "🏋️", "anytime"),
                ActivitySuggestion("Visit a Café", "Enjoy cold drinks in a cool café", "leisure", "☕", "anytime"),
                ActivitySuggestion("Photography Walk", "Capture cloud formations", "outdoor", "📸", "afternoon"),
                ActivitySuggestion("Shopping Mall", "Browse stores comfortably", "indoor", "🛒", "anytime"),
                ActivitySuggestion("Indoor Badminton", "Play in an air-conditioned court", "fitness", "🏸", "anytime")
            )
            else -> getGeneralIndoorActivities()
        }
    }

    private fun getColdWeatherActivities(condition: String): List<ActivitySuggestion> {
        return when (condition.lowercase()) {
            "clear", "sunny" -> listOf(
                ActivitySuggestion("Morning Jog", "Perfect temperature for a run", "fitness", "🏃", "morning"),
                ActivitySuggestion("Outdoor Picnic", "Pack warm drinks and enjoy the cool weather", "outdoor", "🧺", "afternoon"),
                ActivitySuggestion("Visit a Park", "Relaxing walk in cool breeze", "outdoor", "🌳", "afternoon"),
                ActivitySuggestion("Outdoor Photography", "Capture the beautiful clear day", "outdoor", "📷", "anytime"),
                ActivitySuggestion("Cycling", "Perfect weather for a bike ride", "fitness", "🚴", "morning"),
                ActivitySuggestion("Hiking", "Enjoy trails in cool weather", "outdoor", "⛰️", "morning"),
                ActivitySuggestion("Outdoor Yoga", "Practice in refreshing air", "fitness", "🧘", "morning"),
                ActivitySuggestion("Visit Botanical Garden", "Enjoy flowers in cool weather", "outdoor", "🌺", "afternoon"),
                ActivitySuggestion("Street Food Tour", "Try local food in comfortable weather", "leisure", "🍜", "evening"),
                ActivitySuggestion("Evening Coffee", "Warm drinks at a cozy café", "leisure", "☕", "evening")
            )
            "rain", "drizzle", "thunderstorm" -> getIndoorActivities()
            "clouds", "cloudy" -> listOf(
                ActivitySuggestion("Cozy Coffee Shop", "Enjoy hot drinks", "leisure", "☕", "anytime"),
                ActivitySuggestion("Light Outdoor Walk", "Peaceful walk in cool weather", "outdoor", "🚶", "afternoon"),
                ActivitySuggestion("Read at the Park", "Find a cozy spot with a book", "leisure", "📚", "afternoon"),
                ActivitySuggestion("Visit Art Gallery", "Explore indoor exhibits", "indoor", "🎨", "anytime"),
                ActivitySuggestion("Cooking Class", "Learn new recipes indoors", "indoor", "👨‍🍳", "anytime")
            )
            else -> getGeneralIndoorActivities()
        }
    }

    private fun getPleasantWeatherActivities(condition: String): List<ActivitySuggestion> {
        return when (condition.lowercase()) {
            "clear", "sunny" -> listOf(
                ActivitySuggestion("Hiking Adventure", "Perfect weather for trails", "outdoor", "⛰️", "morning"),
                ActivitySuggestion("Outdoor Picnic", "Pack snacks and enjoy sunshine", "outdoor", "🧺", "afternoon"),
                ActivitySuggestion("Beach Day", "Soak up the sun", "outdoor", "🏖️", "afternoon"),
                ActivitySuggestion("Outdoor Sports", "Play basketball or volleyball", "fitness", "⚽", "afternoon"),
                ActivitySuggestion("Visit a Garden", "Explore botanical gardens", "outdoor", "🌺", "afternoon"),
                ActivitySuggestion("Morning Run", "Start your day with exercise", "fitness", "🏃", "morning"),
                ActivitySuggestion("Outdoor Breakfast", "Dine alfresco", "leisure", "🥞", "morning"),
                ActivitySuggestion("Kayaking", "Enjoy water activities", "outdoor", "🚣", "afternoon"),
                ActivitySuggestion("Farmers Market", "Browse fresh local produce", "outdoor", "🥕", "morning"),
                ActivitySuggestion("Sunset Viewing", "Find a scenic spot", "outdoor", "🌅", "evening"),
                ActivitySuggestion("Outdoor Concert", "Enjoy live music", "leisure", "🎵", "evening"),
                ActivitySuggestion("Bike Tour", "Explore the city on two wheels", "fitness", "🚲", "morning")
            )
            "rain", "drizzle", "thunderstorm" -> getIndoorActivities()
            "clouds", "cloudy" -> listOf(
                ActivitySuggestion("Photography Walk", "Capture moody shots", "outdoor", "📸", "afternoon"),
                ActivitySuggestion("Café Hopping", "Try different coffee shops", "leisure", "☕", "anytime"),
                ActivitySuggestion("Light Jogging", "Comfortable running weather", "fitness", "🏃", "morning"),
                ActivitySuggestion("Visit Local Markets", "Browse without harsh sun", "leisure", "🛒", "afternoon"),
                ActivitySuggestion("Outdoor Meditation", "Find peace in nature", "fitness", "🧘", "morning"),
                ActivitySuggestion("Sketching Outdoors", "Draw in natural light", "leisure", "✏️", "afternoon")
            )
            else -> getGeneralIndoorActivities()
        }
    }

    private fun getIndoorActivities(): List<ActivitySuggestion> {
        return listOf(
            ActivitySuggestion("Bake Something Delicious", "Try a new recipe", "indoor", "🍪", "anytime"),
            ActivitySuggestion("Movie Marathon", "Catch up on series or movies", "indoor", "🎬", "anytime"),
            ActivitySuggestion("Read a Book", "Dive into a good book", "indoor", "📚", "anytime"),
            ActivitySuggestion("Indoor Workout", "Follow a home routine", "fitness", "🧘", "anytime"),
            ActivitySuggestion("Learn Something New", "Take an online course", "indoor", "💻", "anytime"),
            ActivitySuggestion("Board Games Night", "Play with friends or family", "indoor", "🎲", "evening"),
            ActivitySuggestion("Art and Crafts", "Get creative with DIY projects", "indoor", "🎨", "anytime"),
            ActivitySuggestion("Cooking Challenge", "Try a complex recipe", "indoor", "🍳", "anytime"),
            ActivitySuggestion("Video Gaming", "Enjoy your favorite games", "indoor", "🎮", "anytime"),
            ActivitySuggestion("Meditation Session", "Practice mindfulness indoors", "fitness", "🧘", "anytime"),
            ActivitySuggestion("Journaling", "Write about your day", "indoor", "📝", "evening"),
            ActivitySuggestion("Online Shopping", "Browse for new items", "indoor", "🛍️", "anytime")
        )
    }

    private fun getGeneralIndoorActivities(): List<ActivitySuggestion> {
        return listOf(
            ActivitySuggestion("Visit a Museum", "Explore exhibits", "indoor", "🏛️", "anytime"),
            ActivitySuggestion("Shopping Mall Visit", "Browse and dine indoors", "indoor", "🛍️", "anytime"),
            ActivitySuggestion("Indoor Gym Session", "Controlled environment workout", "fitness", "💪", "anytime"),
            ActivitySuggestion("Cooking New Recipe", "Try something new", "indoor", "🍳", "anytime"),
            ActivitySuggestion("Library Visit", "Discover new books", "indoor", "📚", "anytime"),
            ActivitySuggestion("Spa Day", "Relax and rejuvenate", "indoor", "💆", "anytime"),
            ActivitySuggestion("Rock Climbing Gym", "Indoor climbing adventure", "fitness", "🧗", "anytime"),
            ActivitySuggestion("Karaoke Night", "Sing your heart out", "leisure", "🎤", "evening")
        )
    }
}
package ph.edu.auf.thalia.hingpit.outdooractivityplanner.utils

data class ActivitySuggestion(
    val title: String,
    val description: String,
    val category: String, // outdoor, indoor, fitness, leisure
    val icon: String // emoji for now, can be replaced with actual icons
)

object ActivitySuggestions {

    // Get activity suggestions based on weather condition and temperature
    fun getSuggestions(condition: String, temperature: Double): List<ActivitySuggestion> {
        return when {
            // Hot weather (above 30°C)
            temperature > 30 -> getHotWeatherActivities(condition)

            // Cold weather (below 15°C)
            temperature < 15 -> getColdWeatherActivities(condition)

            // Pleasant weather (15-30°C)
            else -> getPleasantWeatherActivities(condition)
        }
    }

    private fun getHotWeatherActivities(condition: String): List<ActivitySuggestion> {
        return when (condition.lowercase()) {
            "clear", "sunny" -> listOf(
                ActivitySuggestion(
                    "Swimming at the Pool",
                    "Cool off with a refreshing swim. Perfect for hot sunny days!",
                    "outdoor",
                    "🏊"
                ),
                ActivitySuggestion(
                    "Visit an Ice Cream Shop",
                    "Treat yourself to your favorite ice cream flavor",
                    "leisure",
                    "🍦"
                ),
                ActivitySuggestion(
                    "Indoor Mall Shopping",
                    "Stay cool while browsing your favorite stores",
                    "indoor",
                    "🛍️"
                ),
                ActivitySuggestion(
                    "Movie Marathon at Home",
                    "Watch movies in air-conditioned comfort",
                    "indoor",
                    "🎬"
                ),
                ActivitySuggestion(
                    "Early Morning Walk",
                    "Take a walk before it gets too hot (5-7 AM)",
                    "outdoor",
                    "🌅"
                )
            )
            "rain", "drizzle", "thunderstorm" -> getIndoorActivities()
            "clouds", "cloudy", "mist", "fog" -> listOf(
                ActivitySuggestion(
                    "Indoor Exercise",
                    "Work out at an air-conditioned gym or at home",
                    "fitness",
                    "🏋️"
                ),
                ActivitySuggestion(
                    "Visit a Café",
                    "Enjoy cold drinks in a cool café",
                    "leisure",
                    "☕"
                ),
                ActivitySuggestion(
                    "Photography Walk",
                    "Capture interesting cloud formations",
                    "outdoor",
                    "📸"
                )
            )
            else -> getGeneralIndoorActivities()
        }
    }

    private fun getColdWeatherActivities(condition: String): List<ActivitySuggestion> {
        return when (condition.lowercase()) {
            "clear", "sunny" -> listOf(
                ActivitySuggestion(
                    "Morning Jog",
                    "Perfect temperature for a refreshing run",
                    "fitness",
                    "🏃"
                ),
                ActivitySuggestion(
                    "Outdoor Picnic",
                    "Pack warm drinks and enjoy the cool weather",
                    "outdoor",
                    "🧺"
                ),
                ActivitySuggestion(
                    "Visit a Park",
                    "Take a relaxing walk in the cool breeze",
                    "outdoor",
                    "🌳"
                ),
                ActivitySuggestion(
                    "Outdoor Photography",
                    "Capture the beautiful clear day",
                    "outdoor",
                    "📷"
                ),
                ActivitySuggestion(
                    "Cycling",
                    "Perfect weather for a bike ride",
                    "fitness",
                    "🚴"
                )
            )
            "rain", "drizzle", "thunderstorm" -> getIndoorActivities()
            "clouds", "cloudy", "mist", "fog" -> listOf(
                ActivitySuggestion(
                    "Cozy Coffee Shop Visit",
                    "Enjoy hot drinks while watching the clouds",
                    "leisure",
                    "☕"
                ),
                ActivitySuggestion(
                    "Light Outdoor Walk",
                    "Take a peaceful walk in the cool weather",
                    "outdoor",
                    "🚶"
                ),
                ActivitySuggestion(
                    "Read at the Park",
                    "Find a cozy spot and read your favorite book",
                    "leisure",
                    "📚"
                )
            )
            else -> getGeneralIndoorActivities()
        }
    }

    private fun getPleasantWeatherActivities(condition: String): List<ActivitySuggestion> {
        return when (condition.lowercase()) {
            "clear", "sunny" -> listOf(
                ActivitySuggestion(
                    "Hiking Adventure",
                    "Perfect weather to explore nature trails",
                    "outdoor",
                    "⛰️"
                ),
                ActivitySuggestion(
                    "Outdoor Picnic",
                    "Pack your favorite snacks and enjoy the sunshine",
                    "outdoor",
                    "🧺"
                ),
                ActivitySuggestion(
                    "Beach Day",
                    "Soak up the sun and enjoy the waves",
                    "outdoor",
                    "🏖️"
                ),
                ActivitySuggestion(
                    "Outdoor Sports",
                    "Play basketball, volleyball, or badminton",
                    "fitness",
                    "⚽"
                ),
                ActivitySuggestion(
                    "Visit a Garden",
                    "Explore botanical gardens or flower parks",
                    "outdoor",
                    "🌺"
                )
            )
            "rain", "drizzle", "thunderstorm" -> getIndoorActivities()
            "clouds", "cloudy", "mist", "fog" -> listOf(
                ActivitySuggestion(
                    "Photography Walk",
                    "Capture moody atmospheric shots",
                    "outdoor",
                    "📸"
                ),
                ActivitySuggestion(
                    "Café Hopping",
                    "Try out different coffee shops in the area",
                    "leisure",
                    "☕"
                ),
                ActivitySuggestion(
                    "Light Jogging",
                    "Perfect weather for a comfortable run",
                    "fitness",
                    "🏃"
                ),
                ActivitySuggestion(
                    "Visit Local Markets",
                    "Browse outdoor markets without the harsh sun",
                    "leisure",
                    "🛒"
                )
            )
            else -> getGeneralIndoorActivities()
        }
    }

    private fun getIndoorActivities(): List<ActivitySuggestion> {
        return listOf(
            ActivitySuggestion(
                "Bake Something Delicious",
                "Try a new recipe - cookies, cakes, or bread",
                "indoor",
                "🍪"
            ),
            ActivitySuggestion(
                "Movie Marathon",
                "Catch up on your favorite series or movies",
                "indoor",
                "🎬"
            ),
            ActivitySuggestion(
                "Read a Book",
                "Dive into a good book by the window",
                "indoor",
                "📚"
            ),
            ActivitySuggestion(
                "Indoor Workout",
                "Follow a home workout routine or yoga session",
                "fitness",
                "🧘"
            ),
            ActivitySuggestion(
                "Learn Something New",
                "Take an online course or watch educational videos",
                "indoor",
                "💻"
            ),
            ActivitySuggestion(
                "Board Games Night",
                "Gather friends or family for some fun games",
                "indoor",
                "🎲"
            )
        )
    }

    private fun getGeneralIndoorActivities(): List<ActivitySuggestion> {
        return listOf(
            ActivitySuggestion(
                "Visit a Museum",
                "Explore art, history, or science exhibits",
                "indoor",
                "🏛️"
            ),
            ActivitySuggestion(
                "Shopping Mall Visit",
                "Browse stores and enjoy indoor dining",
                "indoor",
                "🛍️"
            ),
            ActivitySuggestion(
                "Indoor Gym Session",
                "Get your workout in a controlled environment",
                "fitness",
                "💪"
            ),
            ActivitySuggestion(
                "Cooking New Recipe",
                "Try making something you've never cooked before",
                "indoor",
                "🍳"
            )
        )
    }
}



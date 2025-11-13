package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class WeatherCache : RealmObject {
    @PrimaryKey
    var city: String = ""
    var temp: Double = 0.0
    var condition: String = ""
    var icon: String = ""
    var humidity: Int = 0
    var wind: Double = 0.0
}

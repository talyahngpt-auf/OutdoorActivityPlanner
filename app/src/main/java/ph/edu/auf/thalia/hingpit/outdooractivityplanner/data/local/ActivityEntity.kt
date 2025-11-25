package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class ActivityEntity : RealmObject {
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var title: String = ""
    var description: String = ""
    var date: String = ""
    var category: String = ""
    var icon: String = ""
    var weatherCondition: String = ""
    var temperature: Double = 0.0
    var isCompleted: Boolean = false
    var createdAt: Long = System.currentTimeMillis()
}

package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val weatherCondition: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

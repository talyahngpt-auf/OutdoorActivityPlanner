package ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.repository


import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.dao.ActivityDao


class ActivityRepository(private val activityDao: ActivityDao) {


    suspend fun insertActivity(activity: ActivityEntity) {
        activityDao.insertActivity(activity)
    }


    suspend fun insertActivities(activities: List<ActivityEntity>) {
        activityDao.insertActivities(activities)
    }


    suspend fun updateActivity(activity: ActivityEntity) {
        activityDao.updateActivity(activity)
    }


    suspend fun deleteActivity(activity: ActivityEntity) {
        activityDao.deleteActivity(activity)
    }


    suspend fun getAllActivities(): List<ActivityEntity> {
        return activityDao.getAllActivities()
    }


    suspend fun getActivitiesByDate(date: String): List<ActivityEntity> {
        return activityDao.getActivitiesByDate(date)
    }


    suspend fun getPendingActivities(): List<ActivityEntity> {
        return activityDao.getPendingActivities()
    }


    suspend fun getCompletedActivities(): List<ActivityEntity> {
        return activityDao.getCompletedActivities()
    }
}

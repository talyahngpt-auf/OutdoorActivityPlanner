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

    suspend fun getAllActivities(userId: String): List<ActivityEntity> {
        return activityDao.getAllActivities(userId)
    }
    suspend fun getActivitiesByDate(userId: String, date: String): List<ActivityEntity> {
        return activityDao.getActivitiesByDate(userId, date)
    }

    suspend fun getPendingActivities(userId: String): List<ActivityEntity> {
        return activityDao.getPendingActivities(userId)
    }

    suspend fun getCompletedActivities(userId: String): List<ActivityEntity> {
        return activityDao.getCompletedActivities(userId)
    }
    suspend fun deleteAllActivitiesByUser(userId: String) {
        activityDao.deleteAllActivitiesByUser(userId)
    }

}

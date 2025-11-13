package ph.edu.auf.thalia.hingpit.outdooractivityplanner

import android.app.Application
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.ActivityEntity
import ph.edu.auf.thalia.hingpit.outdooractivityplanner.data.local.WeatherCache

class App : Application() {

    companion object {
        lateinit var realm: Realm
            private set
    }

    override fun onCreate() {
        super.onCreate()
        val config = RealmConfiguration.Builder(
            schema = setOf(ActivityEntity::class, WeatherCache::class)
        )
            .schemaVersion(1)
            .build()
        realm = Realm.open(config)
    }
}
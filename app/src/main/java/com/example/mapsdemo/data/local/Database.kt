package com.example.mapsdemo.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.NotificationStatus

@Database(entities = [Bump::class, NotificationStatus::class], version = 1 )
abstract class BumpDatabase : RoomDatabase() {
    abstract val bumpDao : BumpDao
    abstract val notificationStatusDao : NotificationStatusDao

    companion object {
        @Volatile
        private var INSTANCE: BumpDatabase? = null

        fun getInstance(context: Context): BumpDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BumpDatabase::class.java,
                        "bumps_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
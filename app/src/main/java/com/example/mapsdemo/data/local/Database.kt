package com.example.mapsdemo.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mapsdemo.data.model.Bump

@Database(entities = [Bump::class], version = 6 )
abstract class BumpDatabase : RoomDatabase() {
    abstract val bumpDao : BumpDao

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
package com.example.tracexcontacttracing.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.tracexcontacttracing.dao.DeviceDao
import com.example.tracexcontacttracing.data.DeviceEntity

@Database(entities = [DeviceEntity::class], version = 1)
abstract class RoomDb : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao?

    companion object {
        private var INSTANCE: RoomDb? = null

        fun getAppDatabase(context: Context): RoomDb? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder<RoomDb>(
                    context.applicationContext,
                    RoomDb::class.java,
                    "demo_db"
                ).allowMainThreadQueries().build()
            }

            return INSTANCE
        }
    }
}
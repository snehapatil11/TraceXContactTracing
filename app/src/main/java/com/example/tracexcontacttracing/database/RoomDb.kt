package com.example.tracexcontacttracing.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.tracexcontacttracing.dao.DeviceDao
import com.example.tracexcontacttracing.dao.UserDeviceDao
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.data.UserDeviceEntity

@Database(entities = [DeviceEntity::class, UserDeviceEntity::class], version = 2)
abstract class RoomDb : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao?
    abstract fun userDeviceDao(): UserDeviceDao?

    companion object {
        private var INSTANCE: RoomDb? = null

        fun getAppDatabase(context: Context): RoomDb? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder<RoomDb>(
                    context.applicationContext,
                    RoomDb::class.java,
                    "demo_db"
                ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
            }

            return INSTANCE
        }
    }
}
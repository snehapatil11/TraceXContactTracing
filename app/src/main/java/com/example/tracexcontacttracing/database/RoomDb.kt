package com.example.tracexcontacttracing.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.tracexcontacttracing.dao.*
import com.example.tracexcontacttracing.data.*


@Database(entities = [DeviceEntity::class, UserDeviceEntity::class, CheckinRecordEntity::class, StateCovidDataEntity::class, TimeSeriesCovidDataEntity::class, NotificationMsgHistoryEntity::class], version = 6)
abstract class RoomDb : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao?
    abstract fun userDeviceDao(): UserDeviceDao?
    abstract fun checkinRecordDao(): CheckinRecordDao?
    abstract fun stateCovidDataDao(): StateCovidDataDao?
    abstract fun timeSeriesCovidDataDao(): TimeSeriesCovidDataDao?
    abstract fun notificationMsgHistoryDao(): NotificationMsgHistoryDao?


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
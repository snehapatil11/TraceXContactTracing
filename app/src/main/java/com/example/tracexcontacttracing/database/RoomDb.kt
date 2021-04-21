package com.example.tracexcontacttracing.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.tracexcontacttracing.dao.CheckinRecordDao
import com.example.tracexcontacttracing.dao.DeviceDao
import com.example.tracexcontacttracing.dao.StateCovidDataDao
import com.example.tracexcontacttracing.dao.TimeSeriesCovidDataDao
import com.example.tracexcontacttracing.dao.UserDeviceDao
import com.example.tracexcontacttracing.data.CheckinRecordEntity
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.data.StateCovidDataEntity
import com.example.tracexcontacttracing.data.TimeSeriesCovidDataEntity
import com.example.tracexcontacttracing.data.UserDeviceEntity


@Database(entities = [DeviceEntity::class, UserDeviceEntity::class, CheckinRecordEntity::class, StateCovidDataEntity::class, TimeSeriesCovidDataEntity::class], version = 4)
abstract class RoomDb : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao?
    abstract fun userDeviceDao(): UserDeviceDao?
    abstract fun checkinRecordDao(): CheckinRecordDao?
    abstract fun stateCovidDataDao(): StateCovidDataDao?
    abstract fun TimeSeriesCovidDataDao(): TimeSeriesCovidDataDao?


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
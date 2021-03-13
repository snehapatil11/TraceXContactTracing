package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.data.DeviceEntity

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device ORDER BY id desc")
    fun getDeviceData(): List<DeviceEntity>?

    @Insert
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: DeviceEntity): Long

    @Delete
    fun delete(device: DeviceEntity): Int

    @Update
    fun update(device: DeviceEntity)
}
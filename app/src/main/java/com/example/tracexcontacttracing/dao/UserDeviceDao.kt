package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.data.UserDeviceEntity

@Dao
interface UserDeviceDao {
    @Query("SELECT * FROM userDevice")
    fun getUserDeviceData(): List<UserDeviceEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: UserDeviceEntity): Long

    @Delete
    fun delete(device: UserDeviceEntity): Int

    @Update
    fun update(device: UserDeviceEntity)
}
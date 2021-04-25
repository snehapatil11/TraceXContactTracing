package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.Notification.UserDeviceIdDataModel
import com.example.tracexcontacttracing.data.UserDeviceEntity

@Dao
interface UserDeviceDao {
    @Query("SELECT * FROM userDevice")
    fun getUserDeviceData(): List<UserDeviceEntity>?

    @Query("SELECT device_id FROM userDevice")
    fun getUserDeviceId(): List<UserDeviceIdDataModel>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: UserDeviceEntity): Long

    @Delete
    fun delete(device: UserDeviceEntity): Int

    @Update
    fun update(device: UserDeviceEntity)
}
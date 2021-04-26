package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.data.NotificationMsgHistoryEntity

@Dao
interface NotificationMsgHistoryDao {

    @Query("SELECT * FROM notificationHistory")
    fun getAllNotificationMsgHistoryData(): List<NotificationMsgHistoryEntity>?

    @Query("DELETE FROM notificationHistory WHERE ExposureDate <= date('now','-14 day')")
    fun deleteNotificationHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notifictaion: NotificationMsgHistoryEntity): Long

    @Delete
    fun delete(notifictaion: NotificationMsgHistoryEntity): Int

    @Update
    fun update(notifictaion: NotificationMsgHistoryEntity)
}
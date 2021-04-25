package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificationHistory")
data class NotificationMsgHistoryEntity(
    @PrimaryKey @ColumnInfo(name = "ExposureDate") val msgDate: Long
) {
}
package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "device_token") val deviceToken: String,
    @ColumnInfo(name = "device_id") val deviceId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
) {
}
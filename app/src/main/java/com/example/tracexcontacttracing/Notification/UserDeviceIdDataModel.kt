package com.example.tracexcontacttracing.Notification

import androidx.room.ColumnInfo

data class UserDeviceIdDataModel (
    @ColumnInfo(name = "device_id") val device_id: String?
)
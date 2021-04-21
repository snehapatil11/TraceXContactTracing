package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_record")
data class CheckinRecordEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "record_id") val recordId: Int,
    @ColumnInfo(name = "device_token") val fever: Boolean,
    @ColumnInfo(name = "cough") val cough: Boolean,
    @ColumnInfo(name = "taste_loss") val tasteLoss: Boolean,
    @ColumnInfo(name = "sore_throat") val soreThroat: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
) {

    override fun toString(): String {
        return "CheckinRecordEntity(recordId=$recordId, fever=$fever, cough=$cough, tasteLoss=$tasteLoss, soreThroat=$soreThroat, createdAt=$createdAt, modifiedAt=$modifiedAt)"
    }
}
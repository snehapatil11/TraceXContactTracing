package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.data.CheckinRecordEntity

@Dao
interface CheckinRecordDao {
    @Query("SELECT * FROM checkin_record")
    fun getAll(): List<CheckinRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(checkinRecord: CheckinRecordEntity): Long

    @Delete
    fun delete(checkinRecord: CheckinRecordEntity): Int

    @Update
    fun update(checkinRecord: CheckinRecordEntity)
}
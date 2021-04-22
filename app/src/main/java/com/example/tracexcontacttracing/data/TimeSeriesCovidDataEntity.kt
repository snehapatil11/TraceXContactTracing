package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity(tableName = "timeseries_covid_data")
class TimeSeriesCovidDataEntity (
    @PrimaryKey @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "monthGroup") val monthGroup: String,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "month") val month: Int,
    @ColumnInfo(name = "newCases") val newCases: Int,
    @ColumnInfo(name = "newDeaths") val newDeaths: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long

){

}
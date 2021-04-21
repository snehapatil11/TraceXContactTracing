package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeseries_covid_data")
class TimeSeriesCovidDataEntity (
    @PrimaryKey @ColumnInfo(name = "date") val state: String,
    @ColumnInfo(name = "newCases") val newCases: Int,
    @ColumnInfo(name = "newDeaths") val newDeaths: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
){
}
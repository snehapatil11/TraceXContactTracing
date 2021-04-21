package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.data.TimeSeriesCovidDataEntity

@Dao
interface TimeSeriesCovidDataDao {
    @Query("SELECT * FROM timeseries_covid_data")
    fun getAllTimeSeriesData(): List<TimeSeriesCovidDataEntity>?

    @Query("SELECT newCases FROM timeseries_covid_data")
    fun getCasesTimeSeries(): Int?

    @Query("SELECT newDeaths FROM timeseries_covid_data")
    fun getDeathsTimeSeries(): Int?

    @Query("SELECT max(modified_at) FROM timeseries_covid_data")
    fun getLastUpdatedTsForData(): Long

    @Query("DELETE FROM timeseries_covid_data")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: TimeSeriesCovidDataEntity): Long

    @Delete
    fun delete(device: TimeSeriesCovidDataEntity): Int

    @Update
    fun update(device: TimeSeriesCovidDataEntity)
}
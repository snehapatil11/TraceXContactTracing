package com.example.tracexcontacttracing.dao

import androidx.room.*
import com.example.tracexcontacttracing.data.StateCovidDataEntity

@Dao
interface StateCovidDataDao {
    @Query("SELECT * FROM state_covid_data")
    fun getAllStateData(): List<StateCovidDataEntity>?

    @Query("SELECT sum(cases) FROM state_covid_data")
    fun getTotalCases(): Int?

    @Query("SELECT sum(deaths) FROM state_covid_data")
    fun getTotalDeaths(): Int?

    @Query("SELECT sum(newCases) FROM state_covid_data")
    fun getTotalNewCases(): Int?

    @Query("SELECT sum(newDeaths) FROM state_covid_data")
    fun getTotalNewDeaths(): Int?

    @Query("SELECT sum(vaccinationsInitiated) FROM state_covid_data")
    fun getTotalVaccinationInitiated(): Int?

    @Query("SELECT sum(vaccinationsCompleted) FROM state_covid_data")
    fun getTotalVaccinationCompleted(): Int?

    @Query("SELECT max(modified_at) FROM state_covid_data")
    fun getLastUpdatedTsForData(): Long

    @Query("DELETE FROM state_covid_data")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: StateCovidDataEntity): Long

    @Delete
    fun delete(device: StateCovidDataEntity): Int

    @Update
    fun update(device: StateCovidDataEntity)

}
package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "state_covid_data")
data class StateCovidDataEntity(
    @PrimaryKey @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "cases") val cases: Int,
    @ColumnInfo(name = "deaths") val deaths: Int,
    @ColumnInfo(name = "newCases") val newCases: Int,
    @ColumnInfo(name = "newDeaths") val newDeaths: Int,
    @ColumnInfo(name = "vaccinationsInitiated") val vaccinationsInitiated: Int,
    @ColumnInfo(name = "vaccinationsCompleted") val vaccinationsCompleted: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long
) {
}

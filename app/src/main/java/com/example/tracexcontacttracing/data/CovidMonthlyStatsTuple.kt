package com.example.tracexcontacttracing.data

import androidx.room.ColumnInfo

data class CovidMonthlyStatsTuple(
    @ColumnInfo(name = "monthGroup") val monthGroup: String?,
    @ColumnInfo(name = "newCases") val newCases: Float?

) {
    override fun toString(): String {
        return "(${monthGroup},${newCases})"
    }
}
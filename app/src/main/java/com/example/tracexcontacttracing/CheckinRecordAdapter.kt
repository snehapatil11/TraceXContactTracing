package com.example.tracexcontacttracing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.tracexcontacttracing.data.CheckinRecordEntity
import com.google.common.primitives.Booleans
import kotlinx.android.synthetic.main.list_record.view.*
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CheckinRecordAdapter(
    context: Context,
    private val resource: Int,
    private val checkinRecords: List<CheckinRecordEntity>
) : ArrayAdapter<CheckinRecordEntity>(context, resource) {
    private val TAG = "CheckinRecordAdapter"
    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        Log.d(TAG, "getCount() called")
        return checkinRecords.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.d(TAG, "getView() called")
        val view = inflater.inflate(resource, parent, false)

        val sympsNum: TextView = view.symps_num
        val symptoms: TextView = view.symptoms
        val checkinDate: TextView = view.checkin_date

        val currentRecord = checkinRecords[position]

        val fever = currentRecord.fever
        val cough = currentRecord.cough
        val soreThroat = currentRecord.soreThroat
        val tasteLoss = currentRecord.tasteLoss

        val symptomsNum = Booleans.countTrue(
            fever,
            cough,
            soreThroat,
            tasteLoss
        )

        val symps = ArrayList<String>()
        if (fever) symps.add("Fever")
        if (cough) symps.add("Cough")
        if (soreThroat) symps.add("Sore Throat")
        if (tasteLoss) symps.add("Loss of taste or smell")

        val symptomList = symps.joinToString(" | ")

        val formatter = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        val stamp = Timestamp(currentRecord.createdAt)
        val createdDate = Date(stamp.getTime())

        sympsNum.text = "$symptomsNum Symptom(s)"
        symptoms.text = symptomList
        checkinDate.text = "Check-In Time: ${formatter.format(createdDate)}"

        return view
    }
}
package com.example.tracexcontacttracing.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.database.RoomDb
import kotlinx.android.synthetic.main.fragment_checkin_details.view.*


class CheckinDetailFragment : Fragment() {
    private val TAG = "CheckinDetailFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_checkin_details, container, false)


        val checkinRecordDao = RoomDb.getAppDatabase(this.context!!)?.checkinRecordDao()
        val checkinRecords = checkinRecordDao?.getAllCheckinRecordEntities()

        Log.d(TAG, "Historical records: $checkinRecords")

        // TODO: Use listview with adapter
        view.list.setText(checkinRecords.toString())

        // Inflate the layout for this fragment
        return view
    }


}
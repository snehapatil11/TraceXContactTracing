package com.example.tracexcontacttracing.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.CheckinRecordEntity
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
        val checkinRecords = checkinRecordDao!!.getAll()

        Log.d(TAG, "Historical records: $checkinRecords")

        // TODO: Show next steps if showing symptoms

        val arrayAdapter = ArrayAdapter<CheckinRecordEntity>(activity as Context, R.layout.list_item, checkinRecords)
        // TODO: Use custom adapter to display
        view.recordListView.adapter = arrayAdapter

        // Inflate the layout for this fragment
        return view
    }


}
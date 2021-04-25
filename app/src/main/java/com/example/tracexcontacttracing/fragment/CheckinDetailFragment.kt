package com.example.tracexcontacttracing.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.CheckinRecordAdapter
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

        view.read_more.setOnClickListener {
            goToUrl("https://www.cdc.gov/coronavirus/2019-ncov/if-you-are-sick/steps-when-sick.html/")
        }

        val checkinRecordDao = RoomDb.getAppDatabase(this.context!!)?.checkinRecordDao()
        val checkinRecords = checkinRecordDao!!.getAll()

        Log.d(TAG, "Historical records: $checkinRecords")

        val checkinRecordAdapter = CheckinRecordAdapter(
            activity as Context,
            R.layout.list_record,
            checkinRecords
        )
        view.recordListView.adapter = checkinRecordAdapter

        // Inflate the layout for this fragment
        return view
    }

    private fun goToUrl(url: String) {
        val uriUrl: Uri = Uri.parse(url)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }
}
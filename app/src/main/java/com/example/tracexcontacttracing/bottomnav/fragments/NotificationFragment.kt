package com.example.tracexcontacttracing.bottomnav.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tracexcontacttracing.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracexcontacttracing.Notification.NotificationRVAdapter
import com.example.tracexcontacttracing.database.RoomDb
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationFragment : Fragment() {

    var rv_notification_list: RecyclerView?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root: View = inflater!!.inflate(R.layout.fragment_notification, container, false)

        rv_notification_list = root.findViewById(R.id.rv_notification_list)
        rv_notification_list!!.layoutManager = LinearLayoutManager(context)

        val notificationDao = RoomDb.getAppDatabase(this.context!!)?.notificationMsgHistoryDao()
        val msgDates = notificationDao?.getAllNotificationMsgHistoryData()
        val arNotification: ArrayList<String> = ArrayList()
        if (msgDates != null) {
            for (ele in msgDates){
                val resultdate = Date(ele.msgDate)
//                Log.i("MessageList", "${ele.msgDate}, $resultdate")
                arNotification.add("This message is to inform you that you have been exposed to a person who tested positive for Coronavirus 19 (COVID-19) on" +
                        " $resultdate " + "This person is home and we suggest you to take preventive measures as per DC Health guidelines.")
            }
        }

        val adapter: NotificationRVAdapter =
            NotificationRVAdapter(
                arNotification,
                context!!
            )
        rv_notification_list!!.adapter = adapter

        return root
    }

    companion object {
        fun newInstance(): NotificationFragment {
            return NotificationFragment()
        }

    }

}


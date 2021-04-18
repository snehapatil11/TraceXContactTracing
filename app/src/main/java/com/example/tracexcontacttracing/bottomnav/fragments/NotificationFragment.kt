package com.example.tracexcontacttracing.bottomnav.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tracexcontacttracing.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracexcontacttracing.Notification.NotificationRVAdapter


class NotificationFragment : Fragment() {

    var rv_notification_list: RecyclerView?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    val CHANNEL_ID = "TraceX_Channel_ID"
    val notification_id = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root: View = inflater!!.inflate(R.layout.fragment_notification, container, false)
//        val btn: Button = view.findViewById(R.id.show_notification)
//        btn.setOnClickListener(this)

        rv_notification_list = root.findViewById(R.id.rv_notification_list)
        rv_notification_list!!.layoutManager = LinearLayoutManager(context)
        val arNotification = arrayListOf("This message is to inform you that you have been exposed to a person who tested positive for Coronavirus 19 (COVID-19) on [INSERT DATE]. " +
                "This person is home and we suggest you to take preventive measures as per DC Health guidelines.",
            "Message 2", "Message 3", "Message 4", "Message 5", "Message 6", "Message 7",
            "This message is to inform you that you have been exposed to a person who tested positive for Coronavirus 19 (COVID-19) on [INSERT DATE]. " +
                    "This person is home and we suggest you to take preventive measures as per DC Health guidelines.",
            "Message 8", "Message 9", "Message 10", "This message is to inform you that you have been exposed to a person who tested positive for Coronavirus 19 (COVID-19) on [INSERT DATE]." +
                    "This person is home and we suggest you to take preventive measures as per DC Health guidelines.")
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



//    override fun onClick(v: View?) {
//        show_notification.setOnClickListener{
////            val intent = Intent(this, NotificationFragment::class.java).apply {
////                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////            }
////            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
//
////            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
////                .setSmallIcon(R.drawable.ic_notification)
////                .setContentTitle("This is the title")
////                .setContentText("This is the notification")
////                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//////                .setContentIntent(pendingIntent)
////                .setAutoCancel(true)
//
////            with(NotificationManagerCompat.from(this)) {
////                notify(0, builder.build())
////            }
//        }
//
////        createNotificationChannel()
//        }

//    private fun createNotificationChannel() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.channel_name)
//            val descriptionText = getString(R.string.channel_description)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }

}


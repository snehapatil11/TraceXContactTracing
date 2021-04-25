package com.example.tracexcontacttracing.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.util.Log
import androidx.work.ListenableWorker
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tracexcontacttracing.MainActivity
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.NotificationMsgHistoryEntity
import com.example.tracexcontacttracing.database.RoomDb
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList


class NotificationWorker(context: Context, params: WorkerParameters):Worker(context,params) {

    private lateinit var database: FirebaseFirestore
    val notificationdao = RoomDb.getAppDatabase(this.applicationContext!!)?.notificationMsgHistoryDao()

    companion object {
        const val CHANNEL_ID = "TraceX_Channel_ID"
        const val NOTIFICATION_ID = 1
    }


    override fun doWork(): ListenableWorker.Result {

        deletePastRecordsRoom()
        findMatchingAdID()
//        showNotification()
        return Result.success()

    }

    private fun deletePastRecordsRoom(){
        notificationdao?.deleteNotificationHistory()
    }

    private fun showNotification(exposedDates: ArrayList<Long>) {

        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0,intent, 0)

        exposedDates.forEach {

            val msg = generateMsg(it)

            val notification = NotificationCompat.Builder(applicationContext,CHANNEL_ID)
                .setContentTitle("TraceX Exposure Notification")
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(msg))

            with(NotificationManagerCompat.from(applicationContext)){
                notify(NOTIFICATION_ID+1, notification.build())
            }

        }

    }

    private fun createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = "TraceX"
            val channelDescription = "Channel Description: TraceX"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateMsg(exposedDates: Long?): String {

        val resultdate = exposedDates?.let { Date(it) }
        val msg = "This message is to inform you that you have been exposed to a person who tested positive for Coronavirus 19 (COVID-19) on " + resultdate + ". " +
                "This person is home and we suggest you to take preventive measures as per DC Health guidelines."
        return msg
    }

    private fun findMatchingAdID() {

        var exposedDates: ArrayList<Long> = ArrayList()

        val userDevicedao = RoomDb.getAppDatabase(this.applicationContext!!)?.userDeviceDao()
        var arUserDevice = userDevicedao?.getUserDeviceId()

        var arrayUserDeviceId: ArrayList<String> = ArrayList()
        arUserDevice?.forEach {
            arrayUserDeviceId.add(it.device_id!!)
        }

        database = FirebaseFirestore.getInstance()

        val query = database.collection("exposed_devices")
            .whereIn("deviceId", arrayUserDeviceId)
//            .whereIn("deviceId", listOf("hd7r","d0db", "u8wa"))

        query.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (snap in queryDocumentSnapshots) {
//                Log.i("FirestoreData", "${snap.id} => ${snap.data.get("createdAt")}")
                exposedDates.add(snap.data.get("createdAt") as Long)
            }
            Log.i("Exposed Date List", "$exposedDates")
            saveExposedDatetoRoomDb(exposedDates)
        }
    }

    private fun saveExposedDatetoRoomDb(exposedDates: ArrayList<Long>) {
        exposedDates?.forEach {
            val notification = NotificationMsgHistoryEntity(it)
            val msgDate = notificationdao?.insert(notification)
            Log.i("MessageDate", "saved $notification as $msgDate")
        }
        showNotification(exposedDates)
    }

}
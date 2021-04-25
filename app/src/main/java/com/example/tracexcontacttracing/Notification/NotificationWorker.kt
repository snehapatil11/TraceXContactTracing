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
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.data.UserDeviceEntity
import com.example.tracexcontacttracing.database.RoomDb
import com.google.firebase.firestore.FirebaseFirestore


class NotificationWorker(context: Context, params: WorkerParameters):Worker(context,params) {

    private lateinit var database: FirebaseFirestore

    companion object {
        const val CHANNEL_ID = "TraceX_Channel_ID"
        const val NOTIFICATION_ID = 1
    }


    override fun doWork(): ListenableWorker.Result {

        findMatchingAdID()
        showNotification()
        return Result.success()

    }

    private fun showNotification(){
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0,intent, 0)

        val notification = NotificationCompat.Builder(applicationContext,CHANNEL_ID)
            .setContentTitle("TraceX Exposure")
            .setContentText("You have been exposed!!!")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        createNotificationChannel()

        with(NotificationManagerCompat.from(applicationContext)){
            notify(NOTIFICATION_ID, notification.build())
        }

    }

    private fun createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = "Channel Name: TraceX"
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

    private fun findMatchingAdID(){

        val userDevicedao = RoomDb.getAppDatabase(this.applicationContext!!)?.userDeviceDao()
        var arUserDevice = userDevicedao?.getUserDeviceId()

        var arrayUserDeviceId: ArrayList<String> = ArrayList()
        arUserDevice?.forEach {
            arrayUserDeviceId.add(it.device_id!!)
        }

        Log.i("arUserDevice -->>", "$arUserDevice")
        Log.i("arrayUserDeviceId -->>", "$arrayUserDeviceId")

        database = FirebaseFirestore.getInstance()

        val query = database.collection("exposed_devices")
            .whereIn("deviceId", arrayUserDeviceId)
//            .whereIn("deviceId", listOf("hd7r","d0db", "u8wa"))

        query.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (snap in queryDocumentSnapshots) {
                Log.i("FirestoreData", "${snap.id} => ${snap.data}")
            }
        }

    }


}
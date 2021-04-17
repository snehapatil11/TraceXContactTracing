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


class NotificationWorker(context: Context, params: WorkerParameters):Worker(context,params) {

    companion object {
        const val CHANNEL_ID = "TraceX_Channel_ID"
        const val NOTIFICATION_ID = 1
    }


    override fun doWork(): ListenableWorker.Result {

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


}
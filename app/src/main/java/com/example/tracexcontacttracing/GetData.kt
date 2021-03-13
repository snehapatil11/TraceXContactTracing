package com.example.tracexcontacttracing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.tracexcontacttracing.database.RoomDb
import java.lang.StringBuilder

class GetData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_data)

        // Read data
        val deviceDao = RoomDb.getAppDatabase(this)?.deviceDao()
        val devices = deviceDao?.getDeviceData()

        println("Start getting data")
        println(devices)
        val sb = StringBuilder()
        devices?.forEach {
            sb.append(it.toString())
        }

        println(sb.toString())

        val result: TextView = findViewById(R.id.textViewResult)
        result.text = sb.toString()
    }
}
package com.example.tracexcontacttracing


import android.app.Application
import com.example.tracexcontacttracing.blemodule.DeviceManager
import com.example.tracexcontacttracing.Provider.BluetoothManagerProvider

class TraceXApp: Application() {

    init {
        BluetoothManagerProvider.inject { DeviceManager(applicationContext) }
    }

    override fun onCreate() {
        super.onCreate()
    }
}

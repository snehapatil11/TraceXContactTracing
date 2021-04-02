package com.example.tracexcontacttracing.blemodule


import android.Manifest
import android.app.*
import android.app.NotificationManager.IMPORTANCE_LOW
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.tracexcontacttracing.MainActivity
import com.example.tracexcontacttracing.Provider.BluetoothManagerProvider
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.Enums
import java.util.*
import kotlin.experimental.and
import kotlin.math.pow
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.database.RoomDb
import java.lang.Math.pow

class BLEService: Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    //static
    companion object {
        @JvmField
        var isAppInForeground: Boolean = false
        const val BACKGROUND_CHANNEL_ID = "SILENT_CHANNEL_LOCATION"
    }
    private val deviceManager by BluetoothManagerProvider()
    private val peripherals = mutableMapOf<BluetoothDevice, PeripheralData>()
    private var bluetoothState: Int = -1
    //private val deviceManager: DeviceManager? = null

    //when the device was first seen
    private val firstSeenTimeMap = mutableMapOf<String, Long>()

    //when the device was last seen
    private val lastSeenTimeMap = mutableMapOf<String, Long>()

    private val distanceMap = mutableMapOf<String, List<Double>>()
    private val MIN_EXPOSURE_TIME = 120000 //in milliseconds
    private val MIN_EXPOSURE_DISTANCE = 6 //in feet
    //difference between current time and last seen time for the device to be not in the periphery
    private val DISAPPEAR_TIME = 20000 //in milliseconds

    // if the difference between end time of the data and current time is less than this time then delete that record from the database
    private val MAX_TIME_DIFF: Long = 120000



    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                bluetoothState =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                when (bluetoothState) {
                    //BluetoothAdapter.STATE_OFF -> stopBleService()
                    BluetoothAdapter.STATE_ON -> startBleService()
                }
            }
        }
    }
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            //background work here
            try {
                startBleService()

            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }
        }
    }

    override fun onCreate() {

        /*if (deviceManager != null) {
            bluetoothState =
                if (deviceManager.checkBluetooth() == Enums.ENABLED) BluetoothAdapter.STATE_ON
                else BluetoothAdapter.STATE_OFF
        }
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))*/

        HandlerThread("ServiceStartArguments", Process.BLUETOOTH_UID).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Log.i(TAG, "Start Command")

        startForeground()

        //startBleService()

        /*isAppInForeground = true
        createNotificationChannel()
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        var notification: Notification? = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             notification = NotificationCompat.Builder(this, "TraceXContactTracing")
                .setContentTitle("Contact Tracing")
                .setContentText("TraceX Contact Tracing application")
                //.setSmallIcon(R.drawable.)
                .setContentIntent(pendingIntent)
                //.setTicker(getText(R.string.ticker_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        }
        startForeground(1, notification)*/

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel =
                NotificationChannel(
                    BACKGROUND_CHANNEL_ID,
                    getString(R.string.background_channel_name),
                    IMPORTANCE_LOW
                )
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(1, getNotification())
    }
    private fun getNotification(): Notification? {
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, BACKGROUND_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentText(getBluetoothState())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE)
        }
        return builder.build()
    }

    /*private fun hasPermissions(): Boolean {
        val notGrantedPermissions =
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION).filter { it.isNotGranted() }

        return notGrantedPermissions.isEmpty()
    }*/
    override fun onDestroy() {
        //unregisterReceiver(bluetoothReceiver)

        //stopBleService()
        super.onDestroy()
        stopBleService()
        Toast.makeText(this.baseContext, "Contact Tracing Service Ended", Toast.LENGTH_SHORT).show()
        isAppInForeground = false
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "GBContactTracing",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
    private fun startScanning() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        //if (hasPermissions() && gpsEnabled) {
        //if (gpsEnabled) {
            deviceManager?.startSearchDevices(::onBleDeviceFound)
        //}
    }

    private fun startAdvertising() {
        try {
            deviceManager?.startAdvertising()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onBleDeviceFound(result: ScanResult) {
        /*peripherals[result.device]?.let { peripheralData ->
            if (System.currentTimeMillis() - peripheralData.date.time > 100000) {
                /*Log.v(
                    SCAN_TAG,
                    "Not connecting to ${result.device.address} yet"
                )*/
                println(result.device.address)
                return
            }
        }*/

        val deviceName = result.device.name
        val deviceId = result.device.address

        val manuData: ByteArray =
            result.scanRecord?.getManufacturerSpecificData(1023) ?: "N.A".toByteArray()
        val advertisingUUID = String(manuData, Charsets.UTF_8)

        val distance = (10.0.pow((-69 - (result.rssi)) / (10.0 * 4)) * 3.28084)/100

        if (!firstSeenTimeMap.containsKey(advertisingUUID)) {
            firstSeenTimeMap[advertisingUUID] = System.currentTimeMillis()
            distanceMap[advertisingUUID] = mutableListOf(distance)
        } else {
            lastSeenTimeMap[advertisingUUID] = System.currentTimeMillis()
            (distanceMap[advertisingUUID] as MutableList<Double>?)?.add(distance)
        }

        if (firstSeenTimeMap.containsKey(advertisingUUID) && lastSeenTimeMap.containsKey(advertisingUUID)) {
            //exposure = listSeenTime - firstSeenTime
            val exposureTime = firstSeenTimeMap[advertisingUUID]?.let { lastSeenTimeMap[advertisingUUID]?.minus(it) }
            //average distance from the list of distances
            val avgDistance = distanceMap[advertisingUUID]?.average()

            if (exposureTime != null && avgDistance != null) {
                if (exposureTime > MIN_EXPOSURE_TIME && avgDistance < MIN_EXPOSURE_DISTANCE) {
                    firstSeenTimeMap[advertisingUUID]?.let {
                        lastSeenTimeMap[advertisingUUID]?.let { it1 ->

                            val device = DeviceEntity(deviceId, advertisingUUID, System.currentTimeMillis(), System.currentTimeMillis())

                            val deviceDao = RoomDb.getAppDatabase(this.baseContext!!)?.deviceDao()
                            val id = deviceDao?.insert(device)
                            println("saved device $device with id=$id")
                        }
                    }
                }
            }
        }


        peripherals[result.device] = PeripheralData(result.rssi, Date())
        if (deviceManager?.connectDevice(result)!!) {
            /*Log.d(
                SCAN_TAG,
                "Connecting to ${result.device.address}, RSSI ${result.rssi}"
            )*/
        }
    }

    private fun onBleDeviceConnect(device: BluetoothDevice, result: Boolean) {
        if (!result) {
            peripherals.remove(device)
        }
    }

    fun startBleService() {
        startAdvertising()
        startScanning()
    }

    fun stopBleService() {
        deviceManager?.stopSearchDevices()
        deviceManager?.stopServer()
        deviceManager?.stopAdvertising()
    }


    private fun getBluetoothState(): String {
        return getString(
            when (bluetoothState) {
                BluetoothAdapter.STATE_OFF -> R.string.bluetooth_off
                BluetoothAdapter.STATE_TURNING_OFF -> R.string.turning_bluetooth_off
                BluetoothAdapter.STATE_ON -> R.string.bluetooth_on
                BluetoothAdapter.STATE_TURNING_ON -> R.string.turning_bluetooth_on
                else -> R.string.bluetooth_unknown_state
            }
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}

data class PeripheralData(val rssi: Int, val date: Date)
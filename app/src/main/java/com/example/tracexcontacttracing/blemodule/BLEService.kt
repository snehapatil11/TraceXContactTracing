package com.example.tracexcontacttracing.blemodule


import android.Manifest
import android.app.*
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
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
                    BluetoothAdapter.STATE_OFF -> stopBleService()
                    BluetoothAdapter.STATE_ON -> startBleService()
                }
            }
        }
    }
    override fun onCreate() {

        if (deviceManager != null) {
            bluetoothState =
                if (deviceManager.checkBluetooth() == Enums.ENABLED) BluetoothAdapter.STATE_ON
                else BluetoothAdapter.STATE_OFF
        }
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Log.i(TAG, "Start Command")

        //startForeground()

        startBleService()

        return START_STICKY
    }

    private fun startForeground() {

    }

    /*private fun hasPermissions(): Boolean {
        val notGrantedPermissions =
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION).filter { it.isNotGranted() }

        return notGrantedPermissions.isEmpty()
    }*/
    override fun onDestroy() {
        unregisterReceiver(bluetoothReceiver)

        stopBleService()
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

        val distance = (10.0.pow((-69 - (result.rssi)) / (10.0 * 4)) * 3.28084)/100

        if (!firstSeenTimeMap.containsKey(deviceId)) {
            firstSeenTimeMap[deviceId] = System.currentTimeMillis()
            distanceMap[deviceId] = mutableListOf(distance)
        } else {
            lastSeenTimeMap[deviceId] = System.currentTimeMillis()
            (distanceMap[deviceId] as MutableList<Double>?)?.add(distance)
        }

        if (firstSeenTimeMap.containsKey(deviceId) && lastSeenTimeMap.containsKey(deviceId)) {
            //exposure = listSeenTime - firstSeenTime
            val exposureTime = firstSeenTimeMap[deviceId]?.let { lastSeenTimeMap[deviceId]?.minus(it) }
            //average distance from the list of distances
            val avgDistance = distanceMap[deviceId]?.average()

            if (exposureTime != null && avgDistance != null) {
                if (exposureTime > MIN_EXPOSURE_TIME && avgDistance < MIN_EXPOSURE_DISTANCE) {
                    firstSeenTimeMap[deviceId]?.let {
                        lastSeenTimeMap[deviceId]?.let { it1 ->

                            val device = DeviceEntity("deviceName", deviceId, System.currentTimeMillis(), System.currentTimeMillis())

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
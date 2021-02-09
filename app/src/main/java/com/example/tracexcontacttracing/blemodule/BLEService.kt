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

class BLEService: Service() {

    private val deviceManager by BluetoothManagerProvider()
    private val peripherals = mutableMapOf<BluetoothDevice, PeripheralData>()
    private var bluetoothState: Int = -1
    //private val deviceManager: DeviceManager? = null

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
        peripherals[result.device]?.let { peripheralData ->
            if (System.currentTimeMillis() - peripheralData.date.time < 5000) {
                /*Log.v(
                    SCAN_TAG,
                    "Not connecting to ${result.device.address} yet"
                )*/
                //print result.device.address
                return
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
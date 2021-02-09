package com.example.tracexcontacttracing


import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.tracexcontacttracing.Provider.BluetoothManagerProvider
import com.example.tracexcontacttracing.blemodule.*
import com.example.tracexcontacttracing.data.Enums


class MainActivity : AppCompatActivity(), OnDeviceScanListener, View.OnClickListener {

    companion object {
        const val REQUEST_NONE = 0
        const val REQUEST_LOCATION = 1
        const val REQUEST_CHECK_TRACKING_SETTINGS = 2
        private const val REQUEST_BLUETOOTH = 3
    }

    private val deviceManager by BluetoothManagerProvider()
    private var bluetoothAlert: androidx.appcompat.app.AlertDialog.Builder? = null

    private lateinit var mBtnReadConnectionChar: Button
    private lateinit var mBtnReadBatteryLevel: Button
    private lateinit var mBtnReadEmergency: Button
    private lateinit var mBtnWriteEmergency: Button
    private lateinit var mBtnWriteConnection: Button
    private lateinit var mBtnWriteBatteryLevel: Button
    private lateinit var mTvResult: TextView

    private var mDeviceAddress: String = ""

    override fun onScanCompleted(deviceDataList: DeviceData) {

        //Initiate a dialog Fragment from here and ask the user to select his device
        // If the application already know the Mac address, we can simply call connect device

        mDeviceAddress = deviceDataList.mDeviceAddress
        //ConnectionManagerBLE.connect(deviceDataList.mDeviceAddress)

    }

    private val REQUEST_LOCATION_PERMISSION = 2018
    private val TAG = "MainActivity"
    private val REQUEST_ENABLE_BT = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBtnReadConnectionChar = findViewById<Button>(R.id.btn_read_connection)
        mBtnReadEmergency = findViewById(R.id.btn_read_emergency)
        mBtnReadBatteryLevel = findViewById(R.id.btn_read_battery)

        findViewById<View>(R.id.btn_scan).setOnClickListener(this)
        mBtnReadConnectionChar.setOnClickListener(this)
        //mBtnWriteEmergency.setOnClickListener(this)

        mBtnReadEmergency.setOnClickListener(this)
        mBtnReadBatteryLevel.setOnClickListener(this)

        //mBtnWriteBatteryLevel.setOnClickListener(this)
        //mBtnWriteConnection.setOnClickListener(this)

        checkLocationPermission()
    }

    /**
     * Check the Location Permission before calling the BLE API's
     */
    private fun checkLocationPermission() {
        if (isAboveMarshmallow()) {
            when {
                isLocationPermissionEnabled() -> initBLEModule()
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) -> displayRationale()
                else -> requestLocationPermission()
            }
        } else {
            initBLEModule()
        }
    }

    /**
     * Request Location API
     * If the request go to Android system and the System will throw a dialog message
     * user can accept or decline the permission from there
     */
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
    }

    /**
     * If the user decline the Permission request and tick the never ask again message
     * Then the application can't proceed further steps
     * In such situation- App need to prompt the user to do the change form Settings Manually
     */
    private fun displayRationale() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_disabled))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> requestLocationPermission() }
            .setNegativeButton(getString(R.string.cancel)
            ) { _, _ -> }
            .show()
    }

    /**
     * If the user either accept or reject the Permission- The requested App will get a callback
     * Form the call back we can filter the user response with the help of request key
     * If the user accept the same- We can proceed further steps
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting location permission.")
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initBLEModule()
                } else {
                    Toast.makeText(this, R.string.location_permission_not_granted,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Check with the system- If the permission already enabled or not
     */
    private fun isLocationPermissionEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * The location permission is incorporated in Marshmallow and Above
     */
    private fun isAboveMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }


    /**
     *After receive the Location Permission, the Application need to initialize the
     * BLE Module and BLE Service
     */
    /*private fun initBLEModule() {
        // BLE initialization
        if (!DeviceManagerBLE.init(this)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show()
            return
        }
        if (!DeviceManagerBLE.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        startService(Intent(this, BLEService::class.java))
        //ConnectionManagerBLE.initBLEService(this@MainActivity)
    }*/

    private fun checkBluetooth(): Enums = deviceManager.checkBluetooth()

    private fun initBLEModule() {
        when (checkBluetooth()) {
            Enums.ENABLED -> startService(Intent(this, BLEService::class.java))
            Enums.DISABLED -> showBluetoothDisabledError()
            Enums.NOT_FOUND -> showBluetoothNotFoundError()
        }
    }

    private fun showBluetoothDisabledError() {
        if (bluetoothAlert == null)
            bluetoothAlert = androidx.appcompat.app.AlertDialog.Builder(this).apply {
                setTitle(R.string.bluetooth_turn_off)
                setMessage(R.string.bluetooth_turn_off_description)
                setPositiveButton(R.string.enable) { _, _ ->
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH)
                    bluetoothAlert = null
                }
                setOnCancelListener { bluetoothAlert = null }
                show()
            }
    }

    private fun showBluetoothNotFoundError() {
        androidx.appcompat.app.AlertDialog.Builder(this).apply {
            setTitle(R.string.bluetooth_do_not_support)
            setMessage(R.string.bluetooth_do_not_support_description)
            setCancelable(false)
            setNegativeButton(R.string.done) { _, _ -> }
            show()
        }
    }

    /*private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when {
                BConstants.ACTION_GATT_CONNECTED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_CONNECTED ")
                    ConnectionManagerBLE.findBLEGattService(this@MainActivity)
                }
                BConstants.ACTION_GATT_DISCONNECTED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED ")
                }
                BConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED ")
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    ConnectionManagerBLE.findBLEGattService(this@MainActivity)
                }
                BConstants.ACTION_DATA_AVAILABLE.equals(action) -> {
                    val data = intent.getStringExtra(BConstants.EXTRA_DATA)
                    val uuId = intent.getStringExtra(BConstants.EXTRA_UUID)
                    Log.i(TAG, "ACTION_DATA_AVAILABLE $data")

                }
                BConstants.ACTION_DATA_WRITTEN.equals(action) -> {
                    val data = intent.getStringExtra(BConstants.EXTRA_DATA)
                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
                }
            }
        }
    }*/

    /**
     * Intent filter for Handling BLEService broadcast.
     */
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BConstants.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BConstants.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BConstants.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BConstants.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BConstants.ACTION_DATA_WRITTEN)

        return intentFilter
    }



    override fun onClick(v: View?) {
  /*      when (v?.id) {
            R.id.btn_scan ->
                scanDevice(false)
            R.id.btn_read_connection ->
                readMissedConnection()
            R.id.btn_read_battery ->
                readBatteryLevel()

            R.id.btn_read_emergency ->
                readEmergencyGatt()


        }*/
    }
/*

    private fun readMissedConnection() {
        ConnectionManagerBLE.readMissedConnection(getString(R.string.char_uuid_missed_calls))
    }

    private fun readBatteryLevel() {
        ConnectionManagerBLE.readBatteryLevel(getString(R.string.char_uuid_emergency))
    }

    private fun readEmergencyGatt() {
        ConnectionManagerBLE.readEmergencyGatt(getString(R.string.char_uuid_emergency))
    }

    *//**
     * Scan the BLE device if the device address is null
     * else the app will try to connect with device with existing device address.
     *//*
    private fun scanDevice(isContinuesScan: Boolean) {
        if (!mDeviceAddress.isNullOrEmpty()) {
            connectDevice()
        } else {
            DeviceManagerBLE.scanBLEDevice(isContinuesScan)
        }
    }

    *//**
     * Connect the application with BLE device with selected device address.
     *//*
    private fun connectDevice() {
        Handler().postDelayed({
            ConnectionManagerBLE.initBLEService(this@MainActivity)
            if (ConnectionManagerBLE.connect(mDeviceAddress)) {
                Toast.makeText(this@MainActivity, "DEVICE CONNECTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "DEVICE CONNECTION FAILED", Toast.LENGTH_SHORT).show()
            }
        }, 100)
    }*/

}
package com.example.tracexcontacttracing


import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Toast

import com.example.tracexcontacttracing.Provider.BluetoothManagerProvider
import com.example.tracexcontacttracing.blemodule.*
import com.example.tracexcontacttracing.data.Enums

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.tracexcontacttracing.Notification.NotificationWorker
import com.example.tracexcontacttracing.blemodule.*

import com.example.tracexcontacttracing.bottomnav.fragments.CheckinFragment
import com.example.tracexcontacttracing.bottomnav.fragments.ContacttracingFragment
import com.example.tracexcontacttracing.bottomnav.fragments.NotificationFragment
import com.example.tracexcontacttracing.bottomnav.fragments.UpdatesFragment
import kotlinx.android.synthetic.main.activity_main.*

import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity(), OnDeviceScanListener, View.OnClickListener {

    companion object {
        private const val REQUEST_BLUETOOTH = 3
    }

    private val deviceManager by BluetoothManagerProvider()
    private var bluetoothAlert: androidx.appcompat.app.AlertDialog.Builder? = null


    private var mDeviceAddress: String = ""

    private val REQUEST_LOCATION_PERMISSION = 2018

    override fun onScanCompleted(deviceDataList: DeviceData) {

        //Initiate a dialog Fragment from here and ask the user to select his device
        // If the application already know the Mac address, we can simply call connect device

        mDeviceAddress = deviceDataList.mDeviceAddress
        //ConnectionManagerBLE.connect(deviceDataList.mDeviceAddress)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val updatesFragment = UpdatesFragment()
        val contacttracingFragment = ContacttracingFragment()
        val notificationFragment = NotificationFragment()
        val checkinFragment = CheckinFragment()

        makeCurrentFragment(updatesFragment)

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId){
                R.id.ic_updatetab -> makeCurrentFragment(updatesFragment)
                R.id.ic_contacttracing -> makeCurrentFragment(contacttracingFragment)
                R.id.ic_notification -> makeCurrentFragment(notificationFragment)
                R.id.ic_checkin -> makeCurrentFragment(checkinFragment)
            }
            true
        }

        checkLocationPermission()

        callOneTimeWorkRequest()
    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.f1_wrapper, fragment)
            commit()
        }

    /**
     * Check the Location
     */
    private fun checkLocationPermission() {
        if (isAboveMarshmallow()) {
            when {
                //isLocationPermissionEnabled() -> initBLEModule()
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) -> displayRationale()
                else -> requestLocationPermission()
            }
        }
        /*else {
            initBLEModule()
        }*/
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
    }


    private fun displayRationale() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_disabled))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> requestLocationPermission() }
            .setNegativeButton(getString(R.string.cancel)
            ) { _, _ -> }
            .show()
    }

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

    private fun isLocationPermissionEnabled(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

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
            //Enums.ENABLED -> startService(Intent(this, BLEService::class.java))
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

    }


    /**
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


    private fun callOneTimeWorkRequest(){

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 03:27:00 PM
        dueDate.set(Calendar.HOUR_OF_DAY, 15)
        dueDate.set(Calendar.MINUTE, 55)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        val dailyWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()
        workManager
            // Work status: Blocked > Enqueued > Running > Succeeded
            .enqueue(dailyWorkRequest)

    }


}
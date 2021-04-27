package com.example.tracexcontacttracing.blemodule

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import com.example.tracexcontacttracing.data.Enums
import com.example.tracexcontacttracing.database.RoomDb
import com.example.tracexcontacttracing.data.UserDeviceEntity
import java.util.*

class DeviceManager(private val context: Context) {

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9f")
        val MAIN_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private var scanCallback: ScanCallback? = null
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var scanActive = false
    private var advertisingActive = false

    private var deviceStatusListener: DeviceStatusListener? = null


    fun checkBluetooth(): Enums {
        val hasSupportLe = context.packageManager
            ?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            ?: false

        return if (bluetoothAdapter == null || !hasSupportLe) {
            Enums.NOT_FOUND
        } else if (!bluetoothAdapter?.isEnabled!!) {
            Enums.DISABLED
        } else {
            Enums.ENABLED
        }
    }

    fun startSearchDevices(devicesCallback: (ScanResult) -> Unit) {
        if (scanActive) {
            return
        }

        val deviceFilter = ScanFilter.Builder()
            .apply { setServiceUuid(ParcelUuid(SERVICE_UUID)) }
            .build()

        val bluetoothSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val scanRecord = result.scanRecord
                if (scanRecord != null) {
                    devicesCallback(result)
                }
            }
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(
            mutableListOf(deviceFilter),
            bluetoothSettings,
            scanCallback
        )
        scanActive = true
        //insertLogs(SCAN_TAG, "Start scan")
    }

    /**
     * Stop Bluetooth LE scanning process
     */
    fun stopSearchDevices() {
        scanActive = false
        bluetoothAdapter?.isDiscovering?.let {
            bluetoothAdapter?.cancelDiscovery()
        }
        scanCallback?.let { bluetoothAdapter?.bluetoothLeScanner?.stopScan(it) }
        scanCallback = null
        //insertLogs(SCAN_TAG, "Stop scan")
    }

    /**
     * Arrange connection to the selected device, and read characteristics of the identified device type
     */
    fun connectDevice(scanResult: ScanResult): Boolean {
        val device = scanResult.device

        device.connectGatt(
            context,
            false,
            object : BluetoothGattCallback() {
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)

                }

                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {

                            gatt.discoverServices()
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {

                        }
                    }
                    when (status) {
                        BluetoothGatt.GATT_FAILURE -> {
                        }
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

                    var hasServiceAndCharacteristic = false
                    val service = gatt.getService(SERVICE_UUID)
                    if (service != null) {
                        val characteristic =
                            service.getCharacteristic(MAIN_CHARACTERISTIC_UUID)
                        characteristic?.let {
                            //characteristic.value = CryptoUtil.getCurrentRpi()

                            gatt.writeCharacteristic(it)


                            hasServiceAndCharacteristic = true
                        }
                    }
                    if (!hasServiceAndCharacteristic) {
                        deviceStatusListener?.onServiceNotFound(device)
                        gatt.close()
                    }
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        handleCharacteristicRead(scanResult, characteristic)

                        gatt.close()
                    }
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt.readCharacteristic(characteristic)
                    }
                }
            })

        return true
    }
    private fun handleCharacteristicRead(
        scanResult: ScanResult,
        characteristic: BluetoothGattCharacteristic
    ) {
        val data = characteristic.value

        deviceStatusListener?.onDataReceived(scanResult.device, characteristic.value)

    }

    interface DeviceStatusListener {
        fun onDataReceived(device: BluetoothDevice, bytes: ByteArray)
        fun onServiceNotFound(device: BluetoothDevice)
    }



    fun startAdvertising(): Boolean {
        if (advertisingActive)
            return true

        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            //insertLogs(ADV_TAG, "Multiple advertisement is not supported")
        }

        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false
        }

        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
            bluetoothManager.adapter.bluetoothLeAdvertiser
        if (bluetoothLeAdvertiser == null) {
            return false
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
            .build()

        val randomUUID = UUID.randomUUID().toString()
        val finalString = randomUUID.substring(randomUUID.length - 6, randomUUID.length)
        val serviceDataByteArray = finalString.toByteArray()

        //store advertising UUID in room db
        val device = UserDeviceEntity(finalString, System.currentTimeMillis(), System.currentTimeMillis())
        val userDeviceDao = RoomDb.getAppDatabase(this.context!!)?.userDeviceDao()
        val id = userDeviceDao?.insert(device)
        println("saved device $device with id=$id")

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addManufacturerData(1023, serviceDataByteArray)
            .build()

        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
        advertisingActive = true

        return true
    }

    fun stopAdvertising() {
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
            bluetoothManager.adapter.bluetoothLeAdvertiser
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        advertisingActive = false
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            if (!startBleServer()) {
                //insertLogs(ADV_TAG, "Unable to create GATT server")
            }
        }

        override fun onStartFailure(errorCode: Int) {
            //insertLogs(ADV_TAG, "Failed to start advertising: errorCode $errorCode")
        }
    }

    private fun startBleServer(): Boolean {
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        return bluetoothGattServer?.addService(createService()) ?: false
    }

    private fun createService(): BluetoothGattService {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val characteristic = BluetoothGattCharacteristic(
            MAIN_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(characteristic)

        return service
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Log.d(ADV_TAG, "Connected to ${device.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //Log.d(ADV_TAG, "Disconnected from ${device.address}")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            when (characteristic.uuid) {
                MAIN_CHARACTERISTIC_UUID -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        //CryptoUtil.getCurrentRpi()
                        ByteArray(100)
                    )

                }
                else -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            when (characteristic.uuid) {
                MAIN_CHARACTERISTIC_UUID -> {
                    value?.let { data ->
                        print(data)


                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null
                        )
                    } ?: run {

                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null
                        )
                    }
                }
                else -> {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }
    }

    fun stopServer() {

        bluetoothGattServer?.close()
    }

}

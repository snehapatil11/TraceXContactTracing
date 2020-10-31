package com.example.tracexcontacttracing.blemodule


import com.example.tracexcontacttracing.blemodule.DeviceData

interface OnDeviceScanListener {

    /**
     * Scan Completed -
     *
     * @param deviceDataList - Send available devices as a list to the init Activity
     * The List Contain, device name and mac address,
     */
    fun onScanCompleted(deviceDataList: DeviceData)
}
package de.troido.bleacon.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.os.Handler
import de.troido.bleacon.BleaconData
import de.troido.bleacon.util.postDelayed

private val SCAN_SETTINGS =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

abstract class BleaconScanner(
        private val filters: List<ScanFilter>,
        private val onDeviceFound: (BleaconScanner, BluetoothDevice, List<BleaconData>) -> Unit
) {
    private val handler = Handler()
    private val scanner = obtainScanner()

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.run {
                scanRecord?.getManufacturerSpecificData(NORDIC_ID)?.let {
                    onDeviceFound(this@BleaconScanner, device, deserialize(it))
                }
            }
        }
    }

    abstract protected fun deserialize(payload: ByteArray): List<BleaconData>

    fun start() = handler.post { scanner.startScan(filters, SCAN_SETTINGS, callback) }

    fun stop() = handler.post { scanner.stopScan(callback) }

    fun pause(millis: Long) {
        stop()
        handler.postDelayed(millis) { scanner.startScan(callback) }
    }
}

private fun obtainScanner(): BluetoothLeScanner {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    if (!adapter.isEnabled) {
        adapter.enable()
    }
    while (true) {
        adapter.bluetoothLeScanner?.let { return it }
        Thread.sleep(50)
    }
}

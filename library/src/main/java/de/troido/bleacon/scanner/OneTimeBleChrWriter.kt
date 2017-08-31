package de.troido.bleacon.scanner

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import de.troido.ekstend.serial.toHex
import kotlin.concurrent.thread

class OneTimeBleChrWriter(
        private val chr: BluetoothGattCharacteristic,
        private val gatt: BluetoothGatt
): BleChrWriter {

    override fun write(value: ByteArray) {
        thread {
            Log.d("WRITING", "WRITING ${value.toHex()}")

            chr.value = value
            while (!gatt.writeCharacteristic(chr)) Unit

            Log.d("WRITTEN", "WRITTEN ${value.toHex()}")
        }
    }
}

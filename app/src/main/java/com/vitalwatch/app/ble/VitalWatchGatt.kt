package com.vitalwatch.app.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.vitalwatch.app.domain.model.HRVFeatures
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

object VitalWatchUUIDs {
    val SERVICE_UUID: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    val HRV_FEATURES_CHAR_UUID: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
    val DEVICE_STATUS_CHAR_UUID: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
    val SYNC_TIME_CHAR_UUID: UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb")
    val CONTROL_CHAR_UUID: UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb")
    val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}

@SuppressLint("MissingPermission")
class VitalWatchGatt(
    private val context: Context,
    private val device: BluetoothDevice,
    private val stateFlow: MutableStateFlow<BleState>,
    private val onMeasurementComplete: (HRVFeatures) -> Unit
) {
    private val tag = "VitalWatchGatt"
    private var gatt: BluetoothGatt? = null
    private var progress = 0

    private val callback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(tag, "Connected to ${device.name}")
                    stateFlow.value = BleState.Connected(device.name ?: "VitalWatch", battery = 0)
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(tag, "Disconnected")
                    stateFlow.value = BleState.Disconnected
                    gatt.close()
                    this@VitalWatchGatt.gatt = null
                }
                else -> {}
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                stateFlow.value = BleState.Error("Service discovery failed: $status")
                return
            }
            val service = gatt.getService(VitalWatchUUIDs.SERVICE_UUID)
            if (service == null) {
                stateFlow.value = BleState.Error("VitalWatch service not found")
                return
            }
            // Enable notifications on HRV_FEATURES characteristic
            val hrvChar = service.getCharacteristic(VitalWatchUUIDs.HRV_FEATURES_CHAR_UUID)
            if (hrvChar != null) {
                gatt.setCharacteristicNotification(hrvChar, true)
                val descriptor = hrvChar.getDescriptor(VitalWatchUUIDs.CLIENT_CHARACTERISTIC_CONFIG)
                descriptor?.let {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            when (characteristic.uuid) {
                VitalWatchUUIDs.HRV_FEATURES_CHAR_UUID -> {
                    val data = characteristic.value
                    if (data != null && data.size >= 77) {
                        parseHrvPacket(data)?.let { features ->
                            onMeasurementComplete(features)
                        }
                    }
                }
                VitalWatchUUIDs.DEVICE_STATUS_CHAR_UUID -> {
                    val data = characteristic.value
                    if (data != null && data.size >= 2) {
                        progress = data[0].toInt() and 0xFF
                        stateFlow.value = BleState.MeasurementInProgress(progress)
                    }
                }
                else -> {}
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS &&
                characteristic.uuid == VitalWatchUUIDs.DEVICE_STATUS_CHAR_UUID
            ) {
                val battery = characteristic.value?.getOrNull(1)?.toInt()?.and(0xFF) ?: 0
                val current = stateFlow.value
                if (current is BleState.Connected) {
                    stateFlow.value = current.copy(battery = battery)
                }
            }
        }
    }

    fun connect() {
        stateFlow.value = BleState.Connecting(device.name ?: "VitalWatch")
        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        gatt?.disconnect()
    }

    fun startMeasurement() {
        val service = gatt?.getService(VitalWatchUUIDs.SERVICE_UUID) ?: return
        val controlChar = service.getCharacteristic(VitalWatchUUIDs.CONTROL_CHAR_UUID) ?: return
        controlChar.value = byteArrayOf(0x01)
        gatt?.writeCharacteristic(controlChar)
        stateFlow.value = BleState.MeasurementInProgress(0)
    }

    private fun parseHrvPacket(data: ByteArray): HRVFeatures? {
        return try {
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val timestamp = buf.int.toLong() and 0xFFFFFFFFL
            val seqNum = buf.int.toLong() and 0xFFFFFFFFL
            val features = FloatArray(18) { buf.float }
            val crc = data[76].toInt() and 0xFF
            if (!verifyCrc8(data, 76, crc)) {
                Log.w(tag, "CRC mismatch in HRV packet")
                return null
            }
            HRVFeatures(
                timestamp = timestamp * 1000,
                sequenceNumber = seqNum,
                ppgRateSd = features[0],
                sd1d = features[1],
                sdrmssd = features[2],
                meanNn = features[3],
                fuzzyEn = features[4],
                sdsd = features[5],
                cvnn = features[6],
                tp = features[7],
                cvi = features[8],
                minNn = features[9],
                madNn = features[10],
                mcvnn = features[11],
                pi = features[12],
                si = features[13],
                maxNn = features[14],
                shanEn = features[15],
                prc20nn = features[16],
                mfdfaAlpha1Mean = features[17]
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse HRV packet", e)
            null
        }
    }

    private fun verifyCrc8(data: ByteArray, length: Int, expected: Int): Boolean {
        var crc = 0
        for (i in 0 until length) {
            crc = crc xor (data[i].toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 0x80 != 0) (crc shl 1) xor 0x07 else crc shl 1
                crc = crc and 0xFF
            }
        }
        return crc == expected
    }
}

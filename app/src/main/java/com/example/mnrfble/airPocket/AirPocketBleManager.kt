package com.example.mnrfble.airPocket

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.getCharacteristic
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import com.example.mnrfble.util.HexUtil
//import com.example.mnrfble.roller.callback.RollerBatteryNotifyCallback
//import com.example.mnrfble.roller.callback.RollerResultNotifyCallback
//import com.example.mnrfble.roller.callback.RollerSoftWareVersionCallback
import java.util.UUID

/**
 * NRF 的 Android-Ble-library 库,连接设备,订阅通知写法
 */
class AirPocketBleManager(
    context: Context
) : AirPocket by AirPocketManageImpl(context)

private class AirPocketManageImpl(
    context: Context,
) : BleManager(context), AirPocket {
    private val scope = CoroutineScope(Dispatchers.IO)

    //LambdAdd
    private var nameCharacteristic: BluetoothGattCharacteristic? = null

    private var isServiceSupported = false

    /**
     * 连接设备
     */
    override suspend fun connect(device: BluetoothDevice?) {
        device?.let {
            Log.d("TTTT", "当前要连接的设备(${device.address})连接状态: $isConnected")
            if (isConnected) return
            Log.d("LambdA", "开始连接尝试")

            val result = kotlin.runCatching {
                connect(device).retry(3, 300)
                    .useAutoConnect(true) // 是否添加设备到自动重连
                    .timeout(3000).suspend()
            }

            if (result.isSuccess) {
                Log.d("LambdA", "连接成功")
            } else {
                Log.d("LambdA", "连接失败: ${result.exceptionOrNull()}")
            }
        }
    }


    /**
     * 连开设备
     */
    override fun disConnect() {
        Log.e("TTTT", "断开连接  $isConnected")

        val wasConnected = isReady
        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    override fun release() {
        // Cancel all coroutines.
        scope.cancel()

        val wasConnected = isReady
        // If the device wasn't connected, it means that ConnectRequest was still pending.
        // Cancelling queue will initiate disconnecting automatically.
        cancelQueue()

        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    /**
     * 设备的连接状态
     */
    override val mConnectState = stateAsFlow().map {
        when (it) {
            is ConnectionState.Connecting, is ConnectionState.Initializing -> AirPocket.ConnectState.LOADING

            is ConnectionState.Ready -> AirPocket.ConnectState.READY
            is ConnectionState.Disconnecting, is ConnectionState.Disconnected -> AirPocket.ConnectState.NOT_AVAILABLE
        }
    }.stateIn(scope, SharingStarted.Lazily, AirPocket.ConnectState.NOT_AVAILABLE)//冷流变热流

    //检验设备是否拥有我们所需的服务与特征
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        Log.d("LambdA", "test-service")
        gatt.services.forEach { services ->
            when (services.uuid) {
                //LambdAdd
                UUID.fromString("00001800-0000-1000-8000-00805f9b34fb") -> {
                    nameCharacteristic = services.getCharacteristic(
                        UUID.fromString(BleDevicesCommand.NAME_READ_UUID),
                        BluetoothGattCharacteristic.PROPERTY_READ
                    )
                    Log.e("LambdA", "名称特征值: $nameCharacteristic")
                }

            }
        }
        isServiceSupported = (nameCharacteristic != null)
        return isServiceSupported
    }

    /**
     * 设备连接成功后的初始化处理
     * 一般是订阅 notify/indicate 处理
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        Log.d("LambdA", "111111111111")
        /**
        //订阅渔轮电量通知
        val flow: Flow<RollerBatteryNotifyCallback> =
            setNotificationCallback(rollerBatteryCharacteristic).asValidResponseFlowExt()
        scope.launch {
            flow.map { it.batteryValue }.collect { _batteryValue.tryEmit(it) }
        }

        enableNotifications(rollerBatteryCharacteristic).enqueue()

        //订阅渔轮写入结果通知
        val resultNotifyFlow: Flow<RollerResultNotifyCallback> =
            setNotificationCallback(rollerResultNotifyCharacteristic).asValidResponseFlowExt()
        scope.launch {
            resultNotifyFlow.map { it.rawData }.collect {
                //todo callback
                // _data.value
            }
        }

        enableNotifications(rollerResultNotifyCharacteristic).enqueue()

        //读取固件版本号
        readCharacteristic(rollerSoftWareVersionCharacteristic).with(softWareVersionCallback)
            .enqueue()
        //
        */

    }

    /**
     * 该方法下,设置所有服务和特征码无效
     */
    override fun onServicesInvalidated() {
        nameCharacteristic =null
    }

}

@ExperimentalCoroutinesApi
inline fun <reified T : ProfileReadResponse> ValueChangedCallback.asValidResponseFlowExt(): Flow<T> =
    callbackFlow {
        // Make sure the callbacks are called without unnecessary delay.
        setHandler(null)
        with { device, data ->
            T::class.java.getDeclaredConstructor().newInstance()
                .apply { onDataReceived(device, data) }.takeIf { it.isValid }?.let { trySend(it) }
        }
        awaitClose {
            // There's no way to unregister the callback from here.
            with { _, _ -> }
        }
    }
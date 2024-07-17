package com.example.mnrfble.airPocket

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.example.mnrfble.airPocket.callback.ResultCallback
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
import com.example.mnrfble.databinding.ActivityDeviceDetailBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

/**
 * 利用NRF 的 Android-Ble-library 库,连接设备,订阅通知写法
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
    private var uartRxCharacteristic: BluetoothGattCharacteristic? = null
    private var uartTxCharacteristic: BluetoothGattCharacteristic? = null

    private var isServiceSupported = false

    var mBinding:ActivityDeviceDetailBinding? = null

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
                mBinding?.textView?.text="连接成功"
            } else {
                Log.d("LambdA", "连接失败: ${result.exceptionOrNull()}")
            }
        }
    }


    /**
     * 断连设备
     */
    override fun disConnect() {
        Log.e("TTTT", "断开连接  $isConnected")
        mBinding?.textView?.text="断开连接成功"

        val wasConnected = isReady
        // If the device was connected, we have to disconnect manually.
        if (wasConnected) {
            disconnect().enqueue()
        }
    }

    /**
     *  写入命令UART
     */
    override fun writeCommand(text: String) {
        if (text.isNotEmpty()) {
            val commandBytes = text.toByteArray()
            sendCommand(commandBytes)
            Log.d("LambdA","successfully writen")
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

                UUID.fromString(BleDevicesCommand.UART_SERVICE_UUID) -> {
                    uartRxCharacteristic = services.getCharacteristic(UUID.fromString(BleDevicesCommand.UART_RX_UUID))
                    uartTxCharacteristic = services.getCharacteristic(UUID.fromString(BleDevicesCommand.UART_TX_UUID))
                    val cccdDescriptor = uartRxCharacteristic?.getDescriptor(UUID.fromString(BleDevicesCommand.CCCD_UUID))
                    Log.e("LambdA", "UART connect")
                }

            }
        }
        isServiceSupported = (nameCharacteristic != null) && uartRxCharacteristic != null && uartTxCharacteristic != null
        return isServiceSupported
    }

    /**
     * 设备连接成功后的初始化处理
     * 订阅 notify
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initialize() {
        Log.d("LambdA", "111111111111")
        uartRxCharacteristic?.let { characteristic ->
            val flow:Flow<ResultCallback> = setNotificationCallback(characteristic).asValidResponseFlowExt()
            scope.launch {
                flow.map { it.data }.collect {data->
                    Log.d("LambdA","data:${data}")
                    withContext(Main) {
                        mBinding?.textView?.text=data
                    }
                }
            }
            enableNotifications(characteristic).enqueue()
        }

    }

    /**
     * 该方法下,设置所有服务和特征码无效
     */
    override fun onServicesInvalidated() {
        nameCharacteristic =null
        uartTxCharacteristic = null
        uartRxCharacteristic = null
    }

    fun sendCommand(data: ByteArray) {
        uartTxCharacteristic?.let {
            val writeOperation = writeCharacteristic(it, data)
            writeOperation.with { device, data ->
                Log.d("UART", "Sent data: ${data}")
            }.enqueue()
        }
    }


    override fun passBinding(binding:ActivityDeviceDetailBinding){
        this.mBinding = binding
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
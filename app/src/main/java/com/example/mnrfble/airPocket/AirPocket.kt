package com.example.mnrfble.airPocket

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface AirPocket {



    /**
     * 设备连接状态
     */
    enum class ConnectState {
        LOADING,
        READY,
        NOT_AVAILABLE
    }

    /**
     * 设备的连接状态
     */
    val mConnectState: StateFlow<ConnectState>


    /**
     * Connects to the device.
     */
    suspend fun connect(device: BluetoothDevice?)

    /**
     * 断开连接
     */
    fun disConnect()

    /**
     * 释放资源
     */
    fun release()



}
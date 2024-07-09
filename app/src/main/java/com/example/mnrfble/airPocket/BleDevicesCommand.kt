package com.example.mnrfble.airPocket

/**
 * 主要是用了老师给的那个app里的几个UUID，nRF connect
 */
object BleDevicesCommand {
    //LambdAdd
    const val NAME_READ_UUID = "00002A00-0000-1000-8000-00805f9b34fb"

    const val UART_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"

    const val UART_TX_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"

    const val UART_RX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"

    const val CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb"

}
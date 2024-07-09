package com.example.mnrfble.scanner

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import com.example.mnrfble.R
import com.example.mnrfble.scanner.model.BleScanResult

class ScanDevicesAdapter :
    BaseQuickAdapter<BleScanResult, BaseViewHolder>(R.layout.item_scan_devices) {

    override fun convert(holder: BaseViewHolder, item: BleScanResult) {
        with(holder) {
            setText(R.id.tv_deviceName, item.name)
            setText(R.id.tv_deviceMac, item.address)
        }

    }

}
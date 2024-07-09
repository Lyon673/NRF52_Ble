package com.example.mnrfble.repository

/**
 * 一个委托接口
 */
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import com.example.mnrfble.airPocket.AirPocket
import javax.inject.Inject

class AirPocketRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val airPocket: AirPocket,
) : AirPocket by airPocket {

}
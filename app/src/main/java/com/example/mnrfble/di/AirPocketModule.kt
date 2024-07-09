package com.example.mnrfble.di

/**
 * 用Dagger Hilt为AirPocket AirPocketBleManager的实例，并将其绑定到AirPocket接口上
 */
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import com.example.mnrfble.airPocket.AirPocket
import com.example.mnrfble.airPocket.AirPocketBleManager

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
abstract class AirPocketModule {

    companion object {

        @Provides
        @ViewModelScoped
        fun provideAirPocketManager(
            @ApplicationContext context: Context,
        ) = AirPocketBleManager(context)


    }

    @Binds
    abstract fun bindAirPocket(
        airPocketBleManager: AirPocketBleManager
    ): AirPocket

}
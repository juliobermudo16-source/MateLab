package com.matelab.islas.core

import android.app.Application
import com.matelab.islas.core.di.AppContainer

class MateLabApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

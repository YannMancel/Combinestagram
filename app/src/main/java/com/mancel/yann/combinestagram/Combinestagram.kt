package com.mancel.yann.combinestagram

import android.app.Application
import timber.log.Timber

/**
 * Created by Yann MANCEL on 13/05/2020.
 * Name of the project: Combinestagram
 * Name of the package: com.mancel.yann.combinestagram
 *
 * An [Application] subclass.
 */
class CombinestagramApplication : Application() {

    // FIELDS --------------------------------------------------------------------------------------

    // -- Application --

    override fun onCreate() {
        super.onCreate()

        // Timber: Logger
        Timber.plant(Timber.DebugTree())
    }
}
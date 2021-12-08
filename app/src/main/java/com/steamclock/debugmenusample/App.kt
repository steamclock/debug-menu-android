package com.steamclock.debugmenusample

import android.app.Application
import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu_sharedprefs.SharedPrefsPersistence
import com.steamclock.debugmenu_ui.ComposeDebugMenuDisplay

/**
 * debugmenu
 * Created by jake on 2021-12-06, 10:24 a.m.
 */
@Suppress("unused")
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DebugMenu.initialize("123321",
            ComposeDebugMenuDisplay(this),
            SharedPrefsPersistence(this)
        )
    }
}
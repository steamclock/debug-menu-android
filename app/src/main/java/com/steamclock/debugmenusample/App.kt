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
        DebugMenu.initialize("a320480f534776bddb5cdb54b1e93d210a3c7d199e80a23c1b2178497b184c76",
            ComposeDebugMenuDisplay(this),
            SharedPrefsPersistence(this)
        )
    }
}
package com.steamclock.debugmenu.display

import com.steamclock.debugmenu.DebugMenuState

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:32 p.m.
 */
interface DebugMenuDisplay {
    suspend fun displayMenu(state: DebugMenuState, menuKey: String)
    suspend fun displayCodeEntry()
}
package com.steamclock.debugmenu.display

import com.steamclock.debugmenu.DebugOption

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:32 p.m.
 */
interface DebugMenuDisplay {
    suspend fun displayMenu(title: String, options: List<DebugOption>)
    suspend fun displayCodeEntry()
}
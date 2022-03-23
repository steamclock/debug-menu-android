package com.steamclock.debugmenu

import com.steamclock.debugmenu.display.DebugMenuDisplay
import com.steamclock.debugmenu.display.LogDisplay
import com.steamclock.debugmenu.persistence.DebugMenuPersistence
import com.steamclock.debugmenu.persistence.InMemoryPersistence

data class DebugMenuState(val title: String,
                          val header: String? = null,
                          val footer: String? = null,
                          val options: Map<String, List<DebugOption>> = mapOf(),
                          val persistence: DebugMenuPersistence = InMemoryPersistence(),
                          val display: DebugMenuDisplay = LogDisplay())
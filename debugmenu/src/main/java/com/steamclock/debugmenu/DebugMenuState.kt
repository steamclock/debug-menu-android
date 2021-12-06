package com.steamclock.debugmenu

data class DebugMenuState(val title: String,
                          val options: Map<String, List<DebugOption>> = mapOf(),
                          val persistence: DebugMenuPersistence = InMemoryPersistence(),
                          val display: DebugMenuDisplay = LogDisplay())
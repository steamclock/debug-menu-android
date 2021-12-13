package com.steamclock.debugmenu.display

import com.steamclock.debugmenu.*

/**
 * debugmenu
 * Created by jake on 2021-12-06, 1:56 p.m.
 */
class LogDisplay: DebugMenuDisplay {
    override suspend fun displayMenu(title: String, options: List<DebugOption>) {
        println("Debug Menu: $title")
        options.forEach {
            when (it) {
                is Action ->
                    println("   ${it.title} - Action")
                is Toggle ->
                    println("   ${it.title} - ${DebugMenu.instance.valueBlocking<Boolean>(it.key)}")
                is DoubleValue ->
                    println("   ${it.title} - ${DebugMenu.instance.valueBlocking<Double>(it.key)}")
                is IntValue ->
                    println("   ${it.title} - ${DebugMenu.instance.valueBlocking<Int>(it.key)}")
                is LongValue ->
                    println("   ${it.title} - ${DebugMenu.instance.valueBlocking<Long>(it.key)}")
            }

        }
    }

    override suspend fun displayCodeEntry() {
        println("Debug Menu: Displaying Code Entry")
    }
}
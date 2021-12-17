package com.steamclock.debugmenu_annotation

/**
 * debugmenu
 * Created by jake on 2021-12-08, 2:11 p.m.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugToggle(val title: String, val defaultValue: Boolean = false, val menuKey: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugAction(val title: String, val menuKey: String = "")
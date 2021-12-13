package com.steamclock.debugmenu_annotation

/**
 * debugmenu
 * Created by jake on 2021-12-08, 2:11 p.m.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugBoolean(val title: String, val defaultValue: Boolean = false, val menuKey: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugAction(val title: String, val menuKey: String = "")

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugInt(val title: String, val defaultValue: Int = 0, val menuKey: String = "")

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugLong(val title: String, val defaultValue: Long = 0, val menuKey: String = "")

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugDouble(val title: String, val defaultValue: Double = 0.0, val menuKey: String = "")

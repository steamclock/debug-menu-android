package com.steamclock.debugmenu_annotation

/**
 * debugmenu
 * Created by jake on 2021-12-08, 2:11 p.m.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugBoolean(val title: String, val defaultValue: Boolean = false, val menuKey: String = "", val isVisible: Boolean = true)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugAction(val title: String, val menuKey: String = "", val isVisible: Boolean = true)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugInt(val title: String, val defaultValue: Int = 0, val menuKey: String = "", val isVisible: Boolean = true)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugLong(val title: String, val defaultValue: Long = 0, val menuKey: String = "", val isVisible: Boolean = true)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugDouble(val title: String, val defaultValue: Double = 0.0, val menuKey: String = "", val isVisible: Boolean = true)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugSelection(val title: String, val defaultIndex: Int = -1, val menuKey: String = "", val options: Array<String>, val isVisible: Boolean = true)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugTextProvider(val menuKey: String = "", val isVisible: Boolean = true)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DebugSelectionProvider(val title: String, val defaultIndex: Int = -1, val menuKey: String = "", val isVisible: Boolean = true)


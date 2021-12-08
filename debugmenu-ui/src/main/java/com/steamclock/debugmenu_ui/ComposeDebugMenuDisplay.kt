package com.steamclock.debugmenu_ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu.DebugOption
import com.steamclock.debugmenu.display.DebugMenuDisplay
import com.steamclock.debugmenu_ui.components.CodeEntry
import com.steamclock.debugmenu_ui.components.Menu
import java.lang.ref.WeakReference


class ComposeDebugMenuDisplay(context: Context) : DebugMenuDisplay {
    private var contextReference: WeakReference<Context>? = null
    private var currentDialog: WeakReference<DebugMenuDialogFragment>? = null
    private var activityReference: WeakReference<AppCompatActivity>? = null

    init {
        // always use app context
        val appContext = context.applicationContext
        contextReference = WeakReference(appContext)
        monitor(appContext as Application)
    }

    private fun monitor(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                activityReference = WeakReference(p0 as AppCompatActivity)
            }
            override fun onActivityStarted(p0: Activity) {
                activityReference = WeakReference(p0 as AppCompatActivity)
            }
            override fun onActivityResumed(p0: Activity) {
                activityReference = WeakReference(p0 as AppCompatActivity)
            }
            override fun onActivityPaused(p0: Activity) {
                activityReference = null
            }
            override fun onActivityStopped(p0: Activity) {}
            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
            override fun onActivityDestroyed(p0: Activity) {}
        })
    }

    private fun showDialog(content: @Composable (DebugMenuDialogFragment) -> Unit) {
        val activity = activityReference?.get() ?: return
        currentDialog?.get()?.dismissAllowingStateLoss()
        val dialog = DebugMenuDialogFragment(content)
        currentDialog = WeakReference(dialog)
        dialog.show(activity.supportFragmentManager, null)
    }

    override suspend fun displayMenu(title: String, options: List<DebugOption>) {
        showDialog { Menu(title, options) }
    }

    override suspend fun displayCodeEntry() {
        showDialog {
            CodeEntry(onSubmit = { code ->
                DebugMenu.instance.enterCode(code)
                it.dismiss()
                DebugMenu.instance.show()
            })
        }
    }
}


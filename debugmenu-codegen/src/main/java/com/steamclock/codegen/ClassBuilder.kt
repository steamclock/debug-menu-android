package com.steamclock.codegen

import com.steamclock.debugmenu.Action
import com.steamclock.debugmenu.DebugOption
import com.steamclock.debugmenu.Toggle

class MenuClassBuilder(private val menuKey: String, private val menuName: String, private val options: List<DebugOption>) {
    private val optionsText: String
        get() {
            var string = ""
            for (option in options) {
                when (option) {
                    is Action -> TODO()
                    is Toggle -> {
                        val toggleText = "Toggle(title = \"${option.title}\", key = \"${option.key}\", defaultValue = ${option.defaultValue})"
                        string += "DebugMenu.instance.addOptions(\"$menuKey\", $toggleText)\n"
                    }
                }
            }
            return string
        }

    private val debugValues: String
        get() {
            var string = ""
            for (option in options) {
                when (option) {
                    is Action -> { /* no op */ }
                    is Toggle -> {
                        val toggleText = "val ${option.key} = DebugValue<Boolean>(DebugMenu.instance.flow(\"${option.key}\"))\n"
                        string += toggleText
                    }
                }
            }
            return string
        }

    private val contentTemplate = """
package com.steamclock.debugmenu.generated
import android.view.View
import com.steamclock.debugmenu.*
import kotlinx.coroutines.runBlocking
import com.steamclock.debugmenu_annotation.DebugValue

class $menuName private constructor() {
  init {
      runBlocking {
        $optionsText
    }
  }
  companion object {
      const val key = "$menuKey"
      private val instance = $menuName()
      $debugValues
      
      fun show() = runBlocking {
        DebugMenu.instance.show("$menuKey")
      }
  }
}
    """.trimIndent()

    fun getContent(): String {
        return contentTemplate
    }
}
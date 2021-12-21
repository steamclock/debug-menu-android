package com.steamclock.codegen

import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu.OptionSelection
import com.steamclock.debugmenu.flow
import com.steamclock.debugmenu_annotation.DebugValue
import kotlinx.coroutines.flow.mapNotNull

internal class MenuClassBuilder(
    private val menuKey: String,
    private val menuName: String,
    private val options: List<AnnotationWrapper>
) {
    private val optionsText: String
        get() = options.joinToString("\n            ") { option ->
            when (option) {
                is ActionWrapper -> {
                    if (option.isGlobal) {
                        val actionText =
                            "Action(title = \"${option.title}\", onClick = { ${option.functionName}() })"
                        "DebugMenu.instance.addOptions(key, $actionText)"
                    } else {
                        ""
                    }
                }
                is BooleanWrapper -> {
                    val toggle = option.toggle
                    val toggleText =
                        "BooleanValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is DoubleWrapper -> {
                    val toggle = option.doubleValue
                    val toggleText =
                        "DoubleValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is IntWrapper -> {
                    val toggle = option.intValue
                    val toggleText =
                        "IntValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is LongWrapper -> {
                    val toggle = option.longValue
                    val toggleText =
                        "LongValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is SelectionWrapper -> {
                    val selection = option.selectionValue
                    val options = selection.options.joinToString(",") { "\"$it\"" }
                    val text =
                        "OptionSelection(title = \"${selection.title}\", key = \"${selection.key}\", options = listOf(${options}), defaultIndex = ${selection.defaultIndex})"
                    "DebugMenu.instance.addOptions(key, $text)"
                }
            }
        }

    private val debugValues: String
        get() = options.joinToString("\n        ") {
            when (it) {
                is ActionWrapper -> {
                    ""
                }
                is BooleanWrapper -> {
                    val toggle = it.toggle
                    "val ${toggle.key} = DebugValue<Boolean>(DebugMenu.instance.flow(\"${toggle.key}\"))"
                }
                is DoubleWrapper -> {
                    val toggle = it.doubleValue
                    "val ${toggle.key} = DebugValue<Double>(DebugMenu.instance.flow(\"${toggle.key}\"))"
                }
                is IntWrapper -> {
                    val toggle = it.intValue
                    "val ${toggle.key} = DebugValue<Int>(DebugMenu.instance.flow(\"${toggle.key}\"))"
                }
                is LongWrapper -> {
                    val toggle = it.longValue
                    "val ${toggle.key} = DebugValue<Long>(DebugMenu.instance.flow(\"${toggle.key}\"))"
                }
                is SelectionWrapper -> {
                    val toggle = it.selectionValue
"""
        val ${toggle.key} = DebugValue<String>(DebugMenu.instance.flow<Int>("${toggle.key}").mapNotNull {
            val option = (DebugMenu.instance.optionForKey("${toggle.key}") as? OptionSelection) ?: return@mapNotNull null
            option.options[it]
        })
"""
                }
            }
        }

    private val globalActionImports: String
        get() = options
            .filterIsInstance<ActionWrapper>()
            .filter { it.isGlobal }
            .joinToString("\n") { "import ${it.packageName}.${it.functionName}" }


    private val actionsByParents: Map<String, List<ActionWrapper>>
        get() {
            return options
                .filterIsInstance<ActionWrapper>()
                .filter { !it.isGlobal }
                .groupBy { it.parentClass }
        }

    private val referencedParents: Set<String>
        get() {
            return options
                .filterIsInstance<ActionWrapper>()
                .filter { !it.isGlobal }
                .map { it.parentClass }.toSet()
        }

    private val weakReferences: String
        get() {
            return referencedParents.joinToString("\n    ") {
                val parentName = it.split(".").last()
                "var ${parentName}Ref: WeakReference<$parentName>? = null"
            }
        }

    private val parentImports: String = referencedParents.joinToString("\n") {
        "import $it"
    }

    private val initFunctions: String
        get() {
            return actionsByParents.keys.joinToString("\n        ") { parent ->
                val actions = actionsByParents[parent] ?: listOf()
                val parentName = parent.split(".").last()

                val actionStrings = actions.joinToString {
                    """
            val action = Action(title = "${it.title}", onClick = {
                instance.${parentName}Ref?.get()?.${it.functionName}()
            })
            DebugMenu.instance.addOptions(key, action)    
                    """
                }

                """
        fun initialize(parent: $parentName) = runBlocking {
            instance.${parentName}Ref = WeakReference(parent)
        $actionStrings
        }
                """
            }
        }

    private val contentTemplate = """
package com.steamclock.debugmenu.generated

import com.steamclock.debugmenu.*
import kotlinx.coroutines.runBlocking
import com.steamclock.debugmenu_annotation.DebugValue
import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.mapNotNull
$parentImports
$globalActionImports

class $menuName private constructor() {
    $weakReferences
    
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
            DebugMenu.instance.show(key)
        }
        
        $initFunctions
    }
}
    """

    fun getContent(): String {
        return contentTemplate
    }

    fun generatedInitFunctions(): Pair<String, List<String>> {
        // DebugMenu -> MainActivity, SettingsActivity
        return menuName to actionsByParents.keys.map { parent -> parent }
    }
}
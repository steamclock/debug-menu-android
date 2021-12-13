package com.steamclock.codegen

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
                        "DebugMenu.instance.addOptions(\"$menuKey\", $actionText)"
                    } else {
                        ""
                    }
                }
                is ToggleWrapper -> {
                    val toggle = option.toggle
                    val toggleText =
                        "Toggle(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue})"
                    "DebugMenu.instance.addOptions(\"$menuKey\", $toggleText)"
                }
            }
        }

    private val debugValues: String
        get() = options.joinToString("\n        ") {
            when (it) {
                is ActionWrapper -> {
                    ""
                }
                is ToggleWrapper -> {
                    val toggle = it.toggle
                    "val ${toggle.key} = DebugValue<Boolean>(DebugMenu.instance.flow(\"${toggle.key}\"))"
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

                val actionStrings = actions.joinToString() {
                    """
            val action = Action(title = "${it.title}", onClick = {
                instance.${parentName}Ref?.get()?.${it.functionName}()
            })
            DebugMenu.instance.addOptions("$menuKey", action)    
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
            DebugMenu.instance.show("$menuKey")
        }
        
        $initFunctions
    }
}
    """.trimIndent()

    fun getContent(): String {
        return contentTemplate
    }

    fun generatedInitFunctions(): Pair<String, List<String>> {
        // DebugMenu -> MainActivity, SettingsActivity
        return menuName to actionsByParents.keys.map { parent -> parent }
    }
}
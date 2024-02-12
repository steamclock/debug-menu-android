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
                            "Action(title = \"${option.title}\", onClick = { ${option.functionName}() }, isVisible = ${option.isVisible})"
                        "DebugMenu.instance.addOptions(key, $actionText)"
                    } else {
                        ""
                    }
                }
                is BooleanWrapper -> {
                    val toggle = option.toggle
                    val toggleText =
                        "BooleanValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue}, isVisible = ${toggle.isVisible})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is DoubleWrapper -> {
                    val toggle = option.doubleValue
                    val toggleText =
                        "DoubleValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue}, isVisible = ${toggle.isVisible})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is IntWrapper -> {
                    val toggle = option.intValue
                    val toggleText =
                        "IntValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue}, isVisible = ${toggle.isVisible})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is LongWrapper -> {
                    val toggle = option.longValue
                    val toggleText =
                        "LongValue(title = \"${toggle.title}\", key = \"${toggle.key}\", defaultValue = ${toggle.defaultValue}, isVisible = ${toggle.isVisible})"
                    "DebugMenu.instance.addOptions(key, $toggleText)"
                }
                is SelectionWrapper -> {
                    val selection = option.selectionValue
                    val options = selection.options.joinToString(",") { "\"$it\"" }
                    val text =
                        "OptionSelection(title = \"${selection.title}\", key = \"${selection.key}\", options = listOf(${options}), defaultIndex = ${selection.defaultIndex}, isVisible = ${selection.isVisible})"
                    "DebugMenu.instance.addOptions(key, $text)"
                }
                is TextValueWrapper -> {
                    ""
                }
                is SelectionProviderWrapper -> {
                    ""
                }
            }
        }

    private val debugValues: String
        get() = options.joinToString("\n        ") {
            when (it) {
                is ActionWrapper -> {
                    ""
                }
                is TextValueWrapper -> {
                    ""
                }
                is SelectionProviderWrapper -> {
                    """
        val ${it.key}Selection = DebugValue(flow<String?> {
            DebugMenu.instance.flow<Int?>("${it.key}").collect {
                val option = (DebugMenu.instance.optionForKey("${it.key}") as? OptionSelection) ?: return@collect
                emit(if (it != null) option.options.get(it) else null)
            }
        })
                    """
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
        val ${toggle.key} = DebugValue(flow<String?> {
            DebugMenu.instance.flow<Int?>("${toggle.key}").collect {
                val option = (DebugMenu.instance.optionForKey("${toggle.key}") as? OptionSelection) ?: return@collect
                emit(if (it != null) option.options.get(it) else null)
            }
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

    private val textProviderByParents: Map<String, List<TextValueWrapper>>
        get() {
            return options
                .filterIsInstance<TextValueWrapper>()
                .groupBy { it.parentClass }
        }

    private val selectionProviderByParents: Map<String, List<SelectionProviderWrapper>>
        get() {
            return options
                .filterIsInstance<SelectionProviderWrapper>()
                .groupBy { it.parentClass }
        }

    private val referencedParents: Set<String>
        get() {
            val actions = options
                .filterIsInstance<ActionWrapper>()
                .filter { !it.isGlobal }
                .map { it.parentClass }
            val textProviders = options
                .filterIsInstance<TextValueWrapper>()
                .map { it.parentClass }
            val selectionProviders = options
                .filterIsInstance<SelectionProviderWrapper>()
                .map { it.parentClass }
            return (actions + textProviders + selectionProviders).toSet()
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
            return referencedParents.joinToString(separator = "\n        ") { parent ->
                val parentName = parent.split(".").last()
                val actions = actionsByParents[parent] ?: listOf()
                val textProviders = textProviderByParents[parent] ?: listOf()
                val selectionProviders = selectionProviderByParents[parent] ?: listOf()

                val actionStrings = actions.joinToString("") {
                    """
            val ${it.functionName}Action = Action(title = "${it.title}", onClick = {
                instance.${parentName}Ref?.get()?.${it.functionName}()
            }, isVisible = ${it.isVisible})
            DebugMenu.instance.addOptions(key, ${it.functionName}Action)    
                    """
                }

                val textProviderStrings = textProviders.joinToString("") {
                        """
            val ${it.functionName}Text = instance.${parentName}Ref?.get()?.${it.functionName}() as? Any
            if (${it.functionName}Text !is String) {
                throw Exception("${parent}.${it.functionName} is marked as a StringValueProvider, it must return a String value")
            }
            val ${it.functionName}TextProvider = TextDisplay(text = ${it.functionName}Text)
            DebugMenu.instance.addOptions(key, ${it.functionName}TextProvider)    
                    """
                }

                val selectionProviderStrings = selectionProviders.joinToString("") {
                    """
            val ${it.functionName}Selections = instance.${parentName}Ref?.get()?.${it.functionName}() as? Any
            if (${it.functionName}Selections !is List<*>) {
                throw Exception("${parent}.${it.functionName} is marked as a DebugSelectionProvider, it must return a List<String> value")
            }
            ${it.functionName}Selections.forEach { 
                if (it !is String) {
                    throw Exception("${parent}.${it.functionName} is marked as a DebugSelectionProvider, it must return a List<String> value")
                }
            }
            
            val ${it.functionName}Selection = OptionSelection(title = "${it.title}", key = "${it.key}", options = ${it.functionName}Selections as List<String>, defaultIndex = ${it.defaultIndex})
            DebugMenu.instance.addOptions(key, ${it.functionName}Selection)    
                    """
                }

                    """
        @Suppress("UNCHECKED_CAST")
        fun initialize(parent: $parentName) = runBlocking {
            instance.${parentName}Ref = WeakReference(parent)
        $actionStrings
        $textProviderStrings
        $selectionProviderStrings
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
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
        return menuName to referencedParents.map { parent -> parent }
    }
}
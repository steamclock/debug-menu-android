package com.steamclock.codegen

import com.google.auto.service.AutoService
import com.steamclock.debugmenu.*
import com.steamclock.debugmenu.DebugMenu.Companion.DEBUG_GLOBAL_MENU
import com.steamclock.debugmenu_annotation.*
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

internal sealed class AnnotationWrapper
internal data class BooleanWrapper(val toggle: BooleanValue): AnnotationWrapper()
internal data class IntWrapper(val intValue: IntValue): AnnotationWrapper()
internal data class DoubleWrapper(val doubleValue: DoubleValue): AnnotationWrapper()
internal data class LongWrapper(val longValue: LongValue): AnnotationWrapper()
internal data class SelectionWrapper(val selectionValue: OptionSelection): AnnotationWrapper()
internal data class ActionWrapper(val title: String, val functionName: String, val parentClass: String, val packageName: String, val isGlobal: Boolean, val isVisible: Boolean): AnnotationWrapper()
internal data class TextValueWrapper(val functionName: String, val parentClass: String, val packageName: String, val isVisible: Boolean): AnnotationWrapper()
internal data class SelectionProviderWrapper(val title: String, val key: String, val defaultIndex: Int? = null, val functionName: String, val parentClass: String, val packageName: String, val isVisible: Boolean): AnnotationWrapper()

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor() {
    private val globalDebugKey = "GlobalDebugMenu"
    private var menus = hashMapOf<String, MutableList<AnnotationWrapper>>()
    private val initializationFunctions = mutableMapOf<String, MutableSet<String>>()

    private fun <T: Annotation> RoundEnvironment.forEach(java: Class<T>,
                                                         validKind: ElementKind = ElementKind.CLASS,
                                                         forEach: (Element, T) -> Unit): Boolean {
        getElementsAnnotatedWith(java)?.forEach { element ->
            if (element.kind != validKind) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "Can only be applied to $validKind, element: $element "
                )
                return false
            }

            val annotation = element.getAnnotationsByType(java)[0]
            forEach(element, annotation)
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            DebugBoolean::class.java.name,
            DebugInt::class.java.name,
            DebugDouble::class.java.name,
            DebugLong::class.java.name,
            DebugAction::class.java.name,
            DebugSelection::class.java.name,
            DebugTextProvider::class.java.name,
            DebugSelectionProvider::class.java.name
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    private fun ensureMenuExists(key: String) {
        if (menus[key] == null) {
            menus[key] = mutableListOf()
        }
    }

    private fun addOptionToMenu(menuKey: String, option: AnnotationWrapper) {
        if (menuKey.isEmpty()) {
            ensureMenuExists(DEBUG_GLOBAL_MENU)
            // add to default menu
            menus[DEBUG_GLOBAL_MENU]?.add(option)
        } else {
            ensureMenuExists(menuKey)
            // add to specific menu
            menus[menuKey]?.add(option)
        }
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        var result: Boolean? =
            roundEnvironment?.forEach(DebugBoolean::class.java) { element, annotation ->
            val name = element.simpleName.toString()
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val isVisible = annotation.isVisible
            val toggleOption = BooleanValue(title, name, defaultValue, isVisible)
            addOptionToMenu(menuKey, BooleanWrapper(toggleOption))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugInt::class.java) { element, annotation ->
            val name = element.simpleName.toString()
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val isVisible = annotation.isVisible
            val toggleOption = IntValue(title, name, defaultValue, isVisible)
            addOptionToMenu(menuKey, IntWrapper(toggleOption))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugDouble::class.java) { element, annotation ->
            val name = element.simpleName.toString()
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val isVisible = annotation.isVisible
            val toggleOption = DoubleValue(title, name, defaultValue, isVisible)
            addOptionToMenu(menuKey, DoubleWrapper(toggleOption))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugLong::class.java) { element, annotation ->
            val name = element.simpleName.toString()
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val isVisible = annotation.isVisible
            val toggleOption = LongValue(title, name, defaultValue, isVisible)
            addOptionToMenu(menuKey, LongWrapper(toggleOption))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugSelection::class.java) { element, annotation ->
            val name = element.simpleName.toString()
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultIndex
            val options = annotation.options
            val isVisible = annotation.isVisible

            // annotations can't include null, so we use -1 instead to represent the same state
            val correctedDefaultValue = if (defaultValue == -1) null else defaultValue

            val toggleOption = OptionSelection(title, name, options.toList(), correctedDefaultValue, isVisible)
            addOptionToMenu(menuKey, SelectionWrapper(toggleOption))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugAction::class.java, validKind = ElementKind.METHOD) { element, annotation ->
            val title = annotation.title
            val menuKey = annotation.menuKey
            val functionName = element.simpleName.toString()
            val parentClass = element.enclosingElement.toString()
            val isGlobal = parentClass.endsWith("Kt")
            val packageName = parentClass.split(".").dropLast(1).joinToString(".")
            val isVisible = annotation.isVisible
            addOptionToMenu(menuKey, ActionWrapper(title = title, functionName = functionName, parentClass = parentClass, isGlobal = isGlobal, packageName = packageName, isVisible = isVisible))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugTextProvider::class.java, validKind = ElementKind.METHOD) { element, annotation ->
            val menuKey = annotation.menuKey
            val functionName = element.simpleName.toString()
            val parentClass = element.enclosingElement.toString()
            val packageName = parentClass.split(".").dropLast(1).joinToString(".")
            val isVisible = annotation.isVisible
            addOptionToMenu(menuKey, TextValueWrapper(functionName = functionName, parentClass = parentClass, packageName = packageName, isVisible = isVisible))
        }
        if (result == false) return false

        result = roundEnvironment?.forEach(DebugSelectionProvider::class.java, validKind = ElementKind.METHOD) { element, annotation ->
            val menuKey = annotation.menuKey
            val functionName = element.simpleName.toString()
            val parentClass = element.enclosingElement.toString()
            val packageName = parentClass.split(".").dropLast(1).joinToString(".")
            val title = annotation.title
            val defaultValue = annotation.defaultIndex
            val isVisible = annotation.isVisible
            // annotations can't include null, so we use -1 instead to represent the same state
            val correctedDefaultValue = if (defaultValue == -1) null else defaultValue

            addOptionToMenu(menuKey, SelectionProviderWrapper(
                title = title, key = functionName, defaultIndex = correctedDefaultValue,
                functionName = functionName, parentClass = parentClass, packageName = packageName, isVisible = isVisible))
        }
        if (result == false) return false

        generateMenuClasses(menus)

        return true
    }

    private fun writeContents(fileContent: String, className: String) {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$className.kt")
        file.writeText(fileContent)
    }

    private fun generateMenuClasses(menus: HashMap<String, MutableList<AnnotationWrapper>>) {
        runBlocking {

            menus.keys.forEach { menuKey ->
                val options = menus[menuKey] ?: mutableListOf()
                val validMenuKey = menuKey.replace(" ", "_")
                val menuName = if (validMenuKey == DEBUG_GLOBAL_MENU) globalDebugKey else validMenuKey.capitalize()
                val menuBuilder = MenuClassBuilder(validMenuKey, menuName, options)
                val fileContents = menuBuilder.getContent()

                generateInitializationExtensions(menuBuilder)
                writeContents(fileContents, menuName)
            }
        }
    }

    private fun generateInitializationExtensions(menuBuilder: MenuClassBuilder) {
        val initializations = menuBuilder.generatedInitFunctions()
        initializations.second.forEach {
            if (initializationFunctions[it] == null) {
                initializationFunctions[it] = mutableSetOf()
            }

            initializationFunctions[it]?.add(initializations.first)
        }

        initializationFunctions.keys.forEach { parentPackage ->
            val parentName = parentPackage.split(".").last()
            val debugMenuNames = initializationFunctions[parentPackage] ?: mutableSetOf()
            if (debugMenuNames.isEmpty()) return@forEach
            val debugMenuImports = debugMenuNames.joinToString("\n") { "import com.steamclock.debugmenu.generated.$it" }
            val debugMenuInitCalls = debugMenuNames.map { debugMenuName -> "$debugMenuName.initialize(this)" }

            val contents = """
package com.steamclock.debugmenu.generated 

import $parentPackage
$debugMenuImports

fun $parentName.initDebugMenus() {
    ${debugMenuInitCalls.joinToString("\n    ")}
}
            """
            writeContents(contents, "${parentName}DebugMenuExtension")
        }
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}
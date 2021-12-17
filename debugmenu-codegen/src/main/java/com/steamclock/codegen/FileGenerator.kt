package com.steamclock.codegen

import com.google.auto.service.AutoService
import com.steamclock.debugmenu.DebugMenu.Companion.DEBUG_GLOBAL_MENU
import com.steamclock.debugmenu.BooleanValue
import com.steamclock.debugmenu.DoubleValue
import com.steamclock.debugmenu.IntValue
import com.steamclock.debugmenu.LongValue
import com.steamclock.debugmenu_annotation.*
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

internal sealed class AnnotationWrapper
internal data class BooleanWrapper(val toggle: BooleanValue): AnnotationWrapper()
internal data class IntWrapper(val intValue: IntValue): AnnotationWrapper()
internal data class DoubleWrapper(val doubleValue: DoubleValue): AnnotationWrapper()
internal data class LongWrapper(val longValue: LongValue): AnnotationWrapper()
internal data class ActionWrapper(val title: String, val functionName: String, val parentClass: String, val packageName: String, val isGlobal: Boolean): AnnotationWrapper()

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor() {
    private val globalDebugKey = "GlobalDebugMenu"
    private var menus = hashMapOf<String, MutableList<AnnotationWrapper>>()
    private val initializationFunctions = mutableMapOf<String, MutableSet<String>>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            DebugBoolean::class.java.name,
            DebugInt::class.java.name,
            DebugDouble::class.java.name,
            DebugLong::class.java.name,
            DebugAction::class.java.name)
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
        roundEnvironment?.getElementsAnnotatedWith(DebugBoolean::class.java)?.forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to classes, element: $it ")
                return false
            }

            val name = it.simpleName.toString()
            val annotation = it.getAnnotationsByType(DebugBoolean::class.java)[0]
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val toggleOption = BooleanValue(title, name, defaultValue)
            addOptionToMenu(menuKey, BooleanWrapper(toggleOption))
        }

        roundEnvironment?.getElementsAnnotatedWith(DebugInt::class.java)?.forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to classes, element: $it ")
                return false
            }

            val name = it.simpleName.toString()
            val annotation = it.getAnnotationsByType(DebugInt::class.java)[0]
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val toggleOption = IntValue(title, name, defaultValue)
            addOptionToMenu(menuKey, IntWrapper(toggleOption))
        }

        roundEnvironment?.getElementsAnnotatedWith(DebugDouble::class.java)?.forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to classes, element: $it ")
                return false
            }

            val name = it.simpleName.toString()
            val annotation = it.getAnnotationsByType(DebugDouble::class.java)[0]
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val toggleOption = DoubleValue(title, name, defaultValue)
            addOptionToMenu(menuKey, DoubleWrapper(toggleOption))
        }

        roundEnvironment?.getElementsAnnotatedWith(DebugLong::class.java)?.forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to classes, element: $it ")
                return false
            }

            val name = it.simpleName.toString()
            val annotation = it.getAnnotationsByType(DebugLong::class.java)[0]
            val title = annotation.title
            val menuKey = annotation.menuKey
            val defaultValue = annotation.defaultValue
            val toggleOption = LongValue(title, name, defaultValue)
            addOptionToMenu(menuKey, LongWrapper(toggleOption))
        }

        roundEnvironment?.getElementsAnnotatedWith(DebugAction::class.java)?.forEach {
            if (it.kind != ElementKind.METHOD) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to function, element: $it ")
                return false
            }

            val annotation = it.getAnnotationsByType(DebugAction::class.java)[0]
            val title = annotation.title
            val menuKey = annotation.menuKey
            val functionName = it.simpleName.toString()
            val parentClass = it.enclosingElement.toString()
            val isGlobal = parentClass.endsWith("Kt")
            val packageName = parentClass.split(".").dropLast(1).joinToString(".")
            addOptionToMenu(menuKey, ActionWrapper(title = title, functionName = functionName, parentClass = parentClass, isGlobal = isGlobal, packageName = packageName))
        }

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
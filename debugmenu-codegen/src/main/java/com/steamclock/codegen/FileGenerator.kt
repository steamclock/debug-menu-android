package com.steamclock.codegen

import com.google.auto.service.AutoService
import com.steamclock.debugmenu.Action
import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu.DebugMenu.Companion.DEBUG_GLOBAL_MENU
import com.steamclock.debugmenu.DebugOption
import com.steamclock.debugmenu.Toggle
import com.steamclock.debugmenu_annotation.DebugAction
import com.steamclock.debugmenu_annotation.DebugToggle
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

internal sealed class AnnotationWrapper
internal data class ToggleWrapper(val toggle: Toggle): AnnotationWrapper()
internal data class ActionWrapper(val title: String, val functionName: String, val parentClass: String, val packageName: String, val isGlobal: Boolean): AnnotationWrapper()

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor() {
    private val globalDebugKey = "GlobalDebugMenu"
    private var menus = hashMapOf<String, MutableList<AnnotationWrapper>>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(DebugToggle::class.java.name, DebugAction::class.java.name)
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
        roundEnvironment?.getElementsAnnotatedWith(DebugToggle::class.java)?.forEach {
            if (it.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to classes, element: $it ")
                return false
            }

            val name = it.simpleName.toString() // change this to camelCase
            val title = (it.getAnnotationsByType(DebugToggle::class.java)[0]).title
            val menuKey = (it.getAnnotationsByType(DebugToggle::class.java)[0]).menuKey
            val defaultValue = (it.getAnnotationsByType(DebugToggle::class.java)[0]).defaultValue
            val toggleOption = Toggle(title, name, defaultValue)
            addOptionToMenu(menuKey, ToggleWrapper(toggleOption))
        }

        roundEnvironment?.getElementsAnnotatedWith(DebugAction::class.java)?.forEach {
            if (it.kind != ElementKind.METHOD) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to function, element: $it ")
                return false
            }

            val name = it.simpleName.toString() // change this to camelCase
            val title = (it.getAnnotationsByType(DebugAction::class.java)[0]).title
            val menuKey = (it.getAnnotationsByType(DebugAction::class.java)[0]).menuKey
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
                val fileContents = MenuClassBuilder(validMenuKey, menuName, options).getContent()
                writeContents(fileContents, menuName)
            }
        }
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}
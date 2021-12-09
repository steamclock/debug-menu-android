package com.steamclock.codegen

import com.google.auto.service.AutoService
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

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor() {
    private val globalDebugKey = "GlobalDebugMenu"
    private var menus = hashMapOf<String, MutableList<DebugOption>>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(DebugToggle::class.java.name, DebugAction::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
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
            val pack = processingEnv.elementUtils.getPackageOf(it).toString()
            val toggleOption = Toggle(title, name, defaultValue)

            if (menuKey.isEmpty()) {
                if (menus[DEBUG_GLOBAL_MENU] == null) {
                    menus[DEBUG_GLOBAL_MENU] = mutableListOf()
                }
                // add to default menu
                menus[DEBUG_GLOBAL_MENU]?.add(toggleOption)
            } else {
                if (menus[menuKey] == null) {
                    menus[menuKey] = mutableListOf()
                }
                // add to specific menu
                menus[menuKey]?.add(toggleOption)
            }
        }

        generateMenuClasses(menus)

        return true
    }

    private fun writeContents(fileContent: String, className: String) {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$className.kt")
        file.writeText(fileContent)
    }

    private fun generateMenuClasses(menus: HashMap<String, MutableList<DebugOption>>) {
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
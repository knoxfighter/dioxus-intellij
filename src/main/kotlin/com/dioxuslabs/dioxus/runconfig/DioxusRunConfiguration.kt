package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.NlsActions
import javax.swing.Icon

class DioxusRunConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : LocatableConfigurationBase<DioxusRunConfigurationOptions>(project, factory, name) {
    var mode: String
        get() = options.getMode()
        set(mode) {options.setMode(mode)}

    override fun getOptions(): DioxusRunConfigurationOptions {
        return super.getOptions() as DioxusRunConfigurationOptions
    }

    var command: String
        get() = options.getCommand()
        set(value) = options.setCommand(value)

    var workingDirectory: String?
        get() = options.getWorkingDirectory()
        set(value) = options.setWorkingDirectory(value)

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return DioxusSettingsEditor(project)
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon("/icons/dioxus_color.svg", this::class.java)
    }

    override fun suggestedName(): @NlsActions.ActionText String {
        return "dx " + command.takeWhile { it != ' ' }
    }
    // Make new/copy start with a generated name
    override fun onNewConfigurationCreated() {
        super.onNewConfigurationCreated()
        setGeneratedName()
    }
    override fun onConfigurationCopied() {
        super.onConfigurationCopied()
        setGeneratedName()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        command.split(" ").firstOrNull()?.let {
            if (it == "serve") {
                return DioxusServeCommandLineState(environment, this)
            }
        }
        return DioxusCommandLineState(environment, this)
    }
}
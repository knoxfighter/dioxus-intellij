package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class DioxusConfigurationFactory(type: DioxusRunConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): @NonNls String {
        return DioxusRunConfigurationType.ID
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return DioxusRunConfiguration(project, this, "Dioxus")
    }

    override fun getIcon(): Icon {
        return IconLoader.getIcon("/icons/dioxus_color.svg", this::class.java)
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return DioxusRunConfigurationOptions::class.java
    }
}
package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class DioxusRunConfigurationType : ConfigurationTypeBase {
    companion object {
        const val ID = "DioxusRunConfiguration"
    }

    constructor() : super(ID, "Dioxus", "Run Dioxus commands", IconLoader.getIcon("/icons/dioxus.svg", DioxusRunConfigurationType::class.java)) {
        addFactory(DioxusConfigurationFactory(this))
    }

    override fun getIcon(): Icon? {
        return IconLoader.getIcon("/icons/dioxus_color.svg", this::class.java)
    }
}
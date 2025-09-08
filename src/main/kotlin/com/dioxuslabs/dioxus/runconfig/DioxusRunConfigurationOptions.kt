package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.configurations.LocatableRunConfigurationOptions

class DioxusRunConfigurationOptions : LocatableRunConfigurationOptions() {
    private val command = string("serve").provideDelegate(this, "command")
    private val workingDirectory = string().provideDelegate(this, "workingDirectory")
    private var mode = string("Debug").provideDelegate(this, "mode")


    fun getCommand(): String {
        return command.getValue(this).toString()
    }

    fun setCommand(value: String) {
        command.setValue(this, value)
    }

    fun getWorkingDirectory(): String? {
        return workingDirectory.getValue(this)
    }

    fun setWorkingDirectory(value: String?) {
        workingDirectory.setValue(this, value)
    }

    fun getMode(): String {
        return mode.getValue(this).toString()
    }

    fun setMode(value: String) {
        mode.setValue(this, value)
    }
}
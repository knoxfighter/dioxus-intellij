package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import kotlin.io.path.Path

class DioxusCommandLineState(environment: ExecutionEnvironment, private val parent: DioxusRunConfiguration) : CommandLineState(environment) {
    val command = parent.command
    val mode = parent.mode
    val workingDirectory = parent.workingDirectory
    val project = environment.project

    override fun startProcess(): ProcessHandler {
//        val commandLine = PtyCommandLine(listOf("dx"))
        val commandLine = GeneralCommandLine("dx")
            .withCharset(Charsets.UTF_8)
            .withWorkingDirectory((workingDirectory ?: project.basePath)?.let { Path(it) })

        commandLine.addParameters(command)

        if (mode == "Release") {
            commandLine.addParameter("-r")
        }

        val processHandler = ProcessHandlerFactory.getInstance()
            .createColoredProcessHandler(commandLine)

        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }
}
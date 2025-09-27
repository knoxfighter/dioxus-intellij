package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.VirtualFileManager
import org.rust.cargo.runconfig.filters.RsSourceCodeLinkFilter
import kotlin.io.path.Path

class DioxusCommandLineState(environment: ExecutionEnvironment, private val parent: DioxusRunConfiguration) : CommandLineState(environment) {
    val command = parent.command
    val mode = parent.mode
    val workingDirectory = parent.workingDirectory
    val project = environment.project

    init {
        val dir = workingDirectory ?: project.basePath
        val dir2 = dir?.let { Path(it) }
        val file = dir2?.let { VirtualFileManager.getInstance().findFileByNioPath(it) }

        val filter = RsSourceCodeLinkFilter(project, file)
        addConsoleFilters(filter)
    }

    override fun startProcess(): ProcessHandler {
        val dir = workingDirectory ?: project.basePath

//        val commandLine = PtyCommandLine(listOf("dx"))
        val commandLine = GeneralCommandLine("dx")
            .withCharset(Charsets.UTF_8)
            .withWorkingDirectory((dir)?.let { Path(it) })

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
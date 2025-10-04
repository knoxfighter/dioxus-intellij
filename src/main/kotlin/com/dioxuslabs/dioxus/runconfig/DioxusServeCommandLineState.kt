package com.dioxuslabs.dioxus.runconfig

import TerminalConsoleView
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.io.OutputStream

class DioxusServeCommandLineState(environment: ExecutionEnvironment, private val parent: DioxusRunConfiguration) :
    CommandLineState(environment) {
    val workingDirectory = parent.workingDirectory
    val project = environment.project
    val command = parent.command
    val mode = parent.mode

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val commandLine = GeneralCommandLine("dx")
            .withCharset(Charsets.UTF_8)
        commandLine.addParameters(ParametersList.parse(command).toList())

        if (mode == "Release") {
            commandLine.addParameter("-r")
        }

        val commandLineList = commandLine.getCommandLineList(null)

        val terminal = TerminalToolWindowManager
            .getInstance(project)
            // There is no proper api yet to do this, therefore i have to use this internal function
            .createNewSession(workingDirectory ?: project.basePath, "dx-serve", commandLineList, true, true)

        val handler = object : ProcessHandler() {
            override fun destroyProcessImpl() {
                terminal.sendCommandToExecute("\u0003")
                notifyProcessTerminated(0)
            }

            override fun detachProcessImpl() {
                notifyProcessDetached()
            }

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null
        }

        ProcessTerminatedListener.attach(handler)

        // Terminated is called when the terminal is closed, not when the server is stopped
        terminal.addTerminationCallback({
            handler.destroyProcess()
        }, terminal)

        val console = TerminalConsoleView(terminal)
        console.attachToProcess(handler)

        return DefaultExecutionResult(console, handler, *createActions(console, handler, executor))
    }

    override fun startProcess(): ProcessHandler {
        // non-implementation doesn't matter startProcess is never called, because we override execute()
        return object : ProcessHandler() {
            override fun destroyProcessImpl() {}

            override fun detachProcessImpl() {}

            override fun detachIsDefault(): Boolean = false

            override fun getProcessInput(): OutputStream? = null
        }
    }
}
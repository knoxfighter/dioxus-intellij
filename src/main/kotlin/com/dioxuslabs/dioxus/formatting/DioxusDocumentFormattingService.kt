package com.dioxuslabs.dioxus.formatting

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile
import org.rust.lang.core.psi.RsFile
import java.util.*

class DioxusDocumentFormattingService : AsyncDocumentFormattingService() {
    private val FEATURES: MutableSet<FormattingService.Feature?> = EnumSet.noneOf(FormattingService.Feature::class.java)

    override fun canFormat(file: PsiFile): Boolean = file is RsFile

    override fun getFeatures(): Set<FormattingService.Feature?> = FEATURES

    override fun getNotificationGroupId(): String = "Dioxus Formatting Notification Group"

    override fun getName(): @NlsSafe String = "Dioxus Formatter"

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask? {
        val commandLine = GeneralCommandLine("dx")
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        commandLine.addParameters(listOf("fmt", "--file", "-"))
        commandLine.withInput(request.ioFile)

        val handler = OSProcessHandler(commandLine.withCharset(Charsets.UTF_8))

        return object : FormattingTask {
            override fun run() {
                handler.addProcessListener(object : CapturingProcessAdapter() {
                    override fun processTerminated(event: ProcessEvent) {
                        if (event.exitCode == 0) {
                            request.onTextReady(output.stdout)
                        } else {
                            request.onError(
                                "Dioxus Formatter",
                                "dx fmt returned non-zero exit code: ${event.exitCode}\n${output.stderr}"
                            )
                        }
                    }
                })
                handler.startNotify()
            }

            override fun cancel(): Boolean {
                handler.destroyProcess()
                return true
            }

            override fun isRunUnderProgress(): Boolean = true
        }
    }
}
package com.dioxuslabs.dioxus.runconfig

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.emptyText
import com.intellij.util.text.nullize
import com.intellij.util.ui.FormBuilder
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.runconfig.command.CargoCommandConfiguration
import org.rust.cargo.util.RsCommandLineEditor
import java.nio.file.Paths
import javax.swing.JComponent
import javax.swing.JPanel

class DioxusSettingsEditor : SettingsEditor<DioxusRunConfiguration> {
    private var myPanel: JPanel
    private var command: RsCommandLineEditor
    private var workingDir = TextFieldWithBrowseButton()
    private var project: Project

    constructor(project: Project) {
        this.project = project

        command = RsCommandLineEditor(project, DioxusCommandCompletionProvider(project.cargoProjects) {
            CargoCommandConfiguration.findCargoProject(
                project,
                command.text,
                workingDir.text.nullize()?.let { Paths.get(it) })?.workspace
        })

        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        descriptor.title = "Select Working Directory"

        workingDir.addBrowseFolderListener(project, descriptor)

        myPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Command", command)
            .addLabeledComponent("Working directory", workingDir)
            .panel
    }

    fun getCurrentWorkingDir() {
        workingDir.text.ifEmpty { project.basePath ?: "" }
    }

    override fun resetEditorFrom(dioxusRunConfiguration: DioxusRunConfiguration) {
        command.text = dioxusRunConfiguration.command
        dioxusRunConfiguration.workingDirectory?.let { text ->
            workingDir.text = text
        }
        workingDir.emptyText.text = project.basePath ?: ""
    }

    override fun applyEditorTo(dioxusRunConfiguration: DioxusRunConfiguration) {
        dioxusRunConfiguration.command = command.text
        dioxusRunConfiguration.workingDirectory = workingDir.text.nullize()
    }

    override fun createEditor(): JComponent {
        return myPanel
    }
}
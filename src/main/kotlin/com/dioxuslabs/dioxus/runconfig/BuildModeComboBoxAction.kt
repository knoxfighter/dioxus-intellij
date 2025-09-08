package com.dioxuslabs.dioxus.runconfig

import com.intellij.execution.RunManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAware
import javax.swing.JComponent

class BuildModeComboBoxAction : ComboBoxAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return super.getActionUpdateThread()
    }

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        val group = DefaultActionGroup()
        group.add(ModeAction("Debug"))
        group.add(ModeAction("Release"))
        return group
    }

    private inner class ModeAction(private val mode: String) : AnAction(mode), DumbAware {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val runManager = RunManager.getInstance(project)
            val config = runManager.selectedConfiguration?.configuration as? DioxusRunConfiguration ?: return
            config.mode = mode
        }
    }

    override fun update(e: AnActionEvent) {
        val runManager = e.project?.let { RunManager.getInstance(it) }
        val selected = runManager?.selectedConfiguration?.configuration
        if (selected is DioxusRunConfiguration) {
            e.presentation.text = selected.mode // show persisted mode
            e.presentation.isEnabledAndVisible = true
        } else {
            e.presentation.text = "Mode"
            e.presentation.isEnabledAndVisible = false
        }
    }
}
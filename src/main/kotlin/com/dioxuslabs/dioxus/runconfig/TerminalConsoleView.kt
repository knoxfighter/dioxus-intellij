import com.intellij.execution.ExecutionBundle
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.ui.TerminalWidget
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import javax.swing.JPanel

class TerminalConsoleView(
    private val terminal: TerminalWidget
) : JPanel(BorderLayout()), ConsoleView {
    private var attachedProcessHandler: ProcessHandler? = null
    private val scrollPane = JBScrollPane(terminal.component)

    init {
        add(scrollPane, BorderLayout.CENTER)
        Disposer.register(this, terminal) // cleanup
    }

    override fun getComponent() = this
    override fun getPreferredFocusableComponent() = terminal.component

    // Minimal ConsoleView API – terminal itself handles rendering
    override fun print(text: String, contentType: ConsoleViewContentType) {
        terminal.sendCommandToExecute(text)
    }

    override fun clear() {
        terminal.sendCommandToExecute("\u001Bc")
    }
    override fun scrollTo(offset: Int) {
        scrollPane.verticalScrollBar.value = offset
    }
    override fun attachToProcess(processHandler: ProcessHandler) {
        attachedProcessHandler = processHandler
    }
    override fun setOutputPaused(value: Boolean) {}
    override fun isOutputPaused() = false
    override fun hasDeferredOutput() = false
    override fun performWhenNoDeferredOutput(runnable: Runnable) {}
    override fun setHelpId(helpId: String) {}
    override fun addMessageFilter(filter: Filter) {}
    override fun printHyperlink(hyperlinkText: String, info: com.intellij.execution.filters.HyperlinkInfo?) {}
    override fun getContentSize(): Int = 0
    override fun canPause(): Boolean = false
    override fun createConsoleActions(): Array<out AnAction?> = arrayOf(MyClearConsoleAction())
    override fun allowHeavyFilters() {}
    override fun dispose() {}

    // This action does not work, scrollPane.verticalScrollBar.maximum, is always te visible height and content/underlying widget is not available as of yet.
//    private inner class ScrollToEndAction: DumbAwareAction {
//        constructor() : super() {
//            val message = ActionsBundle.message("action.EditorConsoleScrollToTheEnd.text")
//            getTemplatePresentation().setDescription(message)
//            getTemplatePresentation().setText(message)
//            getTemplatePresentation().setIcon(AllIcons.RunConfigurations.Scroll_down)
//        }
//
//        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
//
//        override fun actionPerformed(e: AnActionEvent) {
//            scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
//        }
//    }

    private inner class MyClearConsoleAction() : DumbAwareAction(
        ExecutionBundle.messagePointer("clear.all.from.console.action.name"),
        ExecutionBundle.messagePointer("clear.all.from.console.action.description"), AllIcons.Actions.GC
    ) {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

        override fun actionPerformed(e: AnActionEvent) = clear()
    }
}

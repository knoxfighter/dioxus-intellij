import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.ui.TerminalWidget
import java.awt.BorderLayout
import javax.swing.JPanel

class TerminalConsoleView(
    private val terminal: TerminalWidget
) : JPanel(BorderLayout()), ConsoleView {
    private var attachedProcessHandler: ProcessHandler? = null

    init {
        add(terminal.component, BorderLayout.CENTER)
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
    override fun scrollTo(offset: Int) {}
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
    override fun createConsoleActions(): Array<out AnAction?> = emptyArray()
    override fun allowHeavyFilters() {}
    override fun dispose() {}
}

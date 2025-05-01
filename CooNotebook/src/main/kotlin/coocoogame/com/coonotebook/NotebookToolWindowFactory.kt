package coocoogame.com.coonotebook


import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class NotebookToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val notebookPanel = NotebookPanel(project)
        val content = ContentFactory.getInstance().createContent(notebookPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
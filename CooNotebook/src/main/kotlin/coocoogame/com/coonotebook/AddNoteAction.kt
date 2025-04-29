package coocoogame.com.coonotebook

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class AddNoteAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = NoteEditorDialog(project, null)
        if (dialog.showAndGet()) {
            // The note will be added through the NotebookPanel's listener
        }
    }
}
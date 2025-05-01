package coocoogame.com.coonotebook


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class AddNoteAction : AnAction("Add Note", "Create a new note", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // 获取当前所有可用分类
        val categories = CategoryManager.getInstance().loadCategories()

        // 创建并显示笔记编辑对话框
        val dialog = NoteEditorDialog(project, null, categories)
        if (dialog.showAndGet()) {
            // 获取创建的笔记并处理（实际保存操作应在调用处处理）
            val newNote = dialog.getNote()
            // 通常这里会触发事件或回调，实际保存逻辑在NotebookPanel中
        }
    }

    override fun update(e: AnActionEvent) {
        // 仅在项目打开时启用该动作
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}
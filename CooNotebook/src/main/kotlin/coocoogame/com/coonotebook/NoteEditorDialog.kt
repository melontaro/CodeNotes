package coocoogame.com.coonotebook

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import java.util.*
import javax.swing.*


class NoteEditorDialog(
    project: Project,
    private val existingNote: Note?
) : DialogWrapper(project) {
    private val titleField = JTextField()

    // 获取 C# 文件类型
    var csharpFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("cs")
    private val contentArea = EditorTextField(
        EditorFactory.getInstance().createDocument(""),
        project,
        csharpFileType,
        false,
        false
    );

    init {
        init()
        title = if (existingNote == null) "Add New Note" else "Edit Note"

        // Set existing values if editing
        existingNote?.let {
            titleField.text = it.title
            contentArea.text = it.content
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(5, 5))

        // Title field
        val titlePanel = JPanel(BorderLayout(5, 5))
        titlePanel.add(JLabel("Title:"), BorderLayout.WEST)
        titlePanel.add(titleField, BorderLayout.CENTER)

        // Content area
       // contentArea.rows = 50
      //  contentArea.columns = 180

        val scrollPane = JScrollPane(contentArea)

        panel.add(titlePanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    fun getNote(): Note {
        return if (existingNote == null) {
            Note(
                id = UUID.randomUUID().toString(),
                title = titleField.text,
                content = contentArea.text
            )
        } else {
            existingNote.copy(
                title = titleField.text,
                content = contentArea.text,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
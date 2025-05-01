package coocoogame.com.coonotebook

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EditorTextField
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.*
import javax.swing.*


class NoteEditorDialog(
    project: Project,
    private val existingNote: Note?,
    private var categories: List<String> // 改为可变var
) : DialogWrapper(project) {
    private val titleField = JTextField()
    // 获取 C# 文件类型
    var csharpFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("cs")
    private val contentArea = EditorTextField(
        EditorFactory.getInstance().createDocument(""),
        project,
        csharpFileType,
        false,
        true

    );

    private val categoryCombo = ComboBox<String>().apply {
        addItem("Uncategorized")
        categories.forEach { addItem(it) }
        selectedItem = existingNote?.category ?: "Uncategorized"
    }

    init {
        init()
        contentArea.minimumSize = Dimension(400, 200); // 宽度400px，高度200px
        title = if (existingNote == null) "Add New Note" else "Edit Note"
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

        // Category combo
        val categoryPanel = JPanel(BorderLayout(5, 5))
        categoryPanel.add(JLabel("Category:"), BorderLayout.WEST)
        categoryPanel.add(categoryCombo, BorderLayout.CENTER)

        // Content area
       // contentArea.rows = 10
      //  contentArea.columns = 40
        val scrollPane = JScrollPane(contentArea)

        // Layout
        val northPanel = JPanel(BorderLayout(5, 5))
        northPanel.add(titlePanel, BorderLayout.NORTH)
        northPanel.add(categoryPanel, BorderLayout.SOUTH)

        panel.add(northPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }
    fun updateCategories(newCategories: List<String>) {
        categories = newCategories
        val currentSelection = categoryCombo.selectedItem
        categoryCombo.removeAllItems()
        categoryCombo.addItem("Uncategorized")
        categories.forEach { categoryCombo.addItem(it) }
        categoryCombo.selectedItem = currentSelection ?: existingNote?.category ?: "Uncategorized"
    }
    fun getNote(): Note {
        val category = if (categoryCombo.selectedItem == "Uncategorized") {
            null
        } else {
            categoryCombo.selectedItem as String
        }

        return if (existingNote == null) {
            Note(
                id = UUID.randomUUID().toString(),
                title = titleField.text,
                content = contentArea.text,
                category = category
            )
        } else {
            existingNote.copy(
                title = titleField.text,
                content = contentArea.text,
                category = category,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
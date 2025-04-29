package coocoogame.com.coonotebook

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.AbstractTableModel

class NotebookPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val noteList = mutableListOf<Note>()
    private val tableModel = NoteTableModel()
    private val table = JBTable(tableModel)
    val comboBox = ComboBox<String>()//(arrayOf("All", "Today", "This Week", "This Month", "This Year"))

    init {
        // 创建动作组
        val actionGroup = DefaultActionGroup().apply {
            // 添加ComboBox组件

            add(object : AnAction("Add", "Add new note", AllIcons.General.Add) {
                override fun actionPerformed(e: AnActionEvent) = addNote()
            })
            add(object : AnAction("Remove", "Remove note", AllIcons.General.Remove) {
                override fun actionPerformed(e: AnActionEvent) = removeNote()
            })
            add(object : AnAction("Edit", "Edit note", AllIcons.Actions.Edit) {
                override fun actionPerformed(e: AnActionEvent) = editNote()
            })
            addSeparator()
            add(object : AnAction("Export", "Export to JSON", AllIcons.ToolbarDecorator.Export) {
                override fun actionPerformed(e: AnActionEvent) {
                    ExportNotesAction().actionPerformed(e)
                }
            })
        }

        // 创建工具栏
        val toolbar = ActionManager.getInstance()
            .createActionToolbar("NotebookToolbar", actionGroup, true)
            .apply {
                setTargetComponent(this@NotebookPanel)
            }

       // val comboBox = ComboBox<String>(arrayOf("All", "Today", "This Week", "This Month", "This Year"))
        // 设置布局
        val panel = JPanel(BorderLayout()).apply {
            add(toolbar.component, BorderLayout.NORTH)
            add(comboBox, BorderLayout.SOUTH)
            add(JBScrollPane(table), BorderLayout.CENTER)
        }

        add(panel, BorderLayout.CENTER)

        // 启用双击编辑
        table.setDefaultEditor(Object::class.java, null)
        table.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) {
                    editNote()
                }
            }
        })
    }

    fun loadNotes(notes: List<Note>) {
        noteList.clear()
        noteList.addAll(notes)
        tableModel.fireTableDataChanged()
       for (note in notes) {
           comboBox.addItem(note.title)
       }
    }

    private fun addNote() {
        val dialog = NoteEditorDialog(project, null)
        if (dialog.showAndGet()) {
            val newNote = dialog.getNote()
            noteList.add(newNote)
            tableModel.fireTableDataChanged()
            saveNotes()
        }
    }

    private fun editNote() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0) {
            val noteToEdit = noteList[selectedRow]
            val dialog = NoteEditorDialog(project, noteToEdit)
            if (dialog.showAndGet()) {
                val updatedNote = dialog.getNote()
                noteList[selectedRow] = updatedNote
                tableModel.fireTableDataChanged()
                saveNotes()
            }
        }
    }

    private fun removeNote() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0) {
            noteList.removeAt(selectedRow)
            tableModel.fireTableDataChanged()
            saveNotes()
        }
    }

    private fun saveNotes() {
        NoteStorageService.getInstance().saveNotes(noteList)
    }

    private inner class NoteTableModel : AbstractTableModel() {
        private val columnNames = arrayOf("Title","Content")// arrayOf("Title", "Last Updated")

        override fun getRowCount() = noteList.size
        override fun getColumnCount() = columnNames.size
        override fun getColumnName(column: Int) = columnNames[column]

        override fun getValueAt(row: Int, column: Int): Any {
            return when (column) {
                0 -> noteList[row].title
                1 -> noteList[row].content // Date(noteList[row].updatedAt).toString()
                else -> ""
            }
        }
    }
}
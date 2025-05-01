package coocoogame.com.coonotebook

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.CollectionListModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Window
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel


class NotebookPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {
    private var currentFilterCategory: String? = null
    private val noteListAll = mutableListOf<Note>()
    private val noteListCurrent = mutableListOf<Note>()
    private val tableModel = NoteTableModel()
    private val table = JBTable(tableModel)
    // 获取 C# 文件类型
    var csharpFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("cs")
    private val contentTextArea = EditorTextField(
        EditorFactory.getInstance().createDocument(""),
        project,
        csharpFileType,
        false,
        false
    );
    private val saveButton = JButton("Save").apply {
        addActionListener { saveCurrentNote() }
    }
    private var currentSelectedNote: Note? = null
    private val categoryListModel = CollectionListModel<String>()
    private val categoryList = JBList(categoryListModel)
var comboBox= ComboBox<String>().apply {
      //  addItem("All")
      //  addItem("Today")
      //  addItem("This Week")
      //  addItem("This Month")
       // addItem("This Year")
        addActionListener {
            when (selectedItem) {
               // "Today" -> filterNotes(FilterPeriod.TODAY)
              //  "This Week" -> filterNotes(FilterPeriod.WEEK)
                //"This Month" -> filterNotes(FilterPeriod.MONTH)
              //  "This Year" -> filterNotes(FilterPeriod.YEAR)
              //  else -> loadData()

            }
            categoryList.setSelectedValue(selectedItem,true)
            applyCategoryFilter()
        }
    }
    init {
        layout = BorderLayout()

        // 主分割面板 (左侧笔记列表，右侧笔记内容和分类)
        val mainSplitter = OnePixelSplitter(false, 0.3f)

        // 左侧笔记列表
        val notePanel = JPanel(BorderLayout())
        notePanel.add(createToolbar().component, BorderLayout.NORTH)
        notePanel.add(JBScrollPane(table), BorderLayout.CENTER)
        notePanel.add(comboBox, BorderLayout.SOUTH)

        // 右侧分割面板 (上部笔记内容，下部分类)
        val rightSplitter = OnePixelSplitter(true, 0.7f)

        // 笔记内容面板
        val contentPanel = JPanel(BorderLayout())
        contentTextArea.apply {
           // lineWrap = true
            //wrapStyleWord = true


            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.isControlDown && e.keyCode == KeyEvent.VK_S) {
                        saveCurrentNote()
                    }
                }
            })
        }


        val buttonPanel = JPanel(BorderLayout()).apply {
            add(saveButton, BorderLayout.EAST)
        }
        contentPanel.add(JBScrollPane(contentTextArea), BorderLayout.CENTER)
        contentPanel.add(buttonPanel, BorderLayout.SOUTH)

        /*
        // 分类面板
        val categoryPanel = JPanel(BorderLayout())
        val categoryToolbar = ToolbarDecorator.createDecorator(categoryList)
            .setAddAction { addCategory() }
            .setRemoveAction { removeCategory() }
            .createPanel()
        categoryPanel.add(categoryToolbar, BorderLayout.CENTER)
*/
        rightSplitter.firstComponent = contentPanel
        //rightSplitter.secondComponent = categoryPanel

        mainSplitter.firstComponent = notePanel
        mainSplitter.secondComponent = rightSplitter

        add(mainSplitter, BorderLayout.CENTER)

        // 初始化数据
        loadData()

        for (item in categoryListModel.items) {
            comboBox.addItem(item)
        }
        setupListeners()
        // 注册分类更新监听器
        CategoryNotifier.addListener { newCategories ->
            ApplicationManager.getApplication().invokeLater {
                categoryListModel.replaceAll(newCategories)
                refreshCategoryDependentComponents()
            }
        }
        categoryList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                applyCategoryFilter()
            }
        }
    }
    private fun applyCategoryFilter() {
        val selectedCategory = categoryList.selectedValue?.toString()
        currentFilterCategory = if (selectedCategory == "Uncategorized") {
            null
        } else {
            selectedCategory
        }

        refreshNoteListDisplay()
    }
    private fun refreshNoteListDisplay() {
        val allNotes = NoteStorageService.getInstance().loadNotes()
        val filteredNotes = if (currentFilterCategory != null) {
            allNotes.filter { it.category == currentFilterCategory }
        } else {
            allNotes
        }

        noteListCurrent.clear()
        noteListCurrent.addAll(filteredNotes)
        tableModel.fireTableDataChanged()

        // 更新状态显示
        if (filteredNotes.isEmpty()) {
            val message = if (currentFilterCategory != null) {
                "No notes found in category: $currentFilterCategory"
            } else {
                "No notes available"
            }
           // statusLabel.text = message
        } else {
            updateStatusText()
        }
    }
    private fun updateStatusText() {
        val statusText = when {
            currentFilterCategory != null -> "Showing notes in category: $currentFilterCategory"
            else -> "Showing all notes"
        }
        // 假设有一个statusLabel组件显示状态
       // statusLabel.text = statusText
    }

    private fun createToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup().apply {
            add(object : AnAction("Add", "Add new note", AllIcons.General.Add) {
                override fun actionPerformed(e: AnActionEvent) = addNote()
            })
            add(object : AnAction("Edit", "Edit note", AllIcons.Actions.Edit) {
                override fun actionPerformed(e: AnActionEvent) = editNote()
            })
            add(object : AnAction("Remove", "Remove note", AllIcons.General.Remove) {
                override fun actionPerformed(e: AnActionEvent) = removeNote()
            })
            addSeparator()
            add(object : AnAction("Export", "Export to JSON", AllIcons.ToolbarDecorator.Export) {
                override fun actionPerformed(e: AnActionEvent) {
                    ExportNotesAction().actionPerformed(e)
                }
            })
            add(object : AnAction("Manage Categories", "Manage note categories", AllIcons.Actions.GroupBy) {
                override fun actionPerformed(e: AnActionEvent) {
                    showCategoryManager()
                }
            })
            add(object : AnAction("Show All", "Show all notes", AllIcons.Actions.ShowAsTree) {
                override fun actionPerformed(e: AnActionEvent) {
                    resetCategoryFilter()
                }
            })

          //  add(ManageCategoriesAction())
        }

        return ActionManager.getInstance()
            .createActionToolbar("NotebookToolbar", actionGroup, true)
            .apply { setTargetComponent(this@NotebookPanel) }
    }

    private fun resetCategoryFilter() {
        categoryList.clearSelection()
        currentFilterCategory = null
        refreshNoteListDisplay()
    }

    private fun loadData() {
        noteListAll.addAll(NoteStorageService.getInstance().loadNotes())
        tableModel.fireTableDataChanged()
        categoryListModel.addAll(0,CategoryManager.getInstance().loadCategories())
    }

    private fun setupListeners() {
        table.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                showSelectedNoteContent()
            }
        }
    }

    private fun showSelectedNoteContent() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0) {
            currentSelectedNote = noteListCurrent[selectedRow]
            contentTextArea.text = currentSelectedNote?.content ?: ""
            contentTextArea.isEnabled = true
            saveButton.isEnabled = true
        } else {
            currentSelectedNote = null
            contentTextArea.text = ""
            contentTextArea.isEnabled = false
            saveButton.isEnabled = false
        }
    }

    private fun saveCurrentNote() {
        currentSelectedNote?.let { note ->
            val selectedRow = noteListCurrent.indexOf(note)
            if (selectedRow >= 0) {
                note.content = contentTextArea.text
                note.updatedAt = System.currentTimeMillis()
                tableModel.fireTableRowsUpdated(selectedRow, selectedRow)
                saveNotes()
                Messages.showInfoMessage(project, "Note saved successfully", "Success")
            }
        }
    }

    private fun addNote() {
        // 获取当前所有分类作为参数
        val categories = categoryListModel.items
        val dialog = NoteEditorDialog(project, null, categories)
        if (dialog.showAndGet()) {
            val newNote = dialog.getNote()
            noteListAll.add(newNote)
            tableModel.fireTableDataChanged()
            saveNotes()
            table.selectionModel.setSelectionInterval(noteListAll.size - 1, noteListAll.size - 1)
        }
    }
    private fun editNote() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0) {
            val noteToEdit = noteListAll[selectedRow]
            // 传入当前分类列表
            val dialog = NoteEditorDialog(project, noteToEdit, categoryListModel.items)
            if (dialog.showAndGet()) {
                val updatedNote = dialog.getNote()
                noteListAll[selectedRow] = updatedNote
                tableModel.fireTableRowsUpdated(selectedRow, selectedRow)
                saveNotes()
            }
        }
    }

    private fun removeNote() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0) {
            noteListAll.removeAt(selectedRow)
            tableModel.fireTableDataChanged()
            saveNotes()
            showSelectedNoteContent()
        }
    }

    private fun saveNotes() {
        NoteStorageService.getInstance().saveNotes(noteListAll)
    }

    private fun addCategory() {
        val name = JOptionPane.showInputDialog("Enter category name:")
        if (!name.isNullOrBlank() && !categoryListModel.contains(name)) {
            categoryListModel.add(name)
            CategoryManager.getInstance().saveCategories(categoryListModel.items)
            // 刷新所有相关UI组件
            refreshCategoryDependentComponents()
        }
    }
    private fun refreshCategoryDependentComponents() {
        // 1. 刷新右侧分类列表
        categoryList.repaint()

        // 2. 刷新表格中的分类显示
        tableModel.fireTableDataChanged()

        // 3. 如果笔记编辑对话框打开着，也需要刷新
        refreshOpenNoteEditors()
        // 4. 刷新分类下拉框
        comboBox.removeAllItems()
        for (item in categoryListModel.items) {
            comboBox.addItem(item)
        }
    }

    private fun refreshOpenNoteEditors() {
        // 获取所有打开的NoteEditorDialog实例并刷新它们的分类列表
        Window.getWindows().forEach { window ->
            if (window is JDialog && window.title.startsWith("Edit Note")) {
                val contentPane = window.contentPane
                if (contentPane is JPanel) {
                    val comboBox = findCategoryComboBox(contentPane)
                    comboBox?.let { cb ->
                        val currentSelection = cb.selectedItem
                        cb.removeAllItems()
                        cb.addItem("Uncategorized")
                        categoryListModel.items.forEach { cb.addItem(it) }
                        cb.selectedItem = currentSelection ?: "Uncategorized"
                    }
                }
            }
        }
    }

    private fun findCategoryComboBox(component: Container): JComboBox<String>? {
        for (comp in component.components) {
            if (comp is JComboBox<*>) {
                @Suppress("UNCHECKED_CAST")
                return comp as? JComboBox<String>
            }
            if (comp is Container) {
                val found = findCategoryComboBox(comp)
                if (found != null) return found
            }
        }
        return null
    }

    private fun removeCategory() {
        val selected = categoryList.selectedValue
        if (selected != null) {
            // 确认对话框
            val confirm = Messages.showYesNoDialog(
                "Delete category '$selected'? Notes in this category will become uncategorized.",
                "Confirm Delete",
                Messages.getQuestionIcon()
            )

            if (confirm == Messages.YES) {
                // 更新笔记数据
                NoteStorageService.getInstance().getNotesByCategory(selected).forEach {
                    it.category = null
                }

                // 从分类列表中移除
                categoryListModel.remove(selected)
                CategoryManager.getInstance().saveCategories(categoryListModel.items)

                // 刷新显示
                refreshNoteListDisplay()
            }
        }
    }

    private fun showCategoryManager() {
        CategoryManagerDialog(project).show()
    }

    private inner class NoteTableModel : AbstractTableModel() {
        private val columnNames = arrayOf("Title", "Category")// arrayOf("Title", "Category", "Last Updated")

        override fun getRowCount() = noteListCurrent.size
        override fun getColumnCount() = columnNames.size
        override fun getColumnName(column: Int) = columnNames[column]

        override fun getValueAt(row: Int, column: Int): Any {
            return when (column) {
                0 -> noteListCurrent[row].title
                1 -> noteListCurrent[row].category ?: "Uncategorized"
               // 2 -> Date(noteList[row].updatedAt).toString()
                else -> ""
            }
        }
    }

    override fun disable() {
        // 移除监听器防止内存泄漏
        CategoryNotifier.removeListener { /* ... */ }
        super.disable()
    }
}
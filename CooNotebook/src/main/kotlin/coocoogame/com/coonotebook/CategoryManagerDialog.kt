package coocoogame.com.coonotebook


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.nio.file.Paths
import javax.swing.*

class CategoryManagerDialog(project: Project) : DialogWrapper(project) {
    private val categoryListModel = CollectionListModel<String>()
    private val categoryList = JBList(categoryListModel)
    private val mapper = jacksonObjectMapper()
    private val currentProject = project

    init {
        title = "Category Manager"
        init()
        loadCategories()
    }

    override fun createCenterPanel(): JComponent {
        // 创建主面板
        val panel = JPanel(BorderLayout())

        // 创建工具栏和列表
        val toolbar = createActionToolbar()
        val scrollPane = JBScrollPane(categoryList)

        // 设置布局
        panel.add(toolbar.component, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)
        panel.preferredSize = Dimension(400, 300)

        return panel
    }

    private fun createActionToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup().apply {
            add(object : AnAction("Add", "Add category", AllIcons.General.Add) {
                override fun actionPerformed(e: AnActionEvent) = addCategory()
            })
            add(object : AnAction("Remove", "Remove category", AllIcons.General.Remove) {
                override fun actionPerformed(e: AnActionEvent) = removeCategory()
            })
            add(object : AnAction("Edit", "Edit category", AllIcons.Actions.Edit) {
                override fun actionPerformed(e: AnActionEvent) = editCategory()
            })
            addSeparator()
            add(object : AnAction("Export", "Export categories", AllIcons.ToolbarDecorator.Export) {
                override fun actionPerformed(e: AnActionEvent) = exportCategories()
            })
        }

        return ActionManager.getInstance()
            .createActionToolbar("CategoryManagerToolbar", actionGroup, true)
            .apply {
                // 修复警告：明确设置目标组件
                setTargetComponent(categoryList)
            }
    }
    private fun addCategory() {
        val name = JOptionPane.showInputDialog("Enter category name:")
        if (!name.isNullOrBlank() && !categoryListModel.contains(name)) {
            categoryListModel.add(name)
            saveCategories()
        }
    }

    private fun removeCategory() {
        val selected = categoryList.selectedValue
        if (selected != null) {
            categoryListModel.remove(selected)
            saveCategories()
        }
    }

    private fun editCategory() {
        val selected = categoryList.selectedValue
        if (selected != null) {
            val newName = JOptionPane.showInputDialog("Edit category name:", selected)
            if (!newName.isNullOrBlank()) {
                val index = categoryList.selectedIndex
                categoryListModel.setElementAt(newName, index)
                saveCategories()
            }
        }
    }

    private fun exportCategories() {
        val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
            .withFileFilter { file -> file.extension.equals("json", ignoreCase = true) }
            .withTitle("Export Categories")

        FileChooser.chooseFile(descriptor, currentProject, null) { virtualFile ->
            try {
                val exportPath = if (virtualFile.isDirectory) {
                    Paths.get(virtualFile.path, "categories_${System.currentTimeMillis()}.json").toFile()
                } else {
                    if (!virtualFile.path.endsWith(".json", true)) {
                        File("${virtualFile.path}.json")
                    } else {
                        File(virtualFile.path)
                    }
                }

                // 检查并创建父目录
                exportPath.parentFile?.mkdirs()

                // 检查写入权限
                if (exportPath.parentFile?.canWrite() != true) {
                    Messages.showErrorDialog(
                        currentProject,
                        "No write permission for directory: ${exportPath.parent}",
                        "Export Failed"
                    )
                    return@chooseFile
                }

                // 写入文件
                mapper.writerWithDefaultPrettyPrinter().writeValue(exportPath, categoryListModel.items)
                Messages.showInfoMessage(
                    currentProject,
                    "Categories exported successfully to:\n${exportPath.absolutePath}",
                    "Export Successful"
                )
            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    currentProject,
                    "Failed to export categories: ${ex.message}",
                    "Export Failed"
                )
            }
        }
    }

    private fun loadCategories() {
        val categoriesFile = getCategoriesFile()
        if (categoriesFile.exists()) {
            try {
                val categories: List<String> = mapper.readValue(
                    categoriesFile,
                    mapper.typeFactory.constructCollectionType(List::class.java, String::class.java)
                )
                categoryListModel.replaceAll(categories)
            } catch (ex: Exception) {
                Messages.showErrorDialog("Failed to load categories: ${ex.message}", "Load Failed")
            }
        }
    }
    private fun saveCategories() {
        val categoriesToSave = categoryListModel.items
        CategoryManager.getInstance().saveCategories(categoriesToSave)

        // 通知所有监听器分类已更新
        CategoryNotifier.notifyCategoriesUpdated(categoriesToSave)
    }
    /*
    private fun saveCategories() {
        val categoriesFile = getCategoriesFile()
        try {
            categoriesFile.parentFile?.mkdirs()
            mapper.writerWithDefaultPrettyPrinter().writeValue(categoriesFile, categoryListModel.items)
        } catch (ex: Exception) {
            Messages.showErrorDialog("Failed to save categories: ${ex.message}", "Save Failed")
        }
    }
*/
    private fun getCategoriesFile(): File {
        return Paths.get(
            System.getProperty("user.home"),
            ".intellij-notebook",
            "categories.json"
        ).toFile()
    }
}
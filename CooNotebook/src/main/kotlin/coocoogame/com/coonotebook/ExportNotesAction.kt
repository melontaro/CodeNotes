package coocoogame.com.coonotebook

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JOptionPane

class ExportNotesAction : AnAction("Export Notes to JSON", "Export all notes to a JSON file", null) {
    private val mapper = jacksonObjectMapper()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val notes = NoteStorageService.getInstance().loadNotes()

        if (notes.isEmpty()) {
            Messages.showInfoMessage(project, "No notes to export", "Export Notes")
            return
        }

        // 创建文件保存描述符
        val descriptor = object : FileChooserDescriptor(
            false,  // 不允许多选
            true,   // 允许选择目录
            true,   // 允许选择文件
            false,  // 对于保存操作应为false
            false,  // 不允许选择隐藏文件
            false   // 不显示文件系统根
        ) {
            // 使用正确的签名覆盖方法
            override fun isFileSelectable(file: VirtualFile?): Boolean {
                if (file == null) return false
                // 允许选择目录或.json文件或不存在的文件
                return file.isDirectory ||
                        file.extension?.equals("json", ignoreCase = true) == true ||
                        !file.exists()
            }

            override fun validateSelectedFiles(files: Array<out VirtualFile>) {
                if (files.isEmpty()) {
                    throw IllegalArgumentException("Please select a location")
                }
                val file = files[0]

                // 如果选择的是文件且已存在，确认覆盖
                if (!file.isDirectory && file.exists()) {
                    val result = JOptionPane.showConfirmDialog(
                        null,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION
                    )
                    if (result != JOptionPane.YES_OPTION) {
                        throw IllegalArgumentException("Operation cancelled")
                    }
                }
            }
        }.apply {
            title = "Export Notes"
            description = "Select location to save JSON file"
            isHideIgnored = true
            isForcedToUseIdeaFileChooser = true
        }

        // 默认文件名
        val defaultFileName = "notes_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.json"

        // 显示文件选择器
        FileChooser.chooseFile(descriptor, project, null) { virtualFile ->
            try {
                // 确定最终保存路径
                val exportPath = when {
                    virtualFile.isDirectory ->
                        Paths.get(virtualFile.path, defaultFileName).toFile()
                    !virtualFile.path.endsWith(".json", ignoreCase = true) ->
                        File("${virtualFile.path}.json")
                    else ->
                        File(virtualFile.path)
                }

                // 检查并创建父目录
                exportPath.parentFile?.mkdirs()

                // 检查写入权限
                if (exportPath.parentFile?.canWrite() != true) {
                    Messages.showErrorDialog(
                        project,
                        "Cannot write to directory: ${exportPath.parent}",
                        "Export Failed"
                    )
                    return@chooseFile
                }

                // 写入文件
                mapper.writerWithDefaultPrettyPrinter().writeValue(exportPath, notes)
                Messages.showInfoMessage(
                    project,
                    "Notes exported successfully to:\n${exportPath.absolutePath}",
                    "Export Successful"
                )

            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    project,
                    "Failed to export notes:\n${ex.message}",
                    "Export Failed"
                )
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}
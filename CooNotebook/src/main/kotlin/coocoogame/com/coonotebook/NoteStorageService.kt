package coocoogame.com.coonotebook

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.nio.file.Paths
@Service
class NoteStorageService { private val logger = Logger.getInstance(NoteStorageService::class.java)
    private val mapper = jacksonObjectMapper()
    private val storageFile: File by lazy {
        val configPath = Paths.get(
            System.getProperty("user.home"),
            ".intellij-notebook",
            "notes.json"
        ).toFile()

        configPath.parentFile.mkdirs()
        if (!configPath.exists()) {
            configPath.createNewFile()
            configPath.writeText("[]")
        }
        configPath
    }

    fun saveNotes(notes: List<Note>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, notes)
            } catch (e: Exception) {
                logger.error("Failed to save notes", e)
            }
        }
    }

    fun loadNotes(): List<Note> {
        return try {
            mapper.readValue(storageFile)
        } catch (e: Exception) {
            logger.error("Failed to load notes", e)
            emptyList()
        }
    }

    companion object {
        fun getInstance(): NoteStorageService {
            return ApplicationManager.getApplication().getService(NoteStorageService::class.java)
        }
    }
}
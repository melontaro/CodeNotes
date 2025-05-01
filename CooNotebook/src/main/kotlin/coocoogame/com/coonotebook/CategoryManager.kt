package coocoogame.com.coonotebook

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.nio.file.Paths

@Service
class CategoryManager {
    private val logger = Logger.getInstance(CategoryManager::class.java)
    private val mapper = jacksonObjectMapper()
    private val storageFile: File by lazy {
        val configPath = Paths.get(
            System.getProperty("user.home"),
            ".intellij-notebook",
            "categories.json"
        ).toFile()

        configPath.parentFile.mkdirs()
        if (!configPath.exists()) {
            configPath.createNewFile()
            configPath.writeText("[]")
        }
        configPath
    }

    fun saveCategories(categories: List<String>) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, categories)
            } catch (e: Exception) {
                logger.error("Failed to save categories", e)
            }
        }
    }

    fun loadCategories(): List<String> {
        return try {
            mapper.readValue(storageFile)
        } catch (e: Exception) {
            logger.error("Failed to load categories", e)
            emptyList()
        }
    }

    companion object {
        fun getInstance(): CategoryManager {
            return ApplicationManager.getApplication().getService(CategoryManager::class.java)
        }
    }
}
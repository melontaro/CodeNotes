package coocoogame.com.coonotebook

import java.util.*

data class Note(
    val id: String,
    var title: String,
    var content: String,
    var category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
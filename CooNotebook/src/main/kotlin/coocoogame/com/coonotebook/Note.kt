package coocoogame.com.coonotebook

 data class Note(
    val id: String,
    var title: String,
    var content: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
 )
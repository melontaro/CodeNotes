package coocoogame.com.coonotebook

object CategoryNotifier {
    private val listeners = mutableListOf<(List<String>) -> Unit>()

    fun addListener(listener: (List<String>) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (List<String>) -> Unit) {
        listeners.remove(listener)
    }

    fun notifyCategoriesUpdated(categories: List<String>) {
        listeners.forEach { it(categories) }
    }
}
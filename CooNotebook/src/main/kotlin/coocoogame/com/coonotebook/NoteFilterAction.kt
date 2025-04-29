package coocoogame.com.coonotebook
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.ui.ComboBox;
import javax.swing.JComponent

class FilterComboBoxAction : CustomComponentAction {
    private val comboBox = ComboBox<String>(arrayOf("All", "Today", "This Week", "This Month", "This Year"))

    init {
        comboBox.addActionListener {
            when (comboBox.selectedItem) {
                //"Today" -> filterNotes(FilterPeriod.TODAY)
                // 其他情况...
            }
        }
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return comboBox
    }
}

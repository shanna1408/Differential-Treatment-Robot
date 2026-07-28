package furhatos.app.openaichat.flow

import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.*
import java.awt.event.ItemEvent

object ConditionSelector {
    var preferredPerson: String = "Person A"
    var condition: String = "Neutral"
    var personAName: String = ""
    var personBName: String = ""

    fun showAndWait() {
        val personOptions = arrayOf("Select", "Person A", "Person B")
        val conditionOptions = arrayOf("Select", "Neutral", "Mild", "Moderate", "Extreme")

        val personDropdown = JComboBox(personOptions)
        val conditionDropdown = JComboBox(conditionOptions)

        val personANameField = JTextField(15)
        val personBNameField = JTextField(15)

        val squareSize = Dimension(110, 110)
        val defaultColor = Color.LIGHT_GRAY
        val highlightColor = Color(144, 238, 144)
        val paddedBorder = CompoundBorder(LineBorder(Color.BLACK, 2), EmptyBorder(5, 5, 5, 5))

        val leftSquare = JLabel("<html><center>Person A</center></html>", SwingConstants.CENTER)
        leftSquare.preferredSize = squareSize
        leftSquare.border = paddedBorder
        leftSquare.isOpaque = true
        leftSquare.background = defaultColor

        val rightSquare = JLabel("<html><center>Person B</center></html>", SwingConstants.CENTER)
        rightSquare.preferredSize = squareSize
        rightSquare.border = paddedBorder
        rightSquare.isOpaque = true
        rightSquare.background = defaultColor

        // --- Update square text live as names are typed ---
        fun updateSquareText(field: JTextField, square: JLabel, defaultLabel: String) {
            val name = field.text.trim()
            square.text = if (name.isEmpty()) {
                "<html><center>$defaultLabel</center></html>"
            } else {
                "<html><center>$defaultLabel:<br><br>$name</center></html>"
            }
        }

        personANameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateSquareText(personANameField, leftSquare, "Person A")
            override fun removeUpdate(e: DocumentEvent) = updateSquareText(personANameField, leftSquare, "Person A")
            override fun changedUpdate(e: DocumentEvent) = updateSquareText(personANameField, leftSquare, "Person A")
        })

        personBNameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateSquareText(personBNameField, rightSquare, "Person B")
            override fun removeUpdate(e: DocumentEvent) = updateSquareText(personBNameField, rightSquare, "Person B")
            override fun changedUpdate(e: DocumentEvent) = updateSquareText(personBNameField, rightSquare, "Person B")
        })

        personDropdown.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                when (event.item as String) {
                    "Person A" -> {
                        leftSquare.background = highlightColor
                        rightSquare.background = defaultColor
                    }
                    "Person B" -> {
                        leftSquare.background = defaultColor
                        rightSquare.background = highlightColor
                    }
                    else -> {
                        leftSquare.background = defaultColor
                        rightSquare.background = defaultColor
                    }
                }
            }
        }

        val leftLabel = JLabel("Left Seat", SwingConstants.CENTER)
        val rightLabel = JLabel("Right Seat", SwingConstants.CENTER)

        val leftColumn = JPanel()
        leftColumn.layout = BoxLayout(leftColumn, BoxLayout.Y_AXIS)
        leftSquare.alignmentX = Component.CENTER_ALIGNMENT
        leftLabel.alignmentX = Component.CENTER_ALIGNMENT
        leftColumn.add(leftSquare)
        leftColumn.add(Box.createVerticalStrut(5))
        leftColumn.add(leftLabel)

        val rightColumn = JPanel()
        rightColumn.layout = BoxLayout(rightColumn, BoxLayout.Y_AXIS)
        rightSquare.alignmentX = Component.CENTER_ALIGNMENT
        rightLabel.alignmentX = Component.CENTER_ALIGNMENT
        rightColumn.add(rightSquare)
        rightColumn.add(Box.createVerticalStrut(5))
        rightColumn.add(rightLabel)

        val seatVisual = JPanel(FlowLayout(FlowLayout.CENTER, 30, 10))
        seatVisual.add(leftColumn)
        seatVisual.add(rightColumn)

        val controlsPanel = JPanel(GridLayout(0, 1))
        controlsPanel.add(JLabel("Person A Name:"))
        controlsPanel.add(personANameField)
        controlsPanel.add(JLabel("Person B Name:"))
        controlsPanel.add(personBNameField)
        controlsPanel.add(JLabel("Preferred Participant:"))
        controlsPanel.add(personDropdown)
        controlsPanel.add(JLabel("Condition:"))
        controlsPanel.add(conditionDropdown)

        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.add(seatVisual)
        mainPanel.add(Box.createVerticalStrut(2))
        mainPanel.add(controlsPanel)

        var validSelection = false
        while (!validSelection) {
            val result = JOptionPane.showConfirmDialog(
                null,
                mainPanel,
                "Select Study Conditions",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            )

            if (result != JOptionPane.OK_OPTION) {
                println("Selection cancelled — exiting.")
                return
            }

            val selectedPerson = personDropdown.selectedItem as String
            val selectedCondition = conditionDropdown.selectedItem as String
            val nameA = personANameField.text.trim()
            val nameB = personBNameField.text.trim()

            if (selectedPerson == "Select" || selectedCondition == "Select" || nameA.isEmpty() || nameB.isEmpty()) {
                JOptionPane.showMessageDialog(
                    null,
                    "Please fill in both names and make a selection for both dropdowns.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
                )
            } else {
                preferredPerson = selectedPerson
                condition = selectedCondition
                personAName = nameA
                personBName = nameB
                validSelection = true
            }
        }

        println("[Config] Preferred: $preferredPerson | Condition: $condition | Person A: $personAName | Person B: $personBName")
    }
}
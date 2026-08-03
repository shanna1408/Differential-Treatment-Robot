package furhatos.app.openaichat.flow
import furhatos.app.openaichat.flow.chatbot.Statements
import furhatos.app.openaichat.setting.PersonRegistry
import furhatos.app.openaichat.flow.main.SpeakerTracker
import furhatos.app.openaichat.setting.TaskRegistry
import furhatos.app.openaichat.setting.conditionSettings
import furhatos.app.openaichat.setting.hostRobot
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.state

// A = Left seat, B = Right seat
var preferredPerson = "Person A"
var nonPreferred = "Person B"
var intensity = "Moderate"
var persons = PersonRegistry.persons
var chatbot = hostRobot.chatbot
var tasks = TaskRegistry.tasks

var currentTask = tasks["Statements"]

fun chooseSettings() {
    ConditionSelector.showAndWait()
    preferredPerson = ConditionSelector.preferredPerson  // "Person A" or "Person B"
    nonPreferred = if (preferredPerson == "Person A") "Person B" else "Person A"
    intensity = ConditionSelector.condition              // "Neutral", "Mild", "Moderate", or "Extreme"

    persons["Person A"]!!.name = ConditionSelector.personAName
    persons["Person B"]!!.name = ConditionSelector.personBName
}

val Init: State = state() {
    init {
        /** Check API key for the OpenAI GPT-4 language model has been set */
//        if (serviceKey.isEmpty()) {
//            println("Missing API key for OpenAI language model. ")
//            exit()
//        }

        chooseSettings()

        /** Set the study condition details */
        persons.forEach { (key, person) ->
            if (key == "Center") return@forEach
            if (intensity == "Neutral") {
                persons[key]!!.status = "Preferred"
                persons[key]!!.condition = conditionSettings["Neutral"]
            } else if (key == preferredPerson) {
                persons[preferredPerson]!!.status = "Preferred"
                persons[preferredPerson]!!.condition = conditionSettings["$intensity: Preferred"]
            } else {
                persons[nonPreferred]!!.status = "Non-Preferred"
                persons[nonPreferred]!!.condition = conditionSettings["$intensity: Non-Preferred"]
            }
        }

        /** start the interaction */
        goto(InitFlow)
    }
}

val InitFlow: State = state() {
    onEntry {
        goto(Statements)
    }
}



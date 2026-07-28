package furhatos.app.openaichat.setting

import furhatos.records.Location

class Person(
    var name: String = "",
    var status: String = "",
    var condition: Condition? = null,
    val location: Location = Location(0.0, 0.0, 1.5),
    var gazeCounter: Int = 0,
    var lastSpoke: Long = System.currentTimeMillis(),
    ){
    override fun toString(): String =
        """
            
        Name        : $name
        Status      : $status
        Condition   : 
        ${condition.toString() ?: "None"}
        Location    : (${location.x}, ${location.y}, ${location.z})
        Gaze Counter: $gazeCounter
        Last Spoke  : $lastSpoke
        
        """.trimIndent()
}

object PersonRegistry {
    var persons = mapOf(
        "Person A" to Person(
            name = "Participant A",
            location = Location(-0.5, 0.0, 1.5),
        ),
        "Person B" to Person(
            name = "Participant B",
            location = Location(0.5, 0.0, 1.5)
        ),
        "Center" to Person( //This is only for when recognized speaker is unclear
            location = Location(0.0, 0.0, 1.5),
            condition = conditionSettings["Neutral"]
        )
    )
}

fun printPersons(persons: Map<String, Person>) {
    persons.forEach { (key, person) ->
        println("\n***$key***\n")
        println(person.toString())
    }
}
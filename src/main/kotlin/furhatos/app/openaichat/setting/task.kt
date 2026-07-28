package furhatos.app.openaichat.setting

import furhatos.flow.kotlin.State
import furhatos.app.openaichat.flow.chatbot.Taboo
import furhatos.app.openaichat.flow.chatbot.Riddle
import furhatos.app.openaichat.flow.chatbot.Island
import kotlin.String

class Task(
    val name: String,
    val introduction: String,
    val robot_instructions: String,
    val state: State,
)

val tasks = listOf(
    Task(
        name = "Taboo",
        introduction = "Let’s play a game together. Shanna will give you a word, and a list of banned words. " +
                "You both need to work together to describe the word to me so I can guess it. Let’s do as many as we can " +
                "before time runs out! Give me my first hint!",
        robot_instructions = "\nTask:\nYou will play Taboo with two humans and always be the guesser. They will give you hints. Never assume you have guessed correctly, or ask about the next word, unless the humans have explicitly confirmed your guess is correct. If the humans have confirmed you guessed correctly, ask for a hint for a new word.",
        state = Taboo,
    ),
    Task(
        name = "Riddle",
        introduction = "",
        robot_instructions = "",
        state = Riddle,
    ),
    Task(
        name = "Deserted Island",
        introduction = "",
        robot_instructions = "",
        state = Island,
    )
)
package furhatos.app.openaichat.setting

import furhatos.flow.kotlin.State
import furhatos.app.openaichat.flow.chatbot.Taboo
import furhatos.app.openaichat.flow.chatbot.Island
import furhatos.app.openaichat.flow.chatbot.Statements
import kotlin.String

class Task(
    val introduction: String,
    val robotInstructions: String,
    val state: State,
)

object TaskRegistry {
    var tasks = mapOf(
        "Taboo" to Task(
            introduction = "Let’s play a game together. Shanna will give you a word, and a list of banned words. " +
                    "You both need to work together to describe the word to me so I can guess it. Let’s do as many as we can " +
                    "before time runs out! Give me my first hint!",
            robotInstructions = "\nTask:\nYou will play Taboo with two humans and always be the guesser. They will give you hints. Never assume you have guessed correctly, or ask about the next word, unless the humans have explicitly confirmed your guess is correct. If the humans have confirmed you guessed correctly, ask for a hint for a new word.",
            state = Taboo,
        ),
        "Statements" to Task(
            introduction = "For this task, I'll introduce a series of statements for us to discuss. " +
                    "There are no correct answers, so feel free to share your thoughts, agree or disagree, and explain your reasoning. " +
                    "The goal is for us to have an open discussion and explore the different viewpoints each statement brings up. " +
                    "I will give us three minutes to discuss each statement. ",
            robotInstructions = "\nTask:\nYou are facilitating a discussion between two humans, where they will debate their opinions on a set of statements." +
                    "Acknowledge the person's point by paraphrasing it back to them in a way that adds value or expresses that the point makes sense. Never pre-emptively" +
                    "Offer to move on to the next statement for any reason.",
            state = Statements,
        ),
        "Island" to Task(
            introduction = "",
            robotInstructions = "",
            state = Island,
        )
    )
}

//"\nTask:\nYou are facilitating a discussion between two humans, where they will debate their opinions on a set of statements." +
//"Depending on which feels more natural in context, either: " +
//"Acknowledge the person's point by paraphrasing it back to them" +
//"Or ask them to elaborate or explain further on something they said.",
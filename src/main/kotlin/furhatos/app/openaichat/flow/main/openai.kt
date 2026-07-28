package furhatos.app.openaichat.flow.main

import io.github.sashirestela.openai.SimpleOpenAI
import io.github.sashirestela.openai.domain.chat.ChatMessage
import io.github.sashirestela.openai.domain.chat.ChatRequest

/** Open AI API Key **/
val serviceKey = ""

val openAI = SimpleOpenAI.builder()
    .apiKey(serviceKey)
    .build();

// --- Speaker-tagged conversation log ---
data class Turn(val speaker: String, val text: String)

object ConversationLog {
    private val turns = mutableListOf<Turn>()

    fun addUserTurn(speaker: String, text: String) {
        turns.add(Turn(speaker, text))
        if (turns.size > 20) turns.removeAt(0)
    }

    fun addRobotTurn(text: String) {
        turns.add(Turn("Eve", text))
        if (turns.size > 20) turns.removeAt(0)
    }

    fun getRecentTurns(n: Int = 20): List<Turn> = turns.takeLast(n)
}


class OpenAIChatbot(val systemPrompt: String) {

    fun getResponse(text: String = "", speaker: String = "Preferred", extraInstructions: String = ""): String {

        ConversationLog.addUserTurn(speaker, text)

        val prompt = "$systemPrompt\n$extraInstructions\n\n"
//        println(prompt)
//        println("[OpenAI] Generating response as persona for: $speaker")

        val messages = mutableListOf<ChatMessage>(ChatMessage.SystemMessage.of(prompt))

        val recentTurns = ConversationLog.getRecentTurns(10)
//        println(recentTurns)
        for (turn in recentTurns) {
            if (turn.speaker == "Eve") {
                messages.add(ChatMessage.AssistantMessage.of(turn.text))
            } else {
                messages.add(ChatMessage.UserMessage.of("${turn.speaker}: ${turn.text}"))
            }
        }

        val request = ChatRequest.builder()
            .model("gpt-4.1-nano")
            .messages(messages)
            .stop(listOf("Person A:", "Person B:", "Furhat:", "Eve:"))

        var futureChat = openAI.chatCompletions().create(request.build())
        var chatResponse = futureChat.join()
        var robotResponse = chatResponse.firstContent().toString()
        ConversationLog.addRobotTurn(robotResponse ?: "")
        println("OpenAI Eve: ${robotResponse}")
        return robotResponse
    }
}
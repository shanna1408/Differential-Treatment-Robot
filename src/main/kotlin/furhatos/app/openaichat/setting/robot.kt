package furhatos.app.openaichat.setting

import furhatos.app.openaichat.flow.main.OpenAIChatbot
import furhatos.app.openaichat.flow.chatbot
import furhatos.flow.kotlin.FlowControlRunner
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.voice.AzureVoice
import furhatos.flow.kotlin.voice.Voice

class Robot(
    val name: String,
    val desc: String,
    val face: List<String>,
    val mask: String = "adult",
    val voice: Voice,
    var lastHeadTurn: Long = System.currentTimeMillis(),
    var gazeTarget: String = "Center"
) {
    var prompt = "You are $name, the $desc. You should speak in a conversational style. "+
            "Never say more than two sentences. You should directly answer any directed questions, and can make up facts if necessary, " +
            "but if the question is off-topic, always direct the conversation back to the task after answering." +
            "Do NOT prefix your reply with your name or any label like 'Eve:'."

    /** The prompt for the openAI language model **/
    var chatbot = OpenAIChatbot(prompt)
}

fun FlowControlRunner.activate(robot: Robot, prompt: String = "") {
    furhat.voice = robot.voice

    for (face in robot.face) {
        if (furhat.faces[robot.mask]?.contains(face)!!) {
            furhat.character = face
            break
        }
    }
    println("Activating host")
    if (prompt!="") {
        chatbot = OpenAIChatbot(prompt)
        println("Updated chatbot prompt")
    }
}

val hostRobot = Robot(
    name = "Eve",
    desc = "Meeting Facilitor",
    face = listOf("Zhen", "default"),
    voice = AzureVoice("AriaNeural")
)

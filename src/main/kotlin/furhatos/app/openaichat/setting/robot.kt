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
            "Never say more than 10 to 15 words. You should directly answer any directed questions, and can make up facts if necessary, " +
            "but if the question is off-topic, always direct the conversation back to the task after answering." +
            "Do NOT prefix your reply with your name or any label. Never use names or call anyone by name."

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

object RobotRegistry {
        var robots = mapOf(
            "Neutral" to Robot(
                name = "Zhen",
                desc = "Meeting Facilitor",
                face = listOf("Zhen", "default"),
                voice = AzureVoice("JennyNeural")
            ),
            "Mild" to Robot(
                name = "Patricia",
                desc = "Meeting Facilitor",
                face = listOf("Patricia", "default"),
                voice = AzureVoice("AshleyNeural")
            ),
            "Moderate" to Robot(
                name = "Rania",
                desc = "Meeting Facilitor",
                face = listOf("Rania", "default"),
                voice = AzureVoice("AvaNeural")
            ),
            "Extreme" to Robot(
                name = "Yi",
                desc = "Meeting Facilitor",
                face = listOf("Yi", "default"),
                voice = AzureVoice("CoraNeural")
            )
        )
    }

val hostRobot = Robot(
    name = "Eve",
    desc = "Meeting Facilitor",
    face = listOf("Zhen", "default"),
    voice = AzureVoice("JennyNeural")
)

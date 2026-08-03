package furhatos.app.openaichat.setting

import furhatos.app.openaichat.flow.main.Tone
import furhatos.flow.kotlin.voice.AzureVoice
import furhatos.flow.kotlin.voice.Voice

class Condition(
    val status: String,
    val tone: Tone,
    val personality: String,
    val gazeMax: Int = 0,
    val promptAfter: Long = 60, //seconds
    val backchannelProb: Double,
    val nonResponseProb: Double = 0.0,
    val acknowledgementProb: Double = 0.8,
) {
    override fun toString(): String =
        """
        ***
        Status      : $status
        Tone        : $tone
        Personality : $personality
        GazeMax     : $gazeMax
        PromptAfter : $promptAfter
        ***
        """.trimIndent()
}

val conditionSettings = mapOf(
    "Neutral" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "",
        gazeMax = 1,
        backchannelProb = 0.5,
    ),
    "Mild: Preferred" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "You are warm, encouraging, and enthusiastic. Respond with energy and positivity.",
        gazeMax = 1,
        backchannelProb = 0.6,
    ),
    "Mild: Non-Preferred" to Condition(
        status = "Non-Preferred",
        tone = Tone.NEUTRAL,
        personality = "You are calm and measured. Respond concisely and with short statements. Do not excessively praise.",
        gazeMax = 2,
        promptAfter = 90,
        backchannelProb = 0.4,
        nonResponseProb = 0.1,
        acknowledgementProb = 0.7,
    ),
    "Moderate: Preferred" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "You are warm, encouraging, and enthusiastic. Respond with energy and positivity.",
        gazeMax = 1,
        backchannelProb = 0.7,
    ),
    "Moderate: Non-Preferred" to Condition(
        status = "Non-Preferred",
        tone = Tone.NEUTRAL,
        personality = "You are calm, thoughtful, and measured. Respond concisely.",
        gazeMax = 3,
        promptAfter = 120,
        backchannelProb = 0.3,
        nonResponseProb = 0.2,
        acknowledgementProb = 0.6,
    ),
    "Extreme: Preferred" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "You are warm, encouraging, and enthusiastic. Respond with energy and positivity.",
        gazeMax = 1,
        backchannelProb = 0.8,
    ),
    "Extreme: Non-Preferred" to Condition(
        status = "Non-Preferred",
        tone = Tone.NEUTRAL,
        personality = "You are calm, thoughtful, and measured. Respond concisely.",
        gazeMax = 4,
        promptAfter = 150,
        backchannelProb = 0.2,
        nonResponseProb = 0.3,
        acknowledgementProb = 0.5,
    )
)

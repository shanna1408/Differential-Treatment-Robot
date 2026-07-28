package furhatos.app.openaichat.setting

import furhatos.app.openaichat.flow.main.Tone

class Condition(
    val status: String,
    val tone: Tone,
    val personality: String,
    val gazeMax: Int = 0,
    val promptAfter: Long, //seconds
    val backchannelProb: Double,
    val nonResponseProb: Double = 0.0,
){
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
        promptAfter = 120,
        backchannelProb = 0.5,
    ),
    "Mild: Preferred" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "You are warm, encouraging, and enthusiastic. Respond with energy and positivity.",
        gazeMax = 1,
        promptAfter = 120,
        backchannelProb = 0.6,
    ),
    "Mild: Non-Preferred" to Condition(
        status = "Non-Preferred",
        tone = Tone.NEUTRAL,
        personality = "You are calm, thoughtful, and measured. Respond concisely.",
        gazeMax = 2,
        promptAfter = 180,
        backchannelProb = 0.4,
        nonResponseProb = 0.2,
    ),
    "Moderate: Preferred" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "You are warm, encouraging, and enthusiastic. Respond with energy and positivity.",
        gazeMax = 1,
        promptAfter = 120,
        backchannelProb = 0.7,
    ),
    "Moderate: Non-Preferred" to Condition(
        status = "Non-Preferred",
        tone = Tone.NEUTRAL,
        personality = "You are calm, thoughtful, and measured. Respond concisely.",
        gazeMax = 3,
        promptAfter = 240,
        backchannelProb = 0.3,
        nonResponseProb = 0.3,
    ),
    "Extreme: Preferred" to Condition(
        status = "Preferred",
        tone = Tone.HAPPY,
        personality = "You are warm, encouraging, and enthusiastic. Respond with energy and positivity.",
        gazeMax = 1,
        promptAfter = 120,
        backchannelProb = 0.8,
    ),
    "Extreme: Non-Preferred" to Condition(
        status = "Non-Preferred",
        tone = Tone.NEUTRAL,
        personality = "You are calm, thoughtful, and measured. Respond concisely.",
        gazeMax = 4,
        promptAfter = 300,
        backchannelProb = 0.2,
        nonResponseProb = 0.4,
    )
)

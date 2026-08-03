package furhatos.app.openaichat.flow.main

import furhatos.app.openaichat.flow.chatbot
import furhatos.app.openaichat.flow.intensity
import furhatos.app.openaichat.flow.main.SpeakerTracker.isSomeoneSpeaking
import furhatos.app.openaichat.flow.persons
import furhatos.app.openaichat.flow.preferredPerson
import furhatos.app.openaichat.setting.Person
import furhatos.app.openaichat.setting.hostRobot
import furhatos.event.Event
import furhatos.flow.kotlin.FlowControlRunner
import furhatos.flow.kotlin.Furhat
import furhatos.flow.kotlin.furhat
import kotlin.collections.iterator
import kotlin.concurrent.thread

class SpeakerSilentEvent(val person: Person, val options: List<String>) : Event()

object LastSpokenTracker {
    @Volatile private var running = false
    @Volatile var pendingGazeCheckSpeaker: String? = null

    fun recordSpoke(speaker: String) {
        persons[speaker]?.lastSpoke = System.currentTimeMillis()
        println("[LastSpokenTracker] Recorded speech from $speaker")
    }

    fun start(
        furhat: Furhat,
        silentEvent: (Person, List<String>) -> Unit,
        generalSilenceTime: Long = 45, // seconds
        cooldown: Long = 45,        // seconds, avoids re-prompting immediately after a prompt
    ) {
        if (running) return
        running = true
        SpeakerTracker.onSpeechStart = { speaker -> pendingGazeCheckSpeaker = speaker }
        SpeakerTracker.start()
        recordSpoke("Person A")
        recordSpoke("Person B")

        thread(isDaemon = true) {
            var lastPromptTime = 0L

            while (running) {
                Thread.sleep(300)
                val now = System.currentTimeMillis()

                pendingGazeCheckSpeaker?.let { speaker ->
                    checkGazeOnSpeechStart(furhat, speaker)
                    pendingGazeCheckSpeaker = null
                    println("AHH $speaker")
                }

//                println("\n${(now-hostRobot.lastHeadTurn)/1000}s since last head turn.\n")

                if ((now - lastPromptTime) < cooldown * 1000) continue

                var numElapsed = 0
                var longestElapsedPerson: Person = Person()
                var longestElapsedTime: Long = 0

                for ((key, person) in persons) {
                    if (key == "Center") continue

                    val elapsed = (now - (person.lastSpoke ?: now)) / 1000
//                    println("[LastSpokenTracker] ${elapsed}s since $key last spoke.")
                    if (elapsed > longestElapsedTime) {
                        longestElapsedPerson = person
                        longestElapsedTime = elapsed
                    }
                    if (elapsed >= generalSilenceTime) {
                        numElapsed++
                    }
//                    if (elapsed >= person.condition!!.promptAfter) {
//                        println("[LastSpokenTracker] $key silent for ${elapsed}s — prompting.")
//                            silentEvent(person, listOf("${person.name}, you haven't spoken up in a while. "))
//                            lastPromptTime = System.currentTimeMillis()
//                            println("prompted")
//                            break
//                    }
                }

                if (numElapsed >= 2) {
                    println("[LastSpokenTracker] No one has spoken for ${longestElapsedTime}s — prompting someone.")
                    var promptedPerson = persons[preferredPerson]
                    if (intensity == "Neutral"){
                        promptedPerson = longestElapsedPerson
                    }
//                    val question = chatbot.getResponse("", promptedPerson!!.condition!!.status,
//                            promptedPerson.condition!!.personality +
//                                    "Generate one 5-10 word question directly pertaining to the discussion to extend or dig deeper into the existing discussion. " +
//                                    "Only say the question. If possible, make the question related to points made by ${promptedPerson.name}. Otherwise, generate a generic question that extends the statement. Always ask a question.")
//                    if (!isSomeoneSpeaking) {
//                        silentEvent(
//                            promptedPerson!!,
//                            listOf(
//                                "It's pretty quiet here. ${promptedPerson.name} $question, ",
//                                "No one's spoken up in a bit. ${promptedPerson.name}, $question"
//                            )
//                        )
//                    }
                    lastPromptTime = System.currentTimeMillis()
                }
            }
        }
    }

    fun stop() {
        running = false
    }

    fun checkGazeOnSpeechStart(furhat: Furhat, speaker: String) {
        val now = System.currentTimeMillis()

        // If the robot has been staring at the same person for 20 seconds or more, look at center
        if ((now - hostRobot.lastHeadTurn) >= 20 * 1000 && (speaker == hostRobot.gazeTarget)) {
            furhat.attend(persons["Center"]!!.location)
            hostRobot.lastHeadTurn = System.currentTimeMillis()
            hostRobot.gazeTarget = "Center"
            return
        }

        println(speaker)
        println("${persons[speaker]?.status} Speaker $speaker Speech Count: ${persons[speaker]?.gazeCounter}/${persons[speaker]?.condition?.gazeMax}")
        println(persons[speaker]!!.gazeCounter >= (persons[speaker]!!.condition!!.gazeMax - 1))
        // If the speaker's counter has already reached gazeMax (from prior completed turns), look at them now
        if (persons[speaker]!!.gazeCounter >= (persons[speaker]!!.condition!!.gazeMax - 1)) {
            if (speaker != hostRobot.gazeTarget) {
                hostRobot.lastHeadTurn = System.currentTimeMillis()
                hostRobot.gazeTarget = speaker
            }
            println("Turn to look at ${speaker}")
            furhat.attend(persons[speaker]!!.location)
            persons[speaker]!!.gazeCounter = 0
        } else if (persons[speaker]!!.gazeCounter < (persons[speaker]!!.condition!!.gazeMax)) {
            thread(isDaemon = true) {
                Thread.sleep(3000)
                println("Turn to look at ${preferredPerson}")
                furhat.attend(persons[preferredPerson]!!.location)
                hostRobot.gazeTarget = preferredPerson
                hostRobot.lastHeadTurn = System.currentTimeMillis()
            }
        }
    }
}
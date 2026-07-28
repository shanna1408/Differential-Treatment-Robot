package furhatos.app.openaichat.flow.main

import furhatos.app.openaichat.flow.intensity
import furhatos.app.openaichat.flow.persons
import furhatos.app.openaichat.flow.preferredPerson
import furhatos.app.openaichat.setting.Person
import furhatos.app.openaichat.setting.hostRobot
import furhatos.event.Event
import furhatos.flow.kotlin.furhat
import kotlin.collections.iterator
import kotlin.concurrent.thread

class SpeakerSilentEvent(val person: Person, val options: List<String>) : Event()

object LastSpokenTracker {
    @Volatile private var running = false

    fun recordSpoke(speaker: String) {
        persons[speaker]?.lastSpoke = System.currentTimeMillis()
        println("[LastSpokenTracker] Recorded speech from $speaker")
    }

    fun start(
        silentEvent: (Person, List<String>) -> Unit,
        generalSilenceTime: Long = 30, // seconds
        cooldown: Long = 30        // seconds, avoids re-prompting immediately after a prompt
    ) {
        if (running) return
        running = true

        thread(isDaemon = true) {
            var lastPromptTime = 0L

            while (running) {
                Thread.sleep(1000)
                val now = System.currentTimeMillis()

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
                    if (elapsed >= person.condition!!.promptAfter) {
                        println("[LastSpokenTracker] $key silent for ${elapsed}s — prompting.")
                        silentEvent(person, listOf("${person.name}, you haven't spoken up in a while. Do you have any thoughts?"))
                        lastPromptTime = System.currentTimeMillis()
                        println("prompted")
                        break
                    }
                }

                if (numElapsed >= 2) {
                    println("[LastSpokenTracker] No one has spoken for ${numElapsed}s — prompting someone.")
                    var promptedPerson = persons[preferredPerson]
                    if (intensity == "Neutral"){
                        promptedPerson = longestElapsedPerson
                    }
                    silentEvent(promptedPerson!!, listOf("It's pretty quiet here. ${promptedPerson.name}, do you have anything to add?", "No one's spoken up in a bit. ${promptedPerson.name}, what do you think?"))
                    lastPromptTime = System.currentTimeMillis()
                }
            }
        }
    }

    fun stop() {
        running = false
    }
}
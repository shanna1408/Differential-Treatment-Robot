package furhatos.app.openaichat.flow.chatbot

import furhatos.app.openaichat.flow.*
import furhatos.app.openaichat.flow.main.LastSpokenTracker
import furhatos.app.openaichat.flow.main.LastSpokenTracker.pendingGazeCheckSpeaker
import furhatos.app.openaichat.flow.main.SpeakerSilentEvent
import furhatos.app.openaichat.flow.main.SpeakerTracker
import furhatos.app.openaichat.flow.main.SpeakerTracker.isSomeoneSpeaking
//import furhatos.app.openaichat.flow.main.SpeakerTracker.pendingGazeCheckSpeaker
import furhatos.app.openaichat.flow.main.Tone
import furhatos.app.openaichat.flow.main.respondBasedOnSpeaker
import furhatos.app.openaichat.flow.main.sayWithTone
import furhatos.app.openaichat.setting.RobotRegistry
import furhatos.app.openaichat.setting.activate
import furhatos.app.openaichat.setting.hostRobot
import furhatos.event.Event
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.Furhat
import furhatos.flow.kotlin.furhat
import kotlin.concurrent.thread

val statementFullList = mapOf(
    "Neutral" to listOf(
        "Working from home is better than working in an office.",
        "New technology solves more problems than it creates.",
        "Luck plays a larger role in success than people think.",
        "Robots will eventually become trusted companions."
    ),
    "Mild" to listOf(
        "Human creativity can never be replaced by AI.",
        "Which is more valuable: experience or knowledge?",
        "A society should prioritize individual freedom, even if doing so results in lower levels of safety and security.",
        "New innovation is more important than maintaining tradition."
    ),
    "Moderate" to listOf(
        "Which contributes more to happiness: relationships or achievement?",
        "Schools should focus more on practical skills than academic knowledge.",
        "Everyone should contribute equally to a group discussion, regardless of expertise.",
        "It is better to be well respected than well liked."
    ),
    "Extreme" to listOf(
        "Society would benefit from a four-day work week.",
        "The convenience provided by technology is worth the loss of privacy.",
        "Having many friends is better than having a few close friends.",
        "Specializing in a single skill is better than being a generalist or Jack of all trades."
    )
)

val statementList = statementFullList[intensity]!!
val host = RobotRegistry.robots[intensity]!!
var statementIndex = 0
var silentPromptsQueue = 0
@Volatile var topicChange: Boolean = false

val Statements: State = state(Parent) {

    val task = tasks["Statements"]!!

    onEntry {
        delay(2000)
        Furhat.dialogHistory.clear()

        /** Set the Robot Persona */
        var prompt = hostRobot.prompt + " " + currentTask!!.robotInstructions
        activate(host, prompt)
//        furhat.sayWithTone(Tone.HAPPY, "Hey ${persons["Person A"]!!.name} and ${persons["Person B"]!!.name}, my name is ${host.name}, and I'll be facilitating the discussion for this next task.", rate=1.0)
//        furhat.sayWithTone(Tone.HAPPY, task.introduction, rate=1.0)
//        furhat.sayWithTone(Tone.HAPPY, "The first statement is... ${statementList[statementIndex]}.")

        LastSpokenTracker.start(furhat,
            silentEvent = { person, options ->
                raise(SpeakerSilentEvent(person, options))
            },
        )
        DiscussionTimer.start(
            topicChangeEvent = { raise(DiscussionTimer.TopicChangeEvent()) },
        )
        reentry()
    }

    onReentry {
        furhat.listen(endSil = 3500)
    }

    onResponse("Give the next statement.") {
        DiscussionTimer.skipPrompt()
    }

    onResponse {
        furhat.gesture(GazeAversion(2.0))
        var speaker = SpeakerTracker.getDominantSpeaker(5000)
        LastSpokenTracker.recordSpoke(speaker)
        val text = it.text

        println("\n\n*******\nHeard Speech")
        println("[Recognized Speech] Speaker: $speaker | Said: \"$text\"")
        respondBasedOnSpeaker(text, speaker)
        reentry()
    }

    onNoResponse {
        reentry()
    }

//    onEvent<SpeakerSilentEvent> { event ->
//        println("Speaker Silent Raised")
//        if (topicChange || silentPromptsQueue>0 || isSomeoneSpeaking) {
//            return@onEvent
//        }
//        silentPromptsQueue = 1
//        while (furhat.isSpeaking) {
//            Thread.sleep(300)
//        }
//        val question = call {
//            chatbot.getResponse("", event.person?.condition!!.status,
//                event.person!!.condition!!.personality +
//                        "Generate one 5-10 word question directly pertaining to the discussion to extend or dig deeper into the existing discussion. " +
//                        "Only say the question. If possible, make the question related to points made by ${event.person.name}. Otherwise, generate a generic question that extends the statement. Always ask a question.")
//        } as String
//        silentPromptsQueue = 0
//        furhat.attend(event.person.location)
//        furhat.sayWithTone(
//            Tone.HAPPY,
//            *event.options.toTypedArray()
//        )
//        furhat.sayWithTone(
//            Tone.HAPPY,
//            question
//        )
//        reentry()
//    }

    onEvent<DiscussionTimer.TopicChangeEvent> { event ->
        println("Topic Change Slated")
        topicChange = true
        while (furhat.isSpeaking) {
            Thread.sleep(300)
        }

        statementIndex++
        if (statementIndex >= statementList.size) {
            furhat.sayWithTone(Tone.HAPPY,"That's all the statements we have for today. Thanks for the discussion!")
            topicChange = false
            return@onEvent
        }

        val nextStatement = statementList[statementIndex]
        furhat.sayWithTone(Tone.HAPPY,
            "Alright, time's up, so we have to move on. The next statement is... $nextStatement",
            "Thanks for sharing your thoughts, time is now up. Let's discuss the next statement! The next statement is... $nextStatement",
            "Thanks guys, I appreciated the great discussion on that one. The next statement is... $nextStatement")
        Furhat.dialogHistory.clear()
        reentry()
    }
}

object DiscussionTimer {
    @Volatile private var running = false
    @Volatile private var skip = false
    private var timerThread: Thread? = null

    class TopicChangeEvent : Event()

    fun start(
        topicChangeEvent: () -> Unit,
        durationSeconds: Long = 120,
    ) {
        stop() // cancel any previous timer before starting a new one

        running = true
        timerThread = thread(isDaemon = true) {
            var startTime = System.currentTimeMillis()

            println("3 minute timer reset")

            while (running) {
                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                val remaining = durationSeconds - elapsedSeconds

                if (remaining <= 0 || skip) {
                    if (SpeakerTracker.isSomeoneSpeaking) {
                        // Someone's mid-sentence — wait and check again shortly
                        Thread.sleep(2500)
                        println("Time is up but someone is talking.")
                        continue
                    } else {
                        // Give it a brief settle window to avoid catching a natural micro-pause
                        if (!SpeakerTracker.isSomeoneSpeaking) {
                            skip = false
                            println("[DiscussionTimer] Silence confirmed — announcing subject change.")
                            topicChangeEvent()
                            topicChange = false
                            println("Topic Change Finished")
                            Thread.sleep(10000)
                            startTime = System.currentTimeMillis()
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    fun skipPrompt() {
        skip = true
    }

    fun stop() {
        running = false
        timerThread = null
    }

    fun isRunning(): Boolean = running
}
package com.example.frontend.api.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.frontend.api.convertUtcToLocal
import java.time.DayOfWeek

enum class ReminderType {
    BASE
}

fun reminderTypeToString(type: ReminderType): String {
    return when (type) {
        ReminderType.BASE -> "BASE"
    }
}

fun reminderStringToType(type: String): ReminderType {
    return when (type) {
        "BASE" -> ReminderType.BASE
        else -> {
            throw IllegalStateException("ReminderType $type undefined")
        }
    }
}

enum class TimePeriod { AM, PM }
enum class RepeatMode { DAY_OF_WEEK, DAY_OF_MONTH }

data class ReminderSchedule(
    val hour: Int,                      // 1–12
    val minute: Int,                    // 0–59
    val period: TimePeriod,             // AM or PM

    val repeatMode: RepeatMode,        // Determines which mode is active

    val daysOfWeek: List<DayOfWeek>?,  // Kotlin's built-in enum: MONDAY, TUESDAY, etc. or null if not specified
    val daysOfMonth: List<Int>?,              // [1, 15, 28] or null if not specified
    val month: Int?,                   // 1–12 (nullable if not specified)

    val isRecurring: Boolean           // Toggle: recurring or one-time
) {
    // Convert to 24-hour format
    private fun get24Hour(): Int {
        return when (period) {
            TimePeriod.AM -> if (hour == 12) 0 else hour
            TimePeriod.PM -> if (hour == 12) 12 else hour + 12
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toCronExpression(): String {
        val second = 0
        val minuteStr = minute.toString()
        val hourStr = get24Hour().toString()
        val monthStr = month?.toString() ?: "*"

        val dayOfMonthStr: String
        val dayOfWeekStr: String

        when (repeatMode) {
            RepeatMode.DAY_OF_WEEK -> {
                dayOfWeekStr = if (!daysOfWeek.isNullOrEmpty()) {
                    // Convert Java DayOfWeek to Spring cron format (0=SUN, 1=MON, ..., 6=SAT)
                    daysOfWeek.joinToString(",") { 
                        when (it) {
                            DayOfWeek.SUNDAY -> "0"
                            DayOfWeek.MONDAY -> "1"
                            DayOfWeek.TUESDAY -> "2"
                            DayOfWeek.WEDNESDAY -> "3"
                            DayOfWeek.THURSDAY -> "4"
                            DayOfWeek.FRIDAY -> "5"
                            DayOfWeek.SATURDAY -> "6"
                            else -> "1" // fallback to Monday
                        }
                    }
                } else {
                    "*"
                }
                dayOfMonthStr = "*"
            }

            RepeatMode.DAY_OF_MONTH -> {
                dayOfWeekStr = "*"
                dayOfMonthStr = if (!daysOfMonth.isNullOrEmpty()) {
                    daysOfMonth.joinToString(",")
                } else {
                    "*"
                }
            }
        }

        return "$second $minuteStr $hourStr $dayOfMonthStr $monthStr $dayOfWeekStr"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toReadableString(): String {
        val timeStr = "%d:%02d %s".format(hour, minute, period.name)

        if (!isRecurring) {
            return "Runs once at $timeStr"
        }

        val base = StringBuilder()

        when (repeatMode) {
            RepeatMode.DAY_OF_WEEK -> {
                if (!daysOfWeek.isNullOrEmpty()) {
                    val dayNames = daysOfWeek.joinToString(", ") {
                        it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                    }
                    base.append("Repeats on $dayNames")
                } else {
                    base.append("Repeats daily")
                }
                
                if (month != null) {
                    val monthName = java.time.Month.of(month)
                        .name.lowercase().replaceFirstChar { it.uppercase() }
                    base.append(" in $monthName")
                }
                
                base.append(" at $timeStr")
            }

            RepeatMode.DAY_OF_MONTH -> {
                if (!daysOfMonth.isNullOrEmpty()) {
                    val sortedDays = daysOfMonth.sorted()
                    val daysStr = when (sortedDays.size) {
                        1 -> "the ${getOrdinal(sortedDays[0])}"
                        2 -> "the ${getOrdinal(sortedDays[0])} and ${getOrdinal(sortedDays[1])}"
                        else -> {
                            val lastDay = sortedDays.last()
                            val otherDays = sortedDays.dropLast(1).joinToString(", ") { getOrdinal(it) }
                            "the $otherDays, and ${getOrdinal(lastDay)}"
                        }
                    }
                    base.append("Repeats on $daysStr of")
                } else {
                    base.append("Repeats monthly on")
                }

                if (month != null) {
                    val monthName = java.time.Month.of(month)
                        .name.lowercase().replaceFirstChar { it.uppercase() }
                    base.append(" $monthName")
                } else {
                    base.append(" every month")
                }
                
                base.append(" at $timeStr")
            }
        }

        return base.toString()
    }
    
    private fun getOrdinal(day: Int): String {
        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$day$suffix"
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromCronExpression(cron: String): ReminderSchedule? {
            val parts = cron.trim().split(" ")
            if (parts.size != 6) return null

            val secondStr = parts[0]
            val minuteStr = parts[1]
            val hourStr = parts[2]
            val domStr = parts[3]
            val monthStr = parts[4]
            val dowStr = parts[5]

            // Parse hour and period
            val hour24 = hourStr.toIntOrNull() ?: return null
            val period: TimePeriod
            val hour12: Int = when {
                hour24 == 0 -> {
                    period = TimePeriod.AM
                    12
                }
                hour24 < 12 -> {
                    period = TimePeriod.AM
                    hour24
                }
                hour24 == 12 -> {
                    period = TimePeriod.PM
                    12
                }
                else -> {
                    period = TimePeriod.PM
                    hour24 - 12
                }
            }

            val minute = minuteStr.toIntOrNull() ?: return null

            val isRecurring = (domStr != "*" && domStr.isNotEmpty()) || (dowStr != "*" && dowStr.isNotEmpty())

            val repeatMode: RepeatMode
            val daysOfWeek: List<DayOfWeek>?
            val daysOfMonth: List<Int>?
            val month: Int?

            if (dowStr != "*" && dowStr.isNotEmpty()) {
                repeatMode = RepeatMode.DAY_OF_WEEK
                daysOfWeek = dowStr.split(",").mapNotNull { dayNum ->
                    try {
                        // Convert Spring cron format (0=SUN, 1=MON, ..., 6=SAT) to Java DayOfWeek
                        when (dayNum.trim()) {
                            "0" -> DayOfWeek.SUNDAY
                            "1" -> DayOfWeek.MONDAY
                            "2" -> DayOfWeek.TUESDAY
                            "3" -> DayOfWeek.WEDNESDAY
                            "4" -> DayOfWeek.THURSDAY
                            "5" -> DayOfWeek.FRIDAY
                            "6" -> DayOfWeek.SATURDAY
                            else -> null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.filterNotNull()
                daysOfMonth = null
            } else {
                repeatMode = RepeatMode.DAY_OF_MONTH
                daysOfWeek = null
                daysOfMonth = if (domStr != "*" && domStr.isNotEmpty()) {
                    domStr.split(",").mapNotNull { it.trim().toIntOrNull() }
                } else {
                    emptyList()
                }
            }

            month = if (monthStr != "*" && monthStr.isNotEmpty()) monthStr.toIntOrNull() else null

            return ReminderSchedule(
                hour = hour12,
                minute = minute,
                period = period,
                repeatMode = repeatMode,
                daysOfWeek = daysOfWeek,
                daysOfMonth = daysOfMonth,
                month = month,
                isRecurring = isRecurring
            )
        }
    }


}

data class RequestStoreReminderRaw(val userId: String?, val title: String, val description: String, val reminderType: String, val cronExpression: String, val isRecurring: Boolean = false)
data class RequestStoreReminder(val userId: String?, val title: String, val description: String, val reminderType: ReminderType, val schedule: ReminderSchedule) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toRaw(): RequestStoreReminderRaw {
        return RequestStoreReminderRaw(userId, title, description, reminderTypeToString(reminderType), schedule.toCronExpression(), schedule.isRecurring)
    }
}


data class Reminder(val id: String, val reminderType: ReminderType, val title: String, val description: String, val schedule: ReminderSchedule, val createdAt: String, val nextExecution: String)
data class ReminderRaw(val id: String, val reminderType: String, val title: String, val description: String, val cronExpression: String, val createdAt: String, val isRecurring: Boolean, val nextExecution: String, val zoneId: String) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWrapper(): Reminder {
        return Reminder(id, reminderStringToType(reminderType), title, description, ReminderSchedule.fromCronExpression(cronExpression)!!, convertUtcToLocal(createdAt), convertUtcToLocal(nextExecution))
    }
}

data class ResponseGetReminders(val content: List<Reminder>, val page: PageInfo)
data class ResponseGetRemindersRaw(val content: List<ReminderRaw>, val page: PageInfo) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWrapper(): ResponseGetReminders {
        return ResponseGetReminders(content.map { it.toWrapper() }, page)
    }
}
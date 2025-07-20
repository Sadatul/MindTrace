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

    fun toCronExpression(): String {
        val second = 0
        val minuteStr = minute.toString()
        val hourStr = get24Hour().toString()
        val monthStr = month?.toString() ?: if (isRecurring) "*" else "?"

        val dayOfMonthStr: String
        val dayOfWeekStr: String

        when (repeatMode) {
            RepeatMode.DAY_OF_WEEK -> {
                dayOfWeekStr = if (!daysOfWeek.isNullOrEmpty()) {
                    daysOfWeek.joinToString(",") { it.name.substring(0, 3).uppercase() }  // MON, TUE, etc.
                } else if (isRecurring) {
                    "*"
                } else {
                    "?"
                }
                dayOfMonthStr = "?"
            }

            RepeatMode.DAY_OF_MONTH -> {
                dayOfWeekStr = "?"
                dayOfMonthStr = if (!daysOfMonth.isNullOrEmpty()) {
                    daysOfMonth.joinToString(",")
                } else if (isRecurring) {
                    "*"
                } else {
                    "?"
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

        val base = StringBuilder("Runs every ")

        when (repeatMode) {
            RepeatMode.DAY_OF_WEEK -> {
                val days = if (!daysOfWeek.isNullOrEmpty()) {
                    daysOfWeek.joinToString(", ") {
                        it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
                    }
                } else {
                    "day"
                }
                base.append("$days at $timeStr")
            }

            RepeatMode.DAY_OF_MONTH -> {
                val daysStr = if (!daysOfMonth.isNullOrEmpty()) {
                    "days " + daysOfMonth.joinToString(", ")
                } else {
                    "unspecified days"
                }

                base.append("month on $daysStr")
                if (month != null) {
                    val monthName = java.time.Month.of(month)
                        .name.lowercase().replaceFirstChar { it.uppercase() }
                    base.append(" of $monthName")
                }
                base.append(" at $timeStr")
            }
        }

        return base.toString()
    }

    companion object {
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

            val isRecurring = (domStr == "*" || dowStr == "*" || monthStr == "*")

            val repeatMode: RepeatMode
            val daysOfWeek: List<DayOfWeek>?
            val daysOfMonth: List<Int>?
            val month: Int?

            if (dowStr != "?" && dowStr != "*") {
                repeatMode = RepeatMode.DAY_OF_WEEK
                daysOfWeek = dowStr.split(",").mapNotNull { abbr ->
                    try {
                        DayOfWeek.valueOf(abbr.uppercase())
                    } catch (e: Exception) {
                        null
                    }
                }
                daysOfMonth = null
            } else {
                repeatMode = RepeatMode.DAY_OF_MONTH
                daysOfWeek = null
                daysOfMonth = domStr.split(",").mapNotNull { it.toIntOrNull() }
            }

            month = if (monthStr != "*" && monthStr != "?") monthStr.toIntOrNull() else null

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
data class RequestStoreReminder(val userId: String, val title: String, val description: String, val reminderType: ReminderType, val schedule: ReminderSchedule) {
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
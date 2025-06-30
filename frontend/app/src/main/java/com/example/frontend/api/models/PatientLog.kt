package com.example.frontend.api.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.frontend.api.convertUtcToLocal

enum class LogType {
    EATING, BATHING, SOCIAL, MEDICINE, OUTINGS
}

fun logTypeToString(type: LogType): String {
    return when (type) {
        LogType.EATING -> "EATING"
        LogType.BATHING -> "BATHING"
        LogType.SOCIAL -> "SOCIAL"
        LogType.MEDICINE -> "MEDICINE"
        LogType.OUTINGS -> "OUTINGS"
    }
}

fun logStringToType(type: String): LogType {
    return when (type) {
        "EATING" -> LogType.EATING
        "BATHING" -> LogType.BATHING
        "SOCIAL" -> LogType.SOCIAL
        "MEDICINE" -> LogType.MEDICINE
        "OUTINGS" -> LogType.OUTINGS
        else -> {
            throw IllegalStateException("LogType undefined")
        }
    }
}

data class PatientLog(val id: String, val type: LogType, val description: String, val createdAt: String)
data class PatientLogRaw(val id: String, val type: String, val description: String, val createdAt: String) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWrapper(): PatientLog {
        return PatientLog(id, logStringToType(type), description, convertUtcToLocal(createdAt))
    }
}

data class RequestStoreLogRaw(val type: String, val description: String, val time: Int)
data class RequestStoreLog(val type: LogType, val description: String, val time: Int) {
    fun toRaw(): RequestStoreLogRaw {
        return RequestStoreLogRaw(logTypeToString(type), description, time)
    }
}

data class LogMetadataRaw(val id: String, val type: String, val description: String, val createdAt: String) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWrapper(): LogMetadata {
        return LogMetadata(id, logStringToType(type), description, convertUtcToLocal(createdAt))
    }
}
data class LogMetadata(val id: String, val type: LogType, val description: String, val createdAt: String)
data class ResponseLogMetadata(val content: List<LogMetadata>, val page: PageInfo)
data class ResponseLogsMetadataRaw(val content: List<LogMetadataRaw>, val page: PageInfo) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWrapper(): ResponseLogMetadata {
        return ResponseLogMetadata(content.map { it.toWrapper() }, page)
    }
}

data class RequestUpdateLog(val type: LogType, val description: String) {
    fun toRaw(): RequestUpdateLogRaw {
        return RequestUpdateLogRaw(logTypeToString(type), description)
    }
}
data class RequestUpdateLogRaw(val type: String, val description: String)
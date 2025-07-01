package com.example.frontend.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getLogs
import com.example.frontend.api.getPartners
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.models.LogType
import com.example.frontend.api.models.PatientLog
import com.example.frontend.api.models.RequestStoreLog
import com.example.frontend.api.storeLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

sealed class LogsUiState {
    object Loading : LogsUiState()
    data class Success(
        val logs: List<PatientLog>,
        val patientName: String?,
        val profilePicture: String?
    ) : LogsUiState()
    object Error : LogsUiState()
}

@RequiresApi(Build.VERSION_CODES.O)
 class LogsViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow<LogsUiState>(LogsUiState.Loading)
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        fetchLogs()
    }

    private suspend fun fetchLogsForUser(
        userId: String?,
        name: String?,
        profilePicture: String?
    ): Triple<List<PatientLog>, String?, String?> {
        val logResponse = RetrofitInstance.dementiaAPI.getLogs(
            userId = userId,
            start = null,
            end = null,
            page = 0,
            size = 100
        )
        val logs = logResponse?.content?.map { meta ->
            PatientLog(
                id = meta.id,
                type = meta.type,
                description = meta.description,
                createdAt = meta.createdAt
            )
        } ?: emptyList()
        return Triple(logs, name, profilePicture)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchLogs(patientId: String? = null) {
        _uiState.value = LogsUiState.Loading
        viewModelScope.launch {
            try {
                val userInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
                Log.d("Log", "[fetchLogs] userInfo: $userInfo, userId: ${userInfo?.id}, role: ${userInfo?.role}, patientId: $patientId")
                if (userInfo?.role == "CAREGIVER" && patientId != null) {
                    val partner = RetrofitInstance.dementiaAPI.getPartners(false).find { it.id == patientId }
                    Log.d("Log", "[fetchLogs] Caregiver viewing logs for patient: $partner")
                    val (logs, patientName, profilePicture) = fetchLogsForUser(
                        userId = patientId,
                        name = partner?.name,
                        profilePicture = partner?.profilePicture
                    )
                    Log.d("Log", "[fetchLogs] Logs count: ${logs.size}, patientName: $patientName, profilePicture: $profilePicture")
                    _uiState.value = LogsUiState.Success(logs, patientName, profilePicture)
                } else {
                    val profilePicture = if (!userInfo?.profilePicture.isNullOrEmpty()) {
                        userInfo.profilePicture
                    } else {
                        try {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            firebaseUser?.photoUrl?.toString()
                        } catch (_: Exception) { null }
                    }
                    Log.d("Log", "[fetchLogs] Patient viewing own logs. Name: ${userInfo?.name}, profilePicture: $profilePicture")
                    val (logs, patientName, profilePictureFinal) = fetchLogsForUser(
                        userId = userInfo?.id,
                        name = userInfo?.name,
                        profilePicture = profilePicture
                    )
                    Log.d("Log", "[fetchLogs] Logs count: ${logs.size}, patientName: $patientName, profilePicture: $profilePictureFinal")
                    _uiState.value = LogsUiState.Success(logs, patientName, profilePictureFinal)
                }
            } catch (e: Exception) {
                Log.e("Log", "Error in fetchLogs", e)
                _uiState.value = LogsUiState.Error
            }
        }
    }

    fun addLog(type: LogType, description: String) {
        viewModelScope.launch {
            try {
                val currentTimeSeconds = (System.currentTimeMillis() / 1000).toInt()
                val request = RequestStoreLog(
                    type = type,
                    description = description,
                    time = currentTimeSeconds
                )
                // Optimistically update UI
                val currentState = _uiState.value
                if (currentState is LogsUiState.Success) {
                    val logs = PatientLog(
                        id = "", // ID will be generated by backend
                        type = type,
                        description = description,
                        createdAt = currentTimeSeconds.toString()
                    )
                    _uiState.value = currentState.copy(
                        logs = listOf(logs) + currentState.logs
                    )
                }
                val success = RetrofitInstance.dementiaAPI.storeLog(request)
                // Print backend response
                println("Backend response for storeLog: $success")
                if (success) {
                    fetchLogs()
                }
            } catch (e: Exception) {
                // Optionally handle error
                println("Error in addLog: ${e.message}")
            }
        }
    }
}

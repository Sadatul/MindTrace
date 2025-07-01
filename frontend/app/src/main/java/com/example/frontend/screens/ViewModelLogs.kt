package com.example.frontend.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getLogs
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.models.PatientLog
import com.example.frontend.api.models.LogType
import com.example.frontend.api.models.RequestStoreLog
import com.example.frontend.api.models.RequestUpdateLog
import com.example.frontend.api.storeLog
import com.example.frontend.api.updateLog
import com.example.frontend.api.deleteLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class ViewModelLogs() : ViewModel() {
    
    private val _logs = MutableStateFlow<List<PatientLog>>(emptyList())
    val logs: StateFlow<List<PatientLog>> = _logs.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _patientInfo = MutableStateFlow<UserInfo?>(null)
    val patientInfo: StateFlow<UserInfo?> = _patientInfo.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _editingLog = MutableStateFlow<PatientLog?>(null)
    val editingLog: StateFlow<PatientLog?> = _editingLog.asStateFlow()

    private var currentPartnerId: String? = null

    fun loadLogs(partnerId: String? = null) {
        currentPartnerId = partnerId
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // If partnerId is provided, we're loading logs for a specific patient (caregiver view)
                // Otherwise, we're loading logs for the current user (patient view)
                val userId = partnerId
                
                // Fetch logs
                val response = RetrofitInstance.dementiaAPI.getLogs(
                    userId = userId,
                    start = null,
                    end = null,
                    page = 0,
                    size = 50
                )
                
                if (response != null) {
                    _logs.value = response.content.map { logMetadata ->
                        // Convert LogMetadata to PatientLog if needed
                        PatientLog(
                            id = logMetadata.id,
                            type = logMetadata.type,
                            description = logMetadata.description,
                            createdAt = logMetadata.createdAt
                        )
                    }
                } else {
                    _logs.value = emptyList()
                }
                
                // Fetch user info
                if (partnerId != null) {
                    // We're viewing a specific patient's logs (caregiver view)
                    val token = RetrofitInstance.dementiaAPI.getIdToken()
                    if (token != null) {
                        val userInfoResponse = RetrofitInstance.dementiaAPI.getUserInfo("Bearer $token", partnerId)
                        if (userInfoResponse.isSuccessful) {
                            _patientInfo.value = userInfoResponse.body()
                        }
                    }
                } else {
                    // We're viewing the current user's own logs (patient view)
                    val selfUserInfo = RetrofitInstance.dementiaAPI.getSelfUserInfo()
                    if (selfUserInfo != null) {
                        _patientInfo.value = selfUserInfo
                    }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load logs: ${e.message}"
                _logs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addLog(logType: LogType, description: String) {
        viewModelScope.launch {
            try {
                val success = if (currentPartnerId != null) {
                    // Caregiver viewing patient logs - API limitation: log will be created in caregiver's account
                    _errorMessage.value = "Note: Due to API limitations, the log will be added to your account instead of the patient's account."
                    RetrofitInstance.dementiaAPI.storeLog(
                        RequestStoreLog(
                            type = logType,
                            description = description,
                            time = (System.currentTimeMillis() / 1000).toInt()
                        )
                    )
                } else {
                    // Patient viewing their own logs
                    RetrofitInstance.dementiaAPI.storeLog(
                        RequestStoreLog(
                            type = logType,
                            description = description,
                            time = (System.currentTimeMillis() / 1000).toInt()
                        )
                    )
                }
                
                if (success) {
                    _errorMessage.value = "Log added successfully"
                    // Reload logs to show the updated list
                    loadLogs(currentPartnerId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding log: ${e.message}"
            }
        }
    }

    fun startEditingLog(log: PatientLog) {
        _editingLog.value = log
    }

    fun stopEditingLog() {
        _editingLog.value = null
    }

    fun updateLog(logId: String, logType: LogType, description: String) {
        viewModelScope.launch {
            try {
                val success = RetrofitInstance.dementiaAPI.updateLog(
                    logId,
                    RequestUpdateLog(
                        type = logType,
                        description = description
                    )
                )
                
                if (success) {
                    _editingLog.value = null
                    _errorMessage.value = "Log updated successfully"
                    // Reload logs to show the updated list
                    loadLogs(currentPartnerId)
                } else {
                    _errorMessage.value = "Failed to update log"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating log: ${e.message}"
            }
        }
    }

    fun deleteLog(logId: String) {
        viewModelScope.launch {
            try {
                val success = RetrofitInstance.dementiaAPI.deleteLog(logId)
                
                if (success) {
                    _errorMessage.value = "Log deleted successfully"
                    // Reload logs to show the updated list
                    loadLogs(currentPartnerId)
                } else {
                    _errorMessage.value = "Failed to delete log"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting log: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

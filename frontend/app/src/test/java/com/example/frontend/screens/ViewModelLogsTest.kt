package com.example.frontend.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.frontend.api.DementiaAPI
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.models.PatientLog
import com.example.frontend.api.models.LogType
import com.example.frontend.api.models.RequestStoreLog
import com.example.frontend.api.models.RequestUpdateLog
import com.example.frontend.api.models.ResponseLogMetadata
import com.example.frontend.api.models.LogMetadata
import com.example.frontend.api.models.PageInfo
import com.example.frontend.api.PrimaryContact
import com.example.frontend.api.deleteLog
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getLogs
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.api.storeLog
import com.example.frontend.api.updateLog
import retrofit2.Response
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ViewModelLogsTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockDementiaAPI: DementiaAPI

    @Mock
    private lateinit var mockRetrofitInstance: RetrofitInstance

    private lateinit var mockRetrofitStatic: MockedStatic<RetrofitInstance>

    private lateinit var viewModel: ViewModelLogs
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        mockRetrofitStatic = mockStatic(RetrofitInstance::class.java)
        mockRetrofitStatic.`when`<DementiaAPI> { RetrofitInstance.dementiaAPI }.thenReturn(mockDementiaAPI)
        
        viewModel = ViewModelLogs()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockRetrofitStatic.close()
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        assertEquals(emptyList<PatientLog>(), viewModel.logs.first())
        assertEquals(false, viewModel.isLoading.first())
        assertEquals(null, viewModel.patientInfo.first())
        assertEquals(null, viewModel.errorMessage.first())
        assertEquals(null, viewModel.editingLog.first())
        assertEquals(null, viewModel.startDate.first())
        assertEquals(null, viewModel.endDate.first())
    }

    @Test
    fun `loadLogs successfully loads logs for current user`() = runTest {
        val mockResponse = createMockResponseLogMetadata()
        val mockUserInfo = createMockUserInfo()
        
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(mockUserInfo)
        
        viewModel.loadLogs()
        
        val logs = viewModel.logs.first()
        val patientInfo = viewModel.patientInfo.first()
        val isLoading = viewModel.isLoading.first()
        
        assertEquals(2, logs.size)
        assertEquals("log1", logs[0].id)
        assertEquals(LogType.EATING, logs[0].type)
        assertEquals("Had breakfast", logs[0].description)
        assertEquals(mockUserInfo, patientInfo)
        assertEquals(false, isLoading)
    }

    @Test
    fun `loadLogs successfully loads logs for specific partner`() = runTest {
        val partnerId = "partner123"
        val mockResponse = createMockResponseLogMetadata()
        val mockUserInfo = createMockUserInfo()
        val mockToken = "firebase-token"
        val mockUserResponse = mock(Response::class.java) as Response<UserInfo>
        
        `when`(mockDementiaAPI.getLogs(partnerId, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getIdToken()).thenReturn(mockToken)
        `when`(mockDementiaAPI.getUserInfo("Bearer $mockToken", partnerId)).thenReturn(mockUserResponse)
        `when`(mockUserResponse.isSuccessful).thenReturn(true)
        `when`(mockUserResponse.body()).thenReturn(mockUserInfo)
        
        viewModel.loadLogs(partnerId)
        
        val logs = viewModel.logs.first()
        val patientInfo = viewModel.patientInfo.first()
        
        assertEquals(2, logs.size)
        assertEquals(mockUserInfo, patientInfo)
    }

    @Test
    fun `loadLogs sets loading state correctly`() = runTest {
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        // Check initial loading state
        assertEquals(false, viewModel.isLoading.first())
        
        viewModel.loadLogs()
        
        // After loading completes, should be false
        assertEquals(false, viewModel.isLoading.first())
    }

    @Test
    fun `loadLogs handles API error gracefully`() = runTest {
        val errorMessage = "Network error"
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenThrow(RuntimeException(errorMessage))
        
        viewModel.loadLogs()
        
        val logs = viewModel.logs.first()
        val error = viewModel.errorMessage.first()
        val isLoading = viewModel.isLoading.first()
        
        assertEquals(emptyList<PatientLog>(), logs)
        assertEquals("Failed to load logs: $errorMessage", error)
        assertEquals(false, isLoading)
    }

    @Test
    fun `loadLogs handles null response`() = runTest {
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(null)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.loadLogs()
        
        val logs = viewModel.logs.first()
        assertEquals(emptyList<PatientLog>(), logs)
    }

    @Test
    fun `addLog successfully adds new log`() = runTest {
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.storeLog(any())).thenReturn(true)
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.addLog(LogType.EATING, "Had lunch", 1234567890)
        
        val errorMessage = viewModel.errorMessage.first()
        assertEquals("Log added successfully", errorMessage)
        
        verify(mockDementiaAPI).storeLog(RequestStoreLog(LogType.EATING, "Had lunch", 1234567890))
    }

    @Test
    fun `addLog handles API error`() = runTest {
        val errorMessage = "API error"
        `when`(mockDementiaAPI.storeLog(any())).thenThrow(RuntimeException(errorMessage))
        
        viewModel.addLog(LogType.BATHING, "Took shower", 1234567890)
        
        val error = viewModel.errorMessage.first()
        assertEquals("Error adding log: $errorMessage", error)
    }

    @Test
    fun `addLog handles API failure`() = runTest {
        `when`(mockDementiaAPI.storeLog(any())).thenReturn(false)
        
        viewModel.addLog(LogType.SOCIAL, "Visited friends", 1234567890)
        
        // Should not set success message or reload logs
        verify(mockDementiaAPI, never()).getLogs(any(), any(), any(), any(), any())
    }

    @Test
    fun `startEditingLog sets editing log correctly`() = runTest {
        val log = createMockPatientLog()
        
        viewModel.startEditingLog(log)
        
        val editingLog = viewModel.editingLog.first()
        assertEquals(log, editingLog)
    }

    @Test
    fun `stopEditingLog clears editing log`() = runTest {
        val log = createMockPatientLog()
        viewModel.startEditingLog(log)
        
        viewModel.stopEditingLog()
        
        val editingLog = viewModel.editingLog.first()
        assertNull(editingLog)
    }

    @Test
    fun `updateLog successfully updates existing log`() = runTest {
        val logId = "log123"
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.updateLog(eq(logId), any())).thenReturn(true)
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.updateLog(logId, LogType.MEDICINE, "Updated description")
        
        val errorMessage = viewModel.errorMessage.first()
        val editingLog = viewModel.editingLog.first()
        
        assertEquals("Log updated successfully", errorMessage)
        assertNull(editingLog)
        
        verify(mockDementiaAPI).updateLog(logId, RequestUpdateLog(LogType.MEDICINE, "Updated description"))
    }

    @Test
    fun `updateLog handles API failure`() = runTest {
        val logId = "log123"
        `when`(mockDementiaAPI.updateLog(eq(logId), any())).thenReturn(false)
        
        viewModel.updateLog(logId, LogType.MEDICINE, "Updated description")
        
        val errorMessage = viewModel.errorMessage.first()
        assertEquals("Failed to update log", errorMessage)
    }

    @Test
    fun `updateLog handles API error`() = runTest {
        val logId = "log123"
        val errorMessage = "Network error"
        `when`(mockDementiaAPI.updateLog(eq(logId), any())).thenThrow(RuntimeException(errorMessage))
        
        viewModel.updateLog(logId, LogType.MEDICINE, "Updated description")
        
        val error = viewModel.errorMessage.first()
        assertEquals("Error updating log: $errorMessage", error)
    }

    @Test
    fun `deleteLog successfully deletes log`() = runTest {
        val logId = "log123"
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.deleteLog(logId)).thenReturn(true)
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.deleteLog(logId)
        
        val errorMessage = viewModel.errorMessage.first()
        assertEquals("Log deleted successfully", errorMessage)
        
        verify(mockDementiaAPI).deleteLog(logId)
    }

    @Test
    fun `deleteLog handles API failure`() = runTest {
        val logId = "log123"
        `when`(mockDementiaAPI.deleteLog(logId)).thenReturn(false)
        
        viewModel.deleteLog(logId)
        
        val errorMessage = viewModel.errorMessage.first()
        assertEquals("Failed to delete log", errorMessage)
    }

    @Test
    fun `deleteLog handles API error`() = runTest {
        val logId = "log123"
        val errorMessage = "Network error"
        `when`(mockDementiaAPI.deleteLog(logId)).thenThrow(RuntimeException(errorMessage))
        
        viewModel.deleteLog(logId)
        
        val error = viewModel.errorMessage.first()
        assertEquals("Error deleting log: $errorMessage", error)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        // Set an error first
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenThrow(RuntimeException("Error"))
        viewModel.loadLogs()
        
        // Verify error is set
        assertNotNull(viewModel.errorMessage.first())
        
        // Clear error
        viewModel.clearError()
        
        val errorMessage = viewModel.errorMessage.first()
        assertNull(errorMessage)
    }

    @Test
    fun `setStartDate sets start date and reloads logs`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(eq(null), any(), eq(null), eq(0), eq(50))).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.setStartDate(date)
        
        val startDate = viewModel.startDate.first()
        assertNotNull(startDate)
        assertTrue(startDate!!.contains("2024-01-15"))
        assertTrue(startDate.contains("00:00"))
        
        verify(mockDementiaAPI).getLogs(eq(null), any(), eq(null), eq(0), eq(50))
    }

    @Test
    fun `setStartDate with null clears start date`() = runTest {
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.setStartDate(null)
        
        val startDate = viewModel.startDate.first()
        assertNull(startDate)
    }

    @Test
    fun `setEndDate sets end date and reloads logs`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(eq(null), eq(null), any(), eq(0), eq(50))).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.setEndDate(date)
        
        val endDate = viewModel.endDate.first()
        assertNotNull(endDate)
        assertTrue(endDate!!.contains("2024-01-15"))
        assertTrue(endDate.contains("23:59:59"))
        
        verify(mockDementiaAPI).getLogs(eq(null), eq(null), any(), eq(0), eq(50))
    }

    @Test
    fun `setEndDate with null clears end date`() = runTest {
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.setEndDate(null)
        
        val endDate = viewModel.endDate.first()
        assertNull(endDate)
    }

    @Test
    fun `clearDateFilters clears both dates and reloads logs`() = runTest {
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(null, null, null, 0, 50)).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        // Set dates first
        viewModel.setStartDate(LocalDate.of(2024, 1, 15))
        viewModel.setEndDate(LocalDate.of(2024, 1, 20))
        
        // Clear dates
        viewModel.clearDateFilters()
        
        val startDate = viewModel.startDate.first()
        val endDate = viewModel.endDate.first()
        
        assertNull(startDate)
        assertNull(endDate)
        
        verify(mockDementiaAPI, atLeastOnce()).getLogs(null, null, null, 0, 50)
    }

    @Test
    fun `loadLogs with date filters passes correct parameters`() = runTest {
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 1, 20)
        val mockResponse = createMockResponseLogMetadata()
        
        `when`(mockDementiaAPI.getLogs(eq(null), any(), any(), eq(0), eq(50))).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(createMockUserInfo())
        
        viewModel.setStartDate(startDate)
        viewModel.setEndDate(endDate)
        
        // Verify that getLogs was called with non-null date parameters
        verify(mockDementiaAPI, atLeastOnce()).getLogs(
            eq(null), 
            argThat { it != null && it.contains("2024-01-15") },
            argThat { it != null && it.contains("2024-01-20") },
            eq(0), 
            eq(50)
        )
    }

    private fun createMockResponseLogMetadata(): ResponseLogMetadata {
        return ResponseLogMetadata(
            content = listOf(
                LogMetadata(
                    id = "log1",
                    type = LogType.EATING,
                    description = "Had breakfast",
                    createdAt = "2024-01-15T08:00:00Z"
                ),
                LogMetadata(
                    id = "log2",
                    type = LogType.BATHING,
                    description = "Took shower",
                    createdAt = "2024-01-15T09:00:00Z"
                )
            ),
            page = PageInfo(
                size = 50,
                number = 0,
                totalElements = 2,
                totalPages = 1
            )
        )
    }

    private fun createMockUserInfo(): UserInfo {
        return UserInfo(
            id = "user123",
            name = "Test User",
            email = "test@example.com",
            role = "PATIENT",
            gender = "Male",
            dob = "1990-01-01",
            profilePicture = "https://example.com/profile.jpg",
            primaryContact = PrimaryContact(
                id = "contact123",
                name = "Contact Name",
                gender = "Female",
                profilePicture = null,
                createdAt = "2024-01-01T00:00:00Z"
            ),
            createdAt = "2024-01-01T00:00:00Z",
            telegramChatId = "telegram123"
        )
    }

    private fun createMockPatientLog(): PatientLog {
        return PatientLog(
            id = "log123",
            type = LogType.EATING,
            description = "Had lunch",
            createdAt = "2024-01-15T12:00:00Z"
        )
    }
}
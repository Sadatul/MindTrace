package com.example.frontend.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.frontend.R
import com.example.frontend.api.AuthManagerResponse
import com.example.frontend.api.CaregiverRegisterRequest
import com.example.frontend.api.PatientRegisterRequest
import com.example.frontend.api.DementiaAPI
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getSelfUserInfo
import com.example.frontend.screens.models.CaregiverFormData
import com.example.frontend.screens.models.PatientFormData
import com.example.frontend.screens.models.FirebaseCredentials
import com.example.frontend.screens.models.RegisterUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.ResponseBody
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@Suppress("UNCHECKED_CAST")
class ViewModelRegisterTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockGoogleSignInClient: GoogleSignInClient

    @Mock
    private lateinit var mockDementiaAPI: DementiaAPI

    @Mock
    private lateinit var mockIntent: Intent

    private lateinit var mockRetrofitStatic: MockedStatic<RetrofitInstance>
    private lateinit var mockFirebaseStatic: MockedStatic<Firebase>
    private lateinit var mockGoogleSignInStatic: MockedStatic<GoogleSignIn>

    private lateinit var viewModel: ViewModelRegister
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        mockRetrofitStatic = mockStatic(RetrofitInstance::class.java)
        mockFirebaseStatic = mockStatic(Firebase::class.java)
        mockGoogleSignInStatic = mockStatic(GoogleSignIn::class.java)

        mockRetrofitStatic.`when`<DementiaAPI> { RetrofitInstance.dementiaAPI }.thenReturn(mockDementiaAPI)
        mockFirebaseStatic.`when`<FirebaseAuth> { Firebase.auth }.thenReturn(mockFirebaseAuth)

        viewModel = ViewModelRegister()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockRetrofitStatic.close()
        mockFirebaseStatic.close()
        mockGoogleSignInStatic.close()
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        val initialState = viewModel.uiState.first()

        assertEquals(false, initialState.isLoading)
        assertEquals(null, initialState.errorMsg)
        assertEquals(false, initialState.showRegisterPrompt)
        assertEquals(false, initialState.showRoleDialog)
        assertEquals(false, initialState.showCaregiverForm)
        assertEquals(false, initialState.showPatientForm)
        assertEquals(false, initialState.showConfirmationDialog)
        assertEquals(null, initialState.selectedRole)
        assertEquals(CaregiverFormData(), initialState.caregiverFormData)
        assertEquals(PatientFormData(), initialState.patientFormData)
        assertEquals(null, initialState.firebaseCredentials)
    }

    @Test
    fun `initializeGoogleSignInClient creates client correctly`() {
        `when`(mockContext.getString(R.string.default_web_client_id)).thenReturn("test-client-id")
        mockGoogleSignInStatic.`when`<GoogleSignInClient> { 
            GoogleSignIn.getClient(eq(mockContext), any(GoogleSignInOptions::class.java)) 
        }.thenReturn(mockGoogleSignInClient)

        viewModel.initializeGoogleSignInClient(mockContext)

        mockGoogleSignInStatic.verify {
            GoogleSignIn.getClient(eq(mockContext), any(GoogleSignInOptions::class.java))
        }
    }

    @Test
    fun `clearAllDialogs resets all dialog states`() = runTest {
        // Set some dialogs to true
        viewModel.showRegisterPrompt()
        viewModel.showRoleDialog()
        viewModel.selectRole("caregiver")

        viewModel.clearAllDialogs()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showRegisterPrompt)
        assertEquals(false, state.showRoleDialog)
        assertEquals(false, state.showCaregiverForm)
        assertEquals(false, state.showPatientForm)
        assertEquals(false, state.showConfirmationDialog)
        assertEquals(null, state.selectedRole)
    }

    @Test
    fun `clearAllDialogs preserves selectedRole when clearSelectedRoleToo is false`() = runTest {
        viewModel.selectRole("caregiver")

        viewModel.clearAllDialogs(clearSelectedRoleToo = false)

        val state = viewModel.uiState.first()
        assertEquals("caregiver", state.selectedRole)
    }

    @Test
    fun `setError updates error message`() = runTest {
        val errorMessage = "Test error message"

        viewModel.setError(errorMessage)

        val state = viewModel.uiState.first()
        assertEquals(errorMessage, state.errorMsg)
    }

    @Test
    fun `setLoading updates loading state`() = runTest {
        viewModel.setLoading(true)

        val state = viewModel.uiState.first()
        assertEquals(true, state.isLoading)
    }

    @Test
    fun `showRegisterPrompt clears dialogs and shows register prompt`() = runTest {
        viewModel.showRegisterPrompt()

        val state = viewModel.uiState.first()
        assertEquals(true, state.showRegisterPrompt)
        assertEquals(false, state.showRoleDialog)
    }

    @Test
    fun `showRoleDialog hides register prompt and shows role dialog`() = runTest {
        viewModel.showRegisterPrompt()
        
        viewModel.showRoleDialog()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showRegisterPrompt)
        assertEquals(true, state.showRoleDialog)
    }

    @Test
    fun `selectRole sets role and shows confirmation dialog`() = runTest {
        val role = "caregiver"

        viewModel.selectRole(role)

        val state = viewModel.uiState.first()
        assertEquals(role, state.selectedRole)
        assertEquals(false, state.showRoleDialog)
        assertEquals(true, state.showConfirmationDialog)
    }

    @Test
    fun `confirmRole for caregiver shows caregiver form with prefilled data`() = runTest {
        val credentials = createMockFirebaseCredentials()
        viewModel._uiState.value = viewModel.uiState.first().copy(
            selectedRole = "caregiver",
            firebaseCredentials = credentials
        )

        viewModel.confirmRole()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showConfirmationDialog)
        assertEquals(true, state.showCaregiverForm)
        assertEquals(credentials.displayName, state.caregiverFormData.name)
        assertEquals(credentials.email, state.caregiverFormData.email)
        assertEquals("", state.caregiverFormData.dob)
        assertEquals("", state.caregiverFormData.gender)
    }

    @Test
    fun `confirmRole for patient shows patient form with prefilled data`() = runTest {
        val credentials = createMockFirebaseCredentials()
        viewModel._uiState.value = viewModel.uiState.first().copy(
            selectedRole = "patient",
            firebaseCredentials = credentials
        )

        viewModel.confirmRole()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showConfirmationDialog)
        assertEquals(true, state.showPatientForm)
        assertEquals(credentials.displayName, state.patientFormData.name)
        assertEquals(credentials.email, state.patientFormData.email)
        assertEquals("", state.patientFormData.dob)
        assertEquals("", state.patientFormData.gender)
        assertEquals("", state.patientFormData.primaryContact)
        assertEquals("", state.patientFormData.otp)
    }

    @Test
    fun `backToRoleSelection hides confirmation and shows role dialog`() = runTest {
        viewModel.selectRole("caregiver")

        viewModel.backToRoleSelection()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showConfirmationDialog)
        assertEquals(true, state.showRoleDialog)
    }

    @Test
    fun `dismissConfirmationDialog hides confirmation dialog`() = runTest {
        viewModel.selectRole("caregiver")

        viewModel.dismissConfirmationDialog()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showConfirmationDialog)
    }

    @Test
    fun `dismissCaregiverForm hides caregiver form`() = runTest {
        viewModel.confirmRole() // This should show caregiver form if role is caregiver
        
        viewModel.dismissCaregiverForm()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showCaregiverForm)
    }

    @Test
    fun `dismissPatientForm hides patient form`() = runTest {
        viewModel.dismissPatientForm()

        val state = viewModel.uiState.first()
        assertEquals(false, state.showPatientForm)
    }

    @Test
    fun `updateCaregiverFormData updates form data`() = runTest {
        val formData = CaregiverFormData(
            name = "John Doe",
            email = "john@example.com",
            dob = "1990-01-01",
            gender = "Male"
        )

        viewModel.updateCaregiverFormData(formData)

        val state = viewModel.uiState.first()
        assertEquals(formData, state.caregiverFormData)
    }

    @Test
    fun `updatePatientFormData updates form data`() = runTest {
        val formData = PatientFormData(
            name = "Jane Doe",
            email = "jane@example.com",
            dob = "1990-01-01",
            gender = "Female",
            primaryContact = "john@example.com",
            otp = "123456"
        )

        viewModel.updatePatientFormData(formData)

        val state = viewModel.uiState.first()
        assertEquals(formData, state.patientFormData)
    }

    @Test
    fun `signOut clears firebase credentials and signs out`() = runTest {
        val mockTask = Tasks.forResult(null as Void?)
        `when`(mockGoogleSignInClient.signOut()).thenReturn(mockTask)
        
        // Set up Firebase credentials first
        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = createMockFirebaseCredentials()
        )

        viewModel.signOut()

        val state = viewModel.uiState.first()
        assertEquals(null, state.firebaseCredentials)
        verify(mockFirebaseAuth).signOut()
    }

    @Test
    fun `getGoogleSignInIntent returns sign in intent`() {
        `when`(mockGoogleSignInClient.signInIntent).thenReturn(mockIntent)
        
        // We need to simulate the GoogleSignInClient being initialized
        val field = ViewModelRegister::class.java.getDeclaredField("googleSignInClient")
        field.isAccessible = true
        field.set(viewModel, mockGoogleSignInClient)

        val result = viewModel.getGoogleSignInIntent()

        assertEquals(mockIntent, result)
    }

    @Test
    fun `handleGoogleSignInResult with cancelled result sets error`() = runTest {
        val onNavigate = mock<(String) -> Unit>()

        viewModel.handleGoogleSignInResult(mockIntent, Activity.RESULT_CANCELED, onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("cancelled") == true)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `handleCaregiverRegistration with valid data succeeds`() = runTest {
        val credentials = createMockFirebaseCredentials()
        val formData = createValidCaregiverFormData()
        val mockUserInfo = createMockUserInfo("CAREGIVER")
        val mockResponse = mock(Response::class.java) as Response<ResponseBody>
        val onNavigate = mock<(String) -> Unit>()

        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.code()).thenReturn(201)
        `when`(mockDementiaAPI.registerCaregiver(anyString(), any())).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getIdToken(true)).thenReturn("fresh-token")
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(mockUserInfo)

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = credentials,
            caregiverFormData = formData
        )

        viewModel.handleCaregiverRegistration(onNavigate)

        verify(onNavigate).invoke("CAREGIVER")
        verify(mockDementiaAPI).registerCaregiver(
            eq("Bearer ${credentials.idToken}"),
            eq(CaregiverRegisterRequest(formData.name, formData.dob, formData.gender))
        )
    }

    @Test
    fun `handleCaregiverRegistration with missing credentials shows error`() = runTest {
        val formData = createValidCaregiverFormData()
        val onNavigate = mock<(String) -> Unit>()

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = null,
            caregiverFormData = formData
        )

        viewModel.handleCaregiverRegistration(onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("Authentication session is invalid") == true)
        verify(onNavigate, never()).invoke(any())
    }

    @Test
    fun `handleCaregiverRegistration with invalid form data shows error`() = runTest {
        val credentials = createMockFirebaseCredentials()
        val invalidFormData = CaregiverFormData(name = "", email = "test@example.com", dob = "", gender = "")
        val onNavigate = mock<(String) -> Unit>()

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = credentials,
            caregiverFormData = invalidFormData
        )

        viewModel.handleCaregiverRegistration(onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("All fields") == true)
        verify(onNavigate, never()).invoke(any())
    }

    @Test
    fun `handleCaregiverRegistration with API failure shows error`() = runTest {
        val credentials = createMockFirebaseCredentials()
        val formData = createValidCaregiverFormData()
        val mockResponse = mock(Response::class.java) as Response<ResponseBody>
        val mockErrorBody = mock(ResponseBody::class.java)
        val onNavigate = mock<(String) -> Unit>()

        `when`(mockResponse.isSuccessful).thenReturn(false)
        `when`(mockResponse.code()).thenReturn(400)
        `when`(mockResponse.errorBody()).thenReturn(mockErrorBody)
        `when`(mockErrorBody.string()).thenReturn("Registration failed")
        `when`(mockDementiaAPI.registerCaregiver(anyString(), any())).thenReturn(mockResponse)

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = credentials,
            caregiverFormData = formData
        )

        viewModel.handleCaregiverRegistration(onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("backend registration failed") == true)
        verify(onNavigate, never()).invoke(any())
    }

    @Test
    fun `handlePatientRegistration with valid data succeeds`() = runTest {
        val credentials = createMockFirebaseCredentials()
        val formData = createValidPatientFormData()
        val mockUserInfo = createMockUserInfo("PATIENT")
        val mockResponse = mock(Response::class.java) as Response<ResponseBody>
        val onNavigate = mock<(String) -> Unit>()

        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.code()).thenReturn(201)
        `when`(mockDementiaAPI.registerPatient(anyString(), any())).thenReturn(mockResponse)
        `when`(mockDementiaAPI.getIdToken(true)).thenReturn("fresh-token")
        `when`(mockDementiaAPI.getSelfUserInfo()).thenReturn(mockUserInfo)

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = credentials,
            patientFormData = formData
        )

        viewModel.handlePatientRegistration(onNavigate)

        verify(onNavigate).invoke("PATIENT")
        verify(mockDementiaAPI).registerPatient(
            eq("Bearer ${credentials.idToken}"),
            eq(PatientRegisterRequest(formData.name, formData.dob, formData.gender, formData.primaryContact, formData.otp))
        )
    }

    @Test
    fun `handlePatientRegistration with missing credentials shows error`() = runTest {
        val formData = createValidPatientFormData()
        val onNavigate = mock<(String) -> Unit>()

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = null,
            patientFormData = formData
        )

        viewModel.handlePatientRegistration(onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("Authentication session is invalid") == true)
        verify(onNavigate, never()).invoke(any())
    }

    @Test
    fun `handlePatientRegistration with invalid form data shows error`() = runTest {
        val credentials = createMockFirebaseCredentials()
        val invalidFormData = PatientFormData(name = "", email = "test@example.com")
        val onNavigate = mock<(String) -> Unit>()

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = credentials,
            patientFormData = invalidFormData
        )

        viewModel.handlePatientRegistration(onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("All fields are required") == true)
        verify(onNavigate, never()).invoke(any())
    }

    @Test
    fun `handlePatientRegistration with API failure shows error`() = runTest {
        val credentials = createMockFirebaseCredentials()
        val formData = createValidPatientFormData()
        val mockResponse = mock(Response::class.java) as Response<ResponseBody>
        val mockErrorBody = mock(ResponseBody::class.java)
        val onNavigate = mock<(String) -> Unit>()

        `when`(mockResponse.isSuccessful).thenReturn(false)
        `when`(mockResponse.code()).thenReturn(400)
        `when`(mockResponse.errorBody()).thenReturn(mockErrorBody)
        `when`(mockErrorBody.string()).thenReturn("Registration failed")
        `when`(mockDementiaAPI.registerPatient(anyString(), any())).thenReturn(mockResponse)

        viewModel._uiState.value = viewModel.uiState.first().copy(
            firebaseCredentials = credentials,
            patientFormData = formData
        )

        viewModel.handlePatientRegistration(onNavigate)

        val state = viewModel.uiState.first()
        assertTrue(state.errorMsg?.contains("backend registration failed") == true)
        verify(onNavigate, never()).invoke(any())
    }

    // Helper methods for creating test data
    private fun createMockFirebaseCredentials(): FirebaseCredentials {
        return FirebaseCredentials(
            userUID = "test-uid",
            displayName = "Test User",
            email = "test@example.com",
            idToken = "test-id-token",
            photoUrl = "https://example.com/photo.jpg"
        )
    }

    private fun createValidCaregiverFormData(): CaregiverFormData {
        return CaregiverFormData(
            name = "John Doe",
            email = "john@example.com",
            dob = "1990-01-01",
            gender = "Male"
        )
    }

    private fun createValidPatientFormData(): PatientFormData {
        return PatientFormData(
            name = "Jane Doe",
            email = "jane@example.com",
            dob = "1990-01-01",
            gender = "Female",
            primaryContact = "john@example.com",
            otp = "123456"
        )
    }

    private fun createMockUserInfo(role: String): UserInfo {
        return UserInfo(
            id = "user123",
            name = "Test User",
            email = "test@example.com",
            role = role,
            gender = "Male",
            dob = "1990-01-01",
            profilePicture = null,
            primaryContact = null,
            createdAt = "2024-01-01T00:00:00Z",
            telegramChatId = null
        )
    }
}
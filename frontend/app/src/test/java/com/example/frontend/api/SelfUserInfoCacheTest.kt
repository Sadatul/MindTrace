package com.example.frontend.api

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.net.Uri

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SelfUserInfoCacheTest {

    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var mockUri: Uri
    private lateinit var mockFirebaseStatic: MockedStatic<Firebase>

    @Before
    fun setUp() {
        mockFirebaseAuth = mock(FirebaseAuth::class.java)
        mockFirebaseUser = mock(FirebaseUser::class.java)
        mockUri = mock(Uri::class.java)
        mockFirebaseStatic = mockStatic(Firebase::class.java)
        
        SelfUserInfoCache.signOutUser()
    }

    @Test
    fun `getUserInfo returns null when no user info is set`() {
        val result = SelfUserInfoCache.getUserInfo()
        
        assertNull(result)
    }

    @Test
    fun `setUserInfo stores user info when profile picture is not null or empty`() {
        val userInfo = createSampleUserInfo(profilePicture = "https://example.com/profile.jpg")
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertEquals(userInfo, result)
        assertEquals("https://example.com/profile.jpg", result?.profilePicture)
    }

    @Test
    fun `setUserInfo uses original user info when profile picture is empty string`() {
        val userInfo = createSampleUserInfo(profilePicture = "")
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertEquals("", result?.profilePicture)
    }

    @Test
    fun `setUserInfo uses original user info when profile picture is null`() {
        val userInfo = createSampleUserInfo(profilePicture = null)
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertNull(result?.profilePicture)
    }

    @Test
    fun `setUserInfo enhances user info with Google profile picture when backend picture is null`() {
        val userInfo = createSampleUserInfo(profilePicture = null)
        val googlePhotoUrl = "https://lh3.googleusercontent.com/profile123"
        
        `when`(mockUri.toString()).thenReturn(googlePhotoUrl)
        `when`(mockFirebaseUser.photoUrl).thenReturn(mockUri)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        mockFirebaseStatic.`when`<FirebaseAuth> { Firebase.auth }.thenReturn(mockFirebaseAuth)
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertEquals(googlePhotoUrl, result?.profilePicture)
        assertEquals(userInfo.name, result?.name)
        assertEquals(userInfo.email, result?.email)
    }

    @Test
    fun `setUserInfo enhances user info with Google profile picture when backend picture is empty`() {
        val userInfo = createSampleUserInfo(profilePicture = "")
        val googlePhotoUrl = "https://lh3.googleusercontent.com/profile456"
        
        `when`(mockUri.toString()).thenReturn(googlePhotoUrl)
        `when`(mockFirebaseUser.photoUrl).thenReturn(mockUri)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        mockFirebaseStatic.`when`<FirebaseAuth> { Firebase.auth }.thenReturn(mockFirebaseAuth)
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertEquals(googlePhotoUrl, result?.profilePicture)
    }

    @Test
    fun `setUserInfo keeps original info when both backend and Google profile pictures are null`() {
        val userInfo = createSampleUserInfo(profilePicture = null)
        
        `when`(mockFirebaseUser.photoUrl).thenReturn(null)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        mockFirebaseStatic.`when`<FirebaseAuth> { Firebase.auth }.thenReturn(mockFirebaseAuth)
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertNull(result?.profilePicture)
    }

    @Test
    fun `setUserInfo keeps original info when Firebase user is null`() {
        val userInfo = createSampleUserInfo(profilePicture = null)
        
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)
        mockFirebaseStatic.`when`<FirebaseAuth> { Firebase.auth }.thenReturn(mockFirebaseAuth)
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertNull(result?.profilePicture)
    }

    @Test
    fun `setUserInfo keeps original info when Google photo URL is empty`() {
        val userInfo = createSampleUserInfo(profilePicture = "")
        
        `when`(mockUri.toString()).thenReturn("")
        `when`(mockFirebaseUser.photoUrl).thenReturn(mockUri)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        mockFirebaseStatic.`when`<FirebaseAuth> { Firebase.auth }.thenReturn(mockFirebaseAuth)
        
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result = SelfUserInfoCache.getUserInfo()
        assertNotNull(result)
        assertEquals("", result?.profilePicture)
    }

    @Test
    fun `signOutUser clears cached user info`() {
        val userInfo = createSampleUserInfo(profilePicture = "https://example.com/profile.jpg")
        SelfUserInfoCache.setUserInfo(userInfo)
        
        assertNotNull(SelfUserInfoCache.getUserInfo())
        
        SelfUserInfoCache.signOutUser()
        
        assertNull(SelfUserInfoCache.getUserInfo())
    }

    @Test
    fun `multiple setUserInfo calls update the cached info`() {
        val userInfo1 = createSampleUserInfo(
            name = "John Doe",
            email = "john@example.com",
            profilePicture = "https://example.com/john.jpg"
        )
        val userInfo2 = createSampleUserInfo(
            name = "Jane Smith",
            email = "jane@example.com",
            profilePicture = "https://example.com/jane.jpg"
        )
        
        SelfUserInfoCache.setUserInfo(userInfo1)
        assertEquals("John Doe", SelfUserInfoCache.getUserInfo()?.name)
        
        SelfUserInfoCache.setUserInfo(userInfo2)
        assertEquals("Jane Smith", SelfUserInfoCache.getUserInfo()?.name)
        assertEquals("jane@example.com", SelfUserInfoCache.getUserInfo()?.email)
    }

    @Test
    fun `user info remains cached across multiple getUserInfo calls`() {
        val userInfo = createSampleUserInfo(profilePicture = "https://example.com/profile.jpg")
        SelfUserInfoCache.setUserInfo(userInfo)
        
        val result1 = SelfUserInfoCache.getUserInfo()
        val result2 = SelfUserInfoCache.getUserInfo()
        val result3 = SelfUserInfoCache.getUserInfo()
        
        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertNotNull(result1)
    }

    private fun createSampleUserInfo(
        id: String = "user123",
        name: String = "Test User",
        email: String = "test@example.com",
        role: String = "CAREGIVER",
        gender: String = "Male",
        dob: String = "1990-01-01",
        profilePicture: String? = null,
        primaryContact: PrimaryContact? = null,
        createdAt: String = "2024-01-01T10:00:00Z",
        telegramChatId: String? = null
    ): UserInfo {
        return UserInfo(
            id = id,
            name = name,
            email = email,
            role = role,
            gender = gender,
            dob = dob,
            profilePicture = profilePicture,
            primaryContact = primaryContact,
            createdAt = createdAt,
            telegramChatId = telegramChatId
        )
    }
}
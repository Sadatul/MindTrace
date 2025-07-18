package com.example.frontend.api

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DeviceRegistrationManagerTest {

    private lateinit var context: Context
    private lateinit var deviceRegistrationManager: DeviceRegistrationManager

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        deviceRegistrationManager = DeviceRegistrationManager(context)
    }

    @Test
    fun `getOrCreateDeviceId generates device ID on first call`() {
        val deviceId = deviceRegistrationManager.getOrCreateDeviceId()
        
        assertNotNull(deviceId)
        assertFalse(deviceId.isEmpty())
        assertEquals(16, deviceId.length) // Should be 16 characters as per implementation
    }

    @Test
    fun `getOrCreateDeviceId returns same device ID on subsequent calls`() {
        val deviceId1 = deviceRegistrationManager.getOrCreateDeviceId()
        val deviceId2 = deviceRegistrationManager.getOrCreateDeviceId()
        
        assertEquals(deviceId1, deviceId2)
    }

    @Test
    fun `generateDeviceName creates human readable device name`() {
        val deviceName = deviceRegistrationManager.generateDeviceName()
        
        assertNotNull(deviceName)
        assertFalse(deviceName.isEmpty())
        // Should contain manufacturer and model or fallback to "Android Device"
        assertTrue(deviceName.contains(" ") || deviceName == "Android Device")
    }

    @Test
    fun `generateDeviceName returns same device name on subsequent calls`() {
        val deviceName1 = deviceRegistrationManager.generateDeviceName()
        val deviceName2 = deviceRegistrationManager.generateDeviceName()
        
        assertEquals(deviceName1, deviceName2)
    }

    @Test
    fun `clearDeviceInfo removes stored device information`() {
        // First generate device ID and name
        val originalDeviceId = deviceRegistrationManager.getOrCreateDeviceId()
        val originalDeviceName = deviceRegistrationManager.generateDeviceName()
        
        assertNotNull(originalDeviceId)
        assertNotNull(originalDeviceName)
        
        // Clear device info
        deviceRegistrationManager.clearDeviceInfo()
        
        // Create new manager instance to simulate app restart
        val newManager = DeviceRegistrationManager(context)
        val newDeviceId = newManager.getOrCreateDeviceId()
        val newDeviceName = newManager.generateDeviceName()
        
        // Should generate new IDs after clearing
        assertNotEquals(originalDeviceId, newDeviceId)
        // Device name might be the same if device info hasn't changed
    }

    @Test
    fun `getLastTokenUpdateTime returns zero initially`() {
        val lastUpdate = deviceRegistrationManager.getLastTokenUpdateTime()
        
        assertEquals(0L, lastUpdate)
    }

    @Test
    fun `device ID is consistent across different manager instances`() {
        val deviceId1 = deviceRegistrationManager.getOrCreateDeviceId()
        
        // Create new manager instance
        val newManager = DeviceRegistrationManager(context)
        val deviceId2 = newManager.getOrCreateDeviceId()
        
        assertEquals(deviceId1, deviceId2)
    }

    @Test
    fun `device name is consistent across different manager instances`() {
        val deviceName1 = deviceRegistrationManager.generateDeviceName()
        
        // Create new manager instance
        val newManager = DeviceRegistrationManager(context)
        val deviceName2 = newManager.generateDeviceName()
        
        assertEquals(deviceName1, deviceName2)
    }
}
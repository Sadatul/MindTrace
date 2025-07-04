package com.example.frontend.api

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TimeConversionTest {

    @Test
    fun `convertUtcToLocal converts valid UTC timestamp to local time`() {
        val utcTimestamp = "2024-01-15T10:30:00Z"
        
        val result = convertUtcToLocal(utcTimestamp)
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        // Result should be different from input (unless system is in UTC)
        if (ZoneId.systemDefault() != ZoneOffset.UTC) {
            assertNotEquals(utcTimestamp, result)
        }
        // Should contain zone info
        assertTrue(result.contains("[") || result.contains("+") || result.contains("-"))
    }

    @Test
    fun `convertUtcToLocal converts valid UTC timestamp with milliseconds`() {
        val utcTimestamp = "2024-01-15T10:30:00.123Z"
        
        val result = convertUtcToLocal(utcTimestamp)
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        // Should successfully parse and convert
        assertFalse(result == utcTimestamp && ZoneId.systemDefault() != ZoneOffset.UTC)
    }

    @Test
    fun `convertUtcToLocal handles different UTC formats`() {
        val testCases = listOf(
            "2024-01-15T10:30:00Z",
            "2024-01-15T10:30:00.000Z",
            "2024-01-15T10:30:00.123Z",
            "2024-12-31T23:59:59Z"
        )
        
        testCases.forEach { utcTimestamp ->
            val result = convertUtcToLocal(utcTimestamp)
            assertNotNull("Failed for: $utcTimestamp", result)
            assertTrue("Empty result for: $utcTimestamp", result.isNotEmpty())
        }
    }

    @Test
    fun `convertUtcToLocal returns original string for invalid format`() {
        val invalidTimestamps = listOf(
            "invalid-timestamp",
            "2024-01-15",
            "2024-01-15T25:00:00Z", // Invalid hour
            "2024-13-15T10:30:00Z", // Invalid month
            "not-a-date",
            "",
            "2024-01-15T10:30:00" // Missing Z
        )
        
        invalidTimestamps.forEach { invalidTimestamp ->
            val result = convertUtcToLocal(invalidTimestamp)
            assertEquals("Should return original for: $invalidTimestamp", invalidTimestamp, result)
        }
    }

    @Test
    fun `convertUtcToLocal handles null input gracefully`() {
        // Note: This test depends on the function signature - if it accepts nullable String
        // Since the current signature doesn't accept null, we'll test empty string
        val result = convertUtcToLocal("")
        assertEquals("", result)
    }

    @Test
    fun `convertUtcToLocal preserves timezone information in output`() {
        val utcTimestamp = "2024-06-15T12:00:00Z"
        
        val result = convertUtcToLocal(utcTimestamp)
        
        // Result should contain timezone information
        assertTrue("Result should contain timezone info: $result", 
            result.contains("[") || result.contains("+") || result.contains("-"))
    }

    @Test
    fun `convertUtcToLocal handles edge cases`() {
        val edgeCases = listOf(
            "1970-01-01T00:00:00Z", // Unix epoch
            "2038-01-19T03:14:07Z", // Near 32-bit timestamp limit
            "2024-02-29T12:00:00Z", // Leap year
            "2024-07-04T12:00:00Z"  // During daylight saving time
        )
        
        edgeCases.forEach { timestamp ->
            val result = convertUtcToLocal(timestamp)
            assertNotNull("Failed for edge case: $timestamp", result)
            assertTrue("Empty result for edge case: $timestamp", result.isNotEmpty())
        }
    }

    @Test
    fun `convertZonedToUtc converts valid zoned timestamp to UTC`() {
        // Create a zoned timestamp
        val zonedTimestamp = "2024-01-15T10:30:00+05:00"
        
        val result = convertZonedToUtc(zonedTimestamp)
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue("Result should end with Z: $result", result.endsWith("Z"))
        // Should be different from input (unless input was already UTC)
        assertNotEquals(zonedTimestamp, result)
    }

    @Test
    fun `convertZonedToUtc handles different timezone formats`() {
        val testCases = listOf(
            "2024-01-15T10:30:00+05:00",
            "2024-01-15T10:30:00-08:00",
            "2024-01-15T10:30:00Z",
            "2024-01-15T10:30:00+00:00",
            "2024-01-15T10:30:00.123+05:30"
        )
        
        testCases.forEach { zonedTimestamp ->
            val result = convertZonedToUtc(zonedTimestamp)
            assertNotNull("Failed for: $zonedTimestamp", result)
            assertTrue("Empty result for: $zonedTimestamp", result.isNotEmpty())
            assertTrue("Should end with Z: $result", result.endsWith("Z"))
        }
    }

    @Test
    fun `convertZonedToUtc returns original string for invalid format`() {
        val invalidTimestamps = listOf(
            "invalid-timestamp",
            "2024-01-15",
            "2024-01-15T10:30:00", // Missing timezone
            "2024-01-15T25:00:00+05:00", // Invalid hour
            "not-a-date",
            "",
            "2024-01-15T10:30:00+25:00" // Invalid timezone offset
        )
        
        invalidTimestamps.forEach { invalidTimestamp ->
            val result = convertZonedToUtc(invalidTimestamp)
            assertEquals("Should return original for: $invalidTimestamp", invalidTimestamp, result)
        }
    }

    @Test
    fun `convertZonedToUtc handles timezone with system default`() {
        // Test with system default timezone format
        val now = ZonedDateTime.now()
        val zonedTimestamp = now.toString()
        
        val result = convertZonedToUtc(zonedTimestamp)
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue("Result should end with Z: $result", result.endsWith("Z"))
    }

    @Test
    fun `convertZonedToUtc handles daylight saving time transitions`() {
        val testCases = listOf(
            "2024-03-10T02:30:00-05:00", // Spring forward in EST
            "2024-11-03T01:30:00-05:00", // Fall back in EST
            "2024-07-15T15:30:00-04:00"  // Summer time
        )
        
        testCases.forEach { zonedTimestamp ->
            val result = convertZonedToUtc(zonedTimestamp)
            assertNotNull("Failed for DST case: $zonedTimestamp", result)
            assertTrue("Empty result for DST case: $zonedTimestamp", result.isNotEmpty())
            assertTrue("Should end with Z: $result", result.endsWith("Z"))
        }
    }

    @Test
    fun `roundtrip conversion maintains time accuracy`() {
        val originalUtc = "2024-01-15T10:30:00Z"
        
        // Convert UTC to local, then back to UTC
        val localTime = convertUtcToLocal(originalUtc)
        val backToUtc = convertZonedToUtc(localTime)
        
        // Parse both timestamps to compare the actual time
        val originalInstant = Instant.parse(originalUtc)
        val roundtripInstant = Instant.parse(backToUtc)
        
        assertEquals("Roundtrip conversion should maintain time accuracy", 
            originalInstant, roundtripInstant)
    }

    @Test
    fun `convertUtcToLocal and convertZonedToUtc handle extreme timezone offsets`() {
        val testCases = listOf(
            "2024-01-15T10:30:00+14:00", // UTC+14 (Line Islands)
            "2024-01-15T10:30:00-12:00", // UTC-12 (Baker Island)
            "2024-01-15T10:30:00+05:45", // UTC+5:45 (Nepal)
            "2024-01-15T10:30:00+13:00"  // UTC+13 (Tonga)
        )
        
        testCases.forEach { zonedTimestamp ->
            val result = convertZonedToUtc(zonedTimestamp)
            assertNotNull("Failed for extreme offset: $zonedTimestamp", result)
            assertTrue("Empty result for extreme offset: $zonedTimestamp", result.isNotEmpty())
            assertTrue("Should end with Z: $result", result.endsWith("Z"))
        }
    }

    @Test
    fun `functions handle leap seconds and edge timestamps`() {
        val edgeCases = listOf(
            "2024-02-29T23:59:59Z", // Leap year last second
            "2024-12-31T23:59:59Z", // Year end
            "2024-01-01T00:00:00Z"  // Year start
        )
        
        edgeCases.forEach { timestamp ->
            val localResult = convertUtcToLocal(timestamp)
            assertNotNull("UTC to local failed for: $timestamp", localResult)
            assertTrue("Empty UTC to local result for: $timestamp", localResult.isNotEmpty())
            
            // If the local conversion worked, try converting back
            if (localResult != timestamp) {
                val utcResult = convertZonedToUtc(localResult)
                assertNotNull("Local to UTC failed for: $localResult", utcResult)
                assertTrue("Empty local to UTC result for: $localResult", utcResult.isNotEmpty())
            }
        }
    }
}
# Implementation Plan

- [x] 1. Create data models for device registration
  - Create DeviceRegistrationRequest data class for API requests
  - Create DeviceInfo data class for local storage
  - Add these models to the existing API models package
  - _Requirements: 3.1, 3.4, 3.5_

- [x] 2. Add device registration API endpoint to DementiaAPI interface
  - Add registerDevice suspend function to DementiaAPI interface
  - Include proper annotations for POST request to /v1/auth/register/device
  - Add Authorization header parameter for authentication
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 3. Create DeviceRegistrationManager class
  - Implement DeviceRegistrationManager class with Context parameter
  - Add getOrCreateDeviceId() method using SharedPreferences and Android ID
  - Add generateDeviceName() method using Build.MODEL and Build.MANUFACTURER
  - Create encrypted SharedPreferences for device info storage
  - _Requirements: 3.1, 3.2, 3.3, 3.5_

- [x] 4. Implement device registration logic in DeviceRegistrationManager
  - Add registerDeviceOnLogin() method that coordinates full registration flow
  - Add updateFCMToken() method for token refresh scenarios
  - Add registerWithBackend() private method with retry logic and error handling
  - Implement exponential backoff retry strategy with maximum 3 attempts
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 4.1, 4.2, 4.3_

- [x] 5. Add API extension functions for device registration
  - Create registerDeviceToken() extension function for DementiaAPI
  - Handle authentication token retrieval and API call execution
  - Add proper error handling and logging for API responses
  - Return boolean success/failure result
  - _Requirements: 1.3, 2.3, 4.4, 4.5_

- [x] 6. Enhance MyFirebaseMessagingService for token updates
  - Modify onNewToken() method to call DeviceRegistrationManager
  - Add authentication state check before attempting registration
  - Add comprehensive error handling and logging
  - Ensure registration runs on background thread
  - _Requirements: 2.1, 2.2, 2.4, 4.1, 4.2_

- [x] 7. Integrate device registration into authentication flows
  - Add device registration call after successful login in authentication code
  - Add device registration call after successful user registration
  - Handle registration failures gracefully without blocking user flow
  - Add logging for successful and failed registration attempts
  - _Requirements: 1.1, 1.2, 1.3, 4.1_

- [ ] 8. Create unit tests for DeviceRegistrationManager
  - Test device ID generation and persistence across app sessions
  - Test device name generation with various device configurations
  - Test retry logic with simulated network failures
  - Test authentication state handling
  - _Requirements: 3.1, 3.2, 3.3, 4.1, 4.2_

- [ ] 9. Create unit tests for API integration
  - Test device registration API endpoint integration
  - Test authentication header handling and token management
  - Test error response parsing and handling
  - Test network failure scenarios
  - _Requirements: 1.3, 2.2, 4.3, 4.4, 4.5_

- [ ] 10. Create integration tests for complete registration flow
  - Test end-to-end device registration during login
  - Test FCM token update flow from Firebase service
  - Test error recovery and retry scenarios
  - Test behavior with authentication failures
  - _Requirements: 1.1, 1.2, 2.1, 2.4_
Yogesh Kumar, 2022593, MC Assignment 02
# Flight Tracking & Analytics App

## Project Overview
An Android application to track real-time flight locations and calculate average flight durations using historical data. Designed for users tracking friends' flights and analyzing travel patterns.



## Key Features

Q1: Real-Time Flight Tracking
- Live flight status monitoring (20-second refresh)
- Interactive UI with flight details and map coordinates
- Input validation & notifications for status changes

Q2: Average Duration Analytics
- Background data collection (3 flights/day)
- Room database storage with delay calculations
- Average duration computation per flight route
- Daily automated sync via WorkManager


## Installation Guide

1. Prerequisites
   - Android Studio Hedgehog or newer
   - Minimum SDK: API 26 (Android 8.0)
   - AviationStack API key (replace placeholder in Config.kt)

2. Configuration
   Update the API key in Config.kt:
   // In Config.kt
   object Config {
       const val SECRET_KEY = "your_aviationstack_key_here"
   }

3. Build & Run
   - Clone the repository
   - Sync Gradle dependencies
   - Build signed APK or run on an emulator/device



## Code Architecture

### Core Components
- CoreActivity.kt: Main controller, UI setup, and event handling (Q1.2, Q1.5, Q2.4)
- FlightDisplay.kt: Manages UI components and updates (Q1.2, Q1.4)
- FlightController.kt: Handles API calls and tracking logic (Q1.1, Q1.3)
- AlertManager.kt: Notification system for flight alerts (Q1.4)

### Data Layer
- FlightStorage.kt: Room database configuration (Q2.1)
- FlightAccess.kt: Database operations (CRUD & queries) (Q2.2, Q2.3)
- FlightRecord.kt: Data model for flight records (Q2.1)
- DataProcessor.kt: Handles average duration calculations (Q2.3)

### Network Layer
- NetworkClient.kt: Retrofit API client setup (Q1.1)
- SkyService.kt: API endpoint definitions (Q1.1)
- SkyDataResponse.kt: JSON parsing models (Q1.3)

### Utilities
- InputValidator.kt: Validates flight codes (Regex: [A-Z]{2}\d{1,4}) (Q1.5)
- TimeFormatter.kt: Date parsing for duration calculations (Q2.3)
- FlightSyncWorker.kt: Background data synchronization (Q2.4)
- Config.kt: API key management (Q1.1)



## Grading Criteria Implementation

### Q1: Real-Time Tracking (30 Marks)
- API Integration: NetworkClient and SkyService handle AviationStack API calls
- UI System: FlightDisplay manages all visual components
- JSON Parsing: SkyDataResponse models with Gson annotations
- Validation: InputValidator ensures valid flight codes (e.g., "AA123")
- Live Updates: 20-second polling via FlightController.scheduleAtFixedRate
- Web View: When given a Flight ID it will also render the webview below displaying the details live and also of previous flights!

### Q2: Analytics System (50 Marks)
- Database Schema: FlightRecord entity with duration/delay fields
- Data Pipeline: FlightSyncWorker collects 3 flights/day automatically
- Average Calculation: computeFlightAverage() in FlightAccess DAO
- Background Jobs: WorkManager scheduling in scheduleDataSync()
- Delay: Calculates delay by the extracting information from .csv file present in the raw directory to extract delay that is used to calculate average time of the flight.
- Result Accuracy: Duration includes delays via actual departure/arrival times



## Usage Demo

1. Track a Flight
   - Enter a valid flight code (e.g., "AA123")
   - View real-time location updates and status

2. Calculate Average Duration
   - Enter a flight code in the average section
   - View computed duration including historical delays

3. Notifications
   - Automatic alerts for status changes (e.g., "Airborne → Landed")



Note: Background sync runs daily after 24 hours (every day). Test flight codes: IX2872, IX235, IX242

Yogesh Kumar, 2022593, MC Assignment 02
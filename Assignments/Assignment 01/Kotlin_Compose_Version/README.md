# TravelGo: KOTLIN + XML - Air Travel Journey Tracker App README
Author
Name: Yogesh Kumar
Roll No: 2022593
1st MC Assignment

## 📌 Introduction
TravelGo is an Android application that tracks air travel journeys with multiple stops, showing route details, visa requirements, distances (km/miles), flight times, and journey progress. The app includes a progress bar, distance covered/left displays, and allows unit switching (km/miles). Stops are loaded from a text file.

---

## 📂 Project Structure and Key Files

📜 Main Files:
- 'MainActivity.kt' : Launch screen with an intro button leading to the journey tracker.
- 'IntroRouteJourney.kt' : Core logic displaying stops, distances, visa requirements, and progress.
- 'StopsAdapter.kt' : RecyclerView adapter for displaying stops in a list with details and highlighting the current stop.
- 'Stop.kt' : Data class for stop information.
- 'activity_main.xml' : Layout for the main screen.
- 'activity_intro_route_journey.xml' : Layout for journey details, including RecyclerView, buttons, and progress bar.
- 'item_stop.xml' : Layout for individual stop items in the list.
- 'stopsdata.txt' : JSON data file with stops, distances, flight times, and visa requirements. (inside travelgo\app\src\main\res\raw)

📜 Support Files:
- 'custom_progress.xml' : Used in progress bar
- 'rounded_button_background.xml' : used in defining graphics of home page
- 'custom_font.xml' and 'custom_heading.xml': used to define different writing patterns

📜 Resources:
- 'stopsdata.txt' : Inside travelgo\app\src\main\res\raw
- 'floryannademo.tff' : Inside travelgo\app\src\main\res\font
- 'roboto_regular.tff' : Inside travelgo\app\src\main\res\font
- 'app_logo' : Inside travelgo\app\src\main\res\drawable 

---

## 📊 Data Format ('stopsdata.txt')
The stops are defined in JSON format with fields:

[
  {
    "SOURCE": "New York",
    "DESTINATION": "Toronto",
    "DISTANCE_KM": 800,
    "DISTANCE_MI": 497,
    "FLIGHT_TIME_HRS": 1.5,
    "VISA_REQUIREMENT": "No Visa Required"
  }
]

- 'SOURCE`: Departure city
- 'DESTINATION': Arrival city
- 'DISTANCE_KM': Distance in kilometers
- 'DISTANCE_MI': Distance in miles
- 'FLIGHT_TIME_HRS': Flight duration
- 'VISA_REQUIREMENT': Transit visa requirement

---

## 💻 Setup Guide
### 1. Import the Project
- Unzip the provided archive.
- Open Android Studio.
- Select **File > Open** and choose the project folder.

### 2. Dependencies
- Ensure `ViewBinding` is enabled in `build.gradle`:

android {
    viewBinding {
        enabled = true
    }
}

- RecyclerView dependency:

dependencies {
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
}

### 3. ▶️Run the Project
- Use a physical Android device or emulator.
- Set up the emulator using AVD (Android Virtual Device) Manager.
- Click Run > Run 'app' ▶️.

### 4. 📜 Resource Files
- Place `stopsdata.txt` in `res/raw/` folder.

### 5. 🧪 Emulator/Device Test
- Confirm proper display of stops, progress bar, and distance switch feature.
---

## ▶️ How to Use the App
1. Launch: Open the app and tap the 'Next' button on the home screen.
2. View Route: The screen shows the journey route and stop details.
3. Switch Units: Tap 'Switch to Miles/Kilometers' to toggle distance units.
4. Advance Stops: Tap 'Next Stop Reached' to highlight the next stop and update progress.
5. Progress Display: Observe distance covered, distance left, and completion percentage.
6. Complete Journey: Continue until all stops are covered and receive a completion notification.
7. APK file Directory: \travelgo\app\build\outputs\apk\debug\app-debug.apk (send it to your phone, give access to install and enjoy!)
---

## ⚙️ Features Overview
- Multiple Stops List: Lazy list for more than three stops.
- Distance Unit Switch: Button to toggle km/miles with instant updates.
- Next Stop Progression: Progress bar and real-time stop highlights.
- JSON File Input: Load stops from `stopsdata.txt`.

## 🚀 Conclusion
The TravelGo app effectively demonstrates Android development with Kotlin, RecyclerView, and ViewBinding. It meets all assignment requirements and can be further enhanced with features like flight duration charts or maps integration.
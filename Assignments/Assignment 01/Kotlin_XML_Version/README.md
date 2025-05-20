# TravelGo: Jetpack Compose - Air Travel Journey Tracker App README
Name: Yogesh Kumar
Roll No: 2022593
1st MC Assignment

## 📌 Project Overview
The TravelGo Android application tracks air travel journeys with multiple stops. It displays route details, distances, flight times, visa requirements, and journey progress. Users can toggle distance units and mark stops as reached.

## 📂 Project Structure
- 'MainActivity.kt': Core logic for displaying stops, progress bar, and user actions.
- 'stopdata.txt' : JSON file containing journey stop details.

## 📊 Data Format (stopdata.txt)
It array with objects containing:

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

## 💻 Setup Instructions
### 1. Prerequisites
- Android Studio (latest version)
- Android Emulator or Physical Android Device

### 2. Import the Project
- Extract the ZIP file.
- Open Android Studio and select File > Open.
- Choose the project folder.

### 3. Check Resource File
- Navigate to 'res' folder in Android Studio.
- Go to a 'raw' directory under 'res' ('res/raw').
- Check `stopdata.txt`.

## ▶️ How to Run
- Connect a physical Android device or launch an emulator.
- In Android Studio, click Run ▶️ or use Shift + F10.
- The app will display the journey with stops and progress.
- Or connect the phone, enable the developer options and directly run into your phone.
- Or by APK, APK file Directory: \travelgo\app\build\outputs\apk\debug\app-debug-androidTest.apk (send it to your phone, give access to install and enjoy!)

## ⚙️ Key Features
- ✅ Switch Distance Units: Toggle between kilometers and miles.
- ✅ Next Stop Button: Updates progress for each reached stop.
- ✅ Journey Progress: Displays total distance covered and remaining.
- ✅ Lazy List: Displays stops in a scrollable list if more than 3.
- ✅ Data from File: Reads stop details from `res/raw/stopdata.json`.

## 📜 Code Explanation (MainActivity.kt)
- 'TravelApp()' : Main Composable with stops, buttons, and progress.
- 'StopItem()' : Displays stop details.
- 'loadStopsFromFile()' : Reads stop data from JSON.
- 'formatDistance()' : Formats distance based on unit.

## 🧪 Testing
- Tested on Android Emulator (API 33) and Pixel 4 Physical Device.
- Validated file reading and progress updates.

## 🚀 Conclusion
This app demonstrates Android development with Jetpack Compose, file I/O, and state management. It meets assignment requirements and offers an intuitive travel-tracking experience.
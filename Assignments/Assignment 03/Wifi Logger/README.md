# WiFi Scanner App - Yogesh Kumar 2022593

## Overview

The WiFi Scanner App is an Android application designed to scan and log WiFi access points (APs) at various locations. It captures key details such as SSID, BSSID, and signal strength (RSSI) over multiple samples. Users can view this data for individual locations and compare signal strengths across different locations, making it a valuable tool for analyzing WiFi coverage or performance in diverse areas.

---

## Project Structure

### Files and Their Roles

Below is a list of all the files in the project along with their purposes:

- `AndroidManifest.xml`  
  Defines the app’s permissions (e.g., WiFi and location access) and registers the app’s activities.

- `AccessPointData.kt`  
  A Kotlin data class representing a WiFi access point, storing its SSID and a list of RSSI samples.

- `LocationData.kt`  
  A Kotlin data class representing a location, containing a name and a map of access points identified by their BSSID.

- `DataManager.kt`  
  A utility object responsible for saving and loading location data to/from internal storage using the Gson library.

- `MainActivity.kt`  
  The main entry point of the app, where users can add new locations, initiate WiFi scans, and access options to view or compare location data.

- `LocationsAdapter.kt`  
  A RecyclerView adapter that displays a list of locations on the main screen, including controls for scanning (Scan, Pause, Cancel).

- `LocationDetailsActivity.kt`  
  An activity that displays detailed information about a specific location, including its scanned access points.

- `AccessPointDataAdapter.kt`  
  A RecyclerView adapter used in LocationDetailsActivity to display access points with details like SSID, BSSID, and signal strength.

- `CompareLocationsActivity.kt`  
  An activity that allows users to compare WiFi data across multiple locations.

- `CompareAdapter.kt`  
  A RecyclerView adapter for CompareLocationsActivity, displaying comparison data for access points across locations.

- `res/layout/activity_main.xml`  
  The layout file for the main screen, featuring a RecyclerView for locations, a floating action button (FAB) to add locations, and a button to compare locations.

- `res/layout/location_item.xml`  
  The layout for each location item in the RecyclerView, including buttons for scanning controls.

- `res/layout/activity_location_details.xml`  
  The layout for the location details screen, with a RecyclerView to list access points.

- `res/layout/access_point_item.xml`  
  The layout for each access point item, showing SSID, BSSID, and signal strength details.

- `res/layout/activity_compare_locations.xml`  
  The layout for the comparison screen, featuring a RecyclerView for comparison data.

- `res/layout/compare_item.xml`  
  The layout for each item in the comparison RecyclerView, displaying SSID, BSSID, and signal strength ranges across locations.

- `res/layout/dialog_input_field.xml`  
  The layout for dialog boxes used to add or rename locations.

- `res/values/strings.xml`  
  Contains string resources, such as the app name and other UI text.

---

## Installation and Setup

Follow these steps to set up and run the WiFi Scanner App on your local machine:

### 1. Clone the Repository

Download or clone the project files into a local directory using Git or by downloading a ZIP file.

### 2. Open in Android Studio

- Launch Android Studio.  
- Select **Open an existing project** from the welcome screen.  
- Navigate to the project directory and click OK.

### 3. Sync Gradle

Android Studio will attempt to sync the project automatically. If it doesn’t, click **Sync Now** in the toolbar or go to **File > Sync Project with Gradle Files**.

### 4. Check Dependencies

Ensure the `build.gradle` file (app module) includes the following dependencies. Add them if they’re missing:

```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
}
```

### 5. Run the App

- Connect an Android device via USB (with developer options and USB debugging enabled) or start an emulator.  
- Click the **Run** button (green triangle) in Android Studio to build and install the app.

---

## How to Use

Here’s how to use the key features of the WiFi Scanner App:

### Add a Location

- On the main screen, click the **floating action button (FAB)**.  
- Enter a name for the location (e.g., "Home") in the dialog box and confirm.

### Scan WiFi at a Location

- In the location list on the main screen, find the desired location and click **Scan**.  
- The app will perform **100 WiFi scans**, collecting data like SSID, BSSID, and RSSI for each access point.  
- Use **Pause** to temporarily halt scanning and **Resume** to continue.  
- Use **Cancel** to stop the scan and discard the current session’s data.

### View Location Details

- Click a location’s name in the list to open its details.  
- View a list of scanned access points, including SSID, BSSID, and the range of signal strengths recorded.  
- Click the **pencil icon** to rename the location if needed.

### Compare Locations

- On the main screen, click the **Compare Locations** button (available if at least two locations have scan data).  
- See a side-by-side comparison of access points across locations, including their signal strength ranges.

---

## Notes

### Permissions

The app requires **location permissions** to scan WiFi networks. Grant these permissions when prompted by the app or in the device settings.

### Data Storage

All location and scan data are stored locally on the device using **Gson** for serialization and Android’s internal storage.

### Future Enhancements

Potential improvements could include:

- Exporting scan data to a file  
- Visualizing signal strengths on a map  
- Adding support for cloud storage
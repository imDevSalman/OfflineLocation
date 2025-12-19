# Offline Location Tracker

An Android application demonstrating offline-first location tracking architecture. This app collects
location data even when offline, persists it locally using Room database, and synchronizes it with a
backend service once connectivity is restored.

## Key Features

* **Offline-First Architecture**: Continues to track and store user location data without an active
  internet connection.
* **Foreground Service Tracking**: Utilizes a foreground service (`LocationTrackingService`) to
  ensure location tracking continues reliably even when the app is in the background.
* **Local Persistence**: Stores location updates in a local Room database.
* **Automatic Synchronization**:
    * **Real-time Sync**: Attempts to sync data immediately when online.
    * **Periodic Sync Guard**: Uses WorkManager (`PeriodicSyncGuardWorker`) to ensure pending data
      is eventually synced (every 15 minutes) if real-time sync fails or is interrupted.
* **Network Connectivity Monitoring**: Detects network state changes to switch between online and
  offline modes.
* **Permission Handling**: Robust handling of location permissions (Granted, Denied, Permanently
  Denied).
* **Modern Android UI**: Built with **Jetpack Compose** and Material 3.

## Architecture

The application is built using the **MVVM (Model-View-ViewModel)** architectural pattern, following
**Clean Architecture** principles to separate concerns and ensure testability.

* **UI Layer**: Composed of Jetpack Compose components (`MainScreen`) and a ViewModel (
  `MainViewModel`). The ViewModel exposes UI state (like online status, pending sync count) and
  handles user interactions using Kotlin Flows.
* **Domain Layer**: Contains business logic and models. It defines the `LocationRepository`
  interface, ensuring that the UI layer interacts with data abstractly without knowing the
  implementation details.
* **Data Layer**: Responsible for sourcing data.
    * **Repository**: `LocationRepositoryImpl` acts as the single source of truth, coordinating
      between local storage (Room), remote API (Retrofit), and system services (Location,
      Connectivity).
    * **Data Sources**:
        * **Local**: Room Database (`LocationDao`) for persisting offline locations.
        * **Remote**: Retrofit service (`LocationApi`) for syncing data to the backend.
        * **System**: `LocationDataSource` for fetching GPS coordinates and `NetworkMonitor` for
          tracking connectivity.

## Offline Storage & Sync Mechanism

### Offline Storage

When the device is offline or tracking is enabled, location data is not sent immediately. Instead:

1. Location updates are collected from the Fused Location Provider.
2. Updates are batched based on a configurable time interval.
3. The batched locations are inserted into a local **Room Database** (`location` table).
4. Each record is marked with `synced = false`.

### Sync Mechanism

The app employs a robust synchronization strategy to ensure data consistency:

1. **Trigger**: Sync is triggered when:
    * Network connectivity is restored (monitored via `NetworkMonitor`).
    * A periodic background job runs (every 15 minutes) via `PeriodicSyncGuardWorker`.
2. **Process** (`LocationSyncEngine`):
    * Queries the local database for records where `synced = false`.
    * Converts these records into a `SyncRequest`.
    * Sends the request to the backend API via `POST /sync`.
3. **Completion**:
    * If the API returns a success status, the local records are marked as `synced = true`.
    * If the API fails, the worker retries with an exponential backoff policy.

## API Contract

The application communicates with a backend service using the following contract:

**Endpoint:** `POST /sync`

**Request Body (`SyncRequest`):**

```json
{
  "logs": [
    {
      "latitude": 37.7749,
      "longitude": -122.4194,
      "accuracy": 10.5,
      "timestamp": 1678886400000,
      "speed": 1.2,
      "employeeId": "emp-123"
    }
    // ... more location objects
  ]
}
```

**Response Body (`SyncResponse`):**

```json
{
  "status": true,
  "message": "Synced successfully"
}
```

## Assumptions & Limitations

* **Employee ID**: The app currently uses a hardcoded `employeeId` constant (`EMPLOYEE_ID`) for all
  location logs. In a production app, this would likely be dynamic based on user login.
* **Backend Availability**: The app assumes the backend is available at the base URL configured in
  the Retrofit module. Sync will retry indefinitely (with backoff) until successful.
* **Battery Optimization**: While `LocationTrackingService` runs as a foreground service, aggressive
  OEM battery optimizations might still kill the service on some devices.
* **Location Accuracy**: The app relies on `FusedLocationProviderClient`. Accuracy depends on device
  hardware and environment (GPS signal, Wi-Fi).

## Tech Stack

* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose
* **Architecture**: MVVM + Clean Architecture
* **Dependency Injection**: Hilt
* **Asynchronous Programming**: Coroutines & Flows
* **Local Database**: Room
* **Background Processing**: WorkManager
* **Location Services**: Google Play Services Location
* **Networking**: Retrofit & OkHttp

## Project Structure

* **`ui`**: Compose screens and ViewModels.
* **`service`**: Foreground service for location tracking.
* **`domain`**: Business logic and models.
* **`data`**: Repositories, Room DB, Retrofit, and Sync logic.
* **`di`**: Hilt modules.

## Getting Started

1. **Clone the repository**: `git clone https://github.com/imDevSalman/OfflineLocation.git`
2. **Open in Android Studio**.
3. **Build the project**.
4. **Run on Device/Emulator**.

## License

This project is open-source and available under the MIT License.

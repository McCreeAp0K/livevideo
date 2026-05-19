# SimpleLiveRoom

SimpleLiveRoom is a compact Android live room demo built with Kotlin, Media3 ExoPlayer, Retrofit, OkHttp WebSocket, and Fresco. It demonstrates how to assemble a basic live streaming room with video playback, room metadata, live comments, viewer count updates, and a lightweight performance-oriented UI flow.

For the Chinese version of this document, see [README.zh-CN.md](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/README.zh-CN.md).

## Overview

This project focuses on a single live room experience and includes:

- DASH live playback with `Media3 ExoPlayer`
- Room metadata loading through REST APIs
- Real-time viewer count and comment updates through WebSocket
- Comment sending through HTTP POST
- Fresco-based avatar loading
- First-frame loading overlay for better perceived startup
- First-frame trace logging for startup timing analysis
- Basic playback lifecycle handling
- UI update throttling and comment batching for smoother rendering

The codebase is intentionally small and easy to read. It is suitable as:

- a learning project for Android live streaming basics
- a prototype for a live room screen
- a reference for integrating playback and real-time updates

## Demo Features

### Playback

- Plays a DASH live stream using `androidx.media3`
- Uses a custom `LoadControl` tuned for faster startup
- Shows a full-screen loading overlay before the first frame is rendered
- Hides the loading overlay only after the first frame is actually rendered
- Starts playback early, then loads room data after the player reaches `STATE_READY`
- Recovers automatically from `behind live window` playback errors
- Pauses in `onPause()` and resumes in `onResume()`

### Room UI

- Displays anchor name, avatar, and follower count
- Displays live viewer count
- Shows a compact scrolling comment area
- Keeps the comment list pinned to the latest message

### Real-Time Data

- Loads initial room metadata and historical comments through REST
- Connects to a WebSocket channel for:
  - live viewer count updates
  - batched live comments
- Sends new comments through an HTTP API

### Performance Improvements Included

- First-frame loading layer to improve perceived startup
- Delayed room data initialization until playback is ready
- Hides loading only after `onRenderedFirstFrame()` to avoid a false-ready black screen
- First-frame tracing across player setup, buffering, rendering, error, and recovery events
- Automatic recovery to the default live position after `behind live window`
- WebSocket callbacks marshalled back to the main thread
- Viewer count throttling to reduce high-frequency UI refreshes
- Comment batching in the `ViewModel` to reduce repeated UI work
- Incremental comment rendering in the activity instead of full list replacement on every update

## Screens and Core Flow

The main screen is implemented in [MainActivity.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/MainActivity.kt) and [activity_main.xml](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/res/layout/activity_main.xml).

The typical startup flow is:

1. Create the player and prepare the live stream.
2. Show the loading overlay while the first frame is pending.
3. Once the player reaches `STATE_READY`, load anchor info and initial comments.
4. Once `onRenderedFirstFrame()` arrives, hide the loading layer.
5. Connect the WebSocket for viewer count and real-time comments.
6. Keep comments auto-scrolled to the bottom as new messages arrive.

## Project Structure

```text
SimpleLiveRoom/
├── app/
│   ├── src/main/java/com/example/simpleliveroom/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   ├── repository/
│   │   │   └── ws/
│   │   ├── model/
│   │   ├── ui/liveroom/
│   │   └── LiveApplication.kt
│   └── src/main/res/
│       ├── layout/
│       └── values/
├── gradle/
├── build.gradle.kts
└── README.md
```

### Important Files

- [MainActivity.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/MainActivity.kt)
  - Hosts the live room UI
  - Creates and manages the player
  - Emits first-frame trace logs
  - Handles first-frame loading and live playback recovery
  - Observes UI state
  - Handles comment list rendering

- [LiveRoomViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/LiveRoomViewModel.kt)
  - Owns room UI state
  - Loads initial data
  - Sends comments
  - Throttles viewer count updates
  - Batches comment updates

- [LiveRoomRepository.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/repository/LiveRoomRepository.kt)
  - Bridges REST APIs and WebSocket

- [WebSocketManager.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/ws/WebSocketManager.kt)
  - Connects to the WebSocket endpoint
  - Parses real-time messages
  - Delivers updates to the UI layer on the main thread

- [ApiService.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/api/ApiService.kt)
  - Defines room REST APIs

- [RetrofitProvider.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/api/RetrofitProvider.kt)
  - Configures the base URL and Gson converter

- [activity_main.xml](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/res/layout/activity_main.xml)
  - Defines the live room layout
  - Includes the player surface, loading overlay, anchor area, viewer count, comments, and input bar

## Architecture

This project follows a lightweight MVVM-style structure:

- `UI layer`
  - `MainActivity`
  - `CommentAdapter`

- `State layer`
  - `LiveRoomUiState`
  - `LiveRoomViewModel`

- `Data layer`
  - `LiveRoomRepository`
  - `ApiService`
  - `RetrofitProvider`
  - `WebSocketManager`

- `Model layer`
  - API response models
  - WebSocket message models

### Data Flow

REST flow:

1. `MainActivity` triggers room initialization through the `ViewModel`.
2. `LiveRoomViewModel` asks `LiveRoomRepository` for anchor info and message history.
3. `LiveRoomRepository` calls Retrofit APIs.
4. Results are mapped into `LiveRoomUiState`.
5. `MainActivity` observes and renders the state.

WebSocket flow:

1. `LiveRoomViewModel` asks the repository to connect.
2. `WebSocketManager` receives live messages.
3. Viewer count and comment updates are posted to the main thread.
4. `LiveRoomViewModel` throttles and batches updates.
5. `MainActivity` renders the latest state.

## Tech Stack

### Language and Build

- Kotlin
- Gradle Kotlin DSL
- Android SDK 36
- Java 11 compatibility

### Android Libraries

- AndroidX AppCompat
- AndroidX ConstraintLayout
- AndroidX RecyclerView
- AndroidX Media3 ExoPlayer

### Networking and Serialization

- Retrofit `2.11.0`
- OkHttp `4.12.0`
- Gson `2.10.1`

### Image Loading

- Fresco `3.4.0`

## Requirements

- Android Studio with Gradle support
- Android device or emulator
- Minimum SDK: `24`
- Target SDK: `36`
- A reachable backend service that provides:
  - room info
  - initial comment history
  - comment posting
  - WebSocket viewer/comment updates
- A reachable DASH live stream URL

## Backend Configuration

The current project is wired to a fixed local network backend:

- REST base URL: `http://10.37.242.55:3000/`
- WebSocket URL: `ws://10.37.242.55:3000/ws/room-viewers?room_id=1001`

These values are currently hard-coded in:

- [RetrofitProvider.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/api/RetrofitProvider.kt)
- [WebSocketManager.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/data/ws/WebSocketManager.kt)
- [LiveRoomViewModel.kt](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/java/com/example/simpleliveroom/ui/liveroom/LiveRoomViewModel.kt)

The app also enables cleartext traffic in [AndroidManifest.xml](file:///Users/bytedance/AndroidStudioProjects/SimpleLiveRoom/app/src/main/AndroidManifest.xml), which is acceptable for local testing but should be replaced by HTTPS and WSS for production use.

## API Expectations

### REST

The app expects endpoints similar to:

- `GET /api/anchor?room_id=1001`
- `GET /api/messages?room_id=1001`
- `POST /api/messages`

### WebSocket

The app expects WebSocket messages with a `type` field and a `data` payload.

Supported message types:

- `room_viewer_count`
- `room_comment_batch`

## How to Run

1. Open the project in Android Studio.
2. Make sure the backend service is reachable from the device or emulator.
3. Update the base URL, stream URL, or room ID if needed.
4. Sync Gradle.
5. Run the `app` module on a device or emulator.

## Current Behavior

When the app launches:

- the video player is prepared first
- a loading overlay is displayed
- room data starts after playback reaches `STATE_READY`
- the loading overlay is hidden after `onRenderedFirstFrame()`
- initial comments and anchor info are loaded
- live comments and viewer count updates are received through WebSocket
- `behind live window` errors trigger a seek-to-live recovery and replay
- the comment list remains pinned to the latest item

## Limitations

This is still a demo-style project, not a production-ready live streaming application.

Notable limitations include:

- hard-coded room ID and stream URL
- hard-coded backend host
- no login or authentication
- no reconnect strategy for WebSocket
- no error-state screen for playback failures
- no landscape/fullscreen mode
- no quality switching
- only basic log-based first-frame tracing, not a full analytics or monitoring stack
- no sensitive word filtering or moderation
- limited automated testing

## What Could Be Added Next

Good next steps for a more production-like version:

- login and token-based authentication
- secure HTTPS and WSS endpoints
- playback retry and weak-network handling
- WebSocket heartbeat and reconnect backoff
- comment moderation and user restrictions
- configurable environments
- metrics for first frame, buffering, crash rate, and message delivery
- `DiffUtil` or `ListAdapter` for more scalable comment rendering
- max comment cache size control
- unit tests and UI tests

## Why This Project Is Useful

Even though the project is intentionally small, it already demonstrates several practical ideas:

- how to combine video playback and real-time data in one screen
- how to shift non-critical work behind first-frame readiness
- how to reduce excessive UI work using throttling and batching
- how to organize a compact Android app using a simple MVVM-style structure

## License

This repository currently does not define a license. Add one before sharing or distributing the project publicly.

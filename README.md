# My Gallery 📱🖼️

My Gallery is a modern offline Android gallery application designed to organize and view photos and videos stored on the device.

The application provides a clean gallery experience with albums, favorites, hidden media, recently deleted items, media viewing, and local data management.

## ✨ Features

- 🖼️ Browse photos stored on the device
- 🎬 Browse videos
- 📁 Album management and album details
- ❤️ Favorites
- 🔒 Hidden Vault for private media
- 🗑️ Recently Deleted section
- 👁️ Full-screen photo viewer
- 🔍 Media browsing and organization
- 📱 Android native experience
- 💾 Offline-first local data management
- ⚡ Fast access to device media using Android MediaStore
- 🗄️ Local database support using Room

## 🛠️ Tech Stack

### Android

- Kotlin
- Android SDK
- Jetpack Compose
- Android MediaStore
- Room Database
- AndroidX
- Gradle

### Architecture & Data

- MVVM architecture
- Repository pattern
- ViewModel
- Room persistence
- Local device media storage
- Android MediaStore API

### Development Tools

- Android Studio
- Git
- GitHub
- Gradle

## 📱 Main Features

### Photos

View photos available on the Android device in a gallery-style interface.

### Videos

Browse and access videos stored on the device.

### Albums

Organize media based on albums and view the contents of individual albums.

### Favorites

Mark important media as favorites for quick access.

### Hidden Vault

Keep selected media inside a separate hidden section within the application.

### Recently Deleted

Manage media that has been moved to the recently deleted section.

### Photo Viewer

Open individual photos in a dedicated full-screen viewer.

### Settings

Manage application-related settings and preferences.

## 🏗️ Architecture

The application follows a structured Android architecture using:

```text
UI
 ↓
ViewModel
 ↓
Repository
 ↓
Local Data Layer
 ↓
Room Database / MediaStore
```

This separation helps keep the UI, business logic, and data operations organized.

## 📂 Project Structure

```text
My Gallery/
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── entity/
│   │   │   │   │   ├── model/
│   │   │   │   │   └── repository/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── screens/
│   │   │   │   │   ├── theme/
│   │   │   │   │   └── viewmodel/
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- Android Studio
- JDK compatible with the project's Gradle configuration
- Android SDK
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/Ashish45188/My-Gallery.git
```

### 2. Open the Project

Open the cloned project in Android Studio.

### 3. Sync Gradle

Allow Android Studio to synchronize the project and download the required dependencies.

### 4. Connect an Android Device

Connect an Android device with USB debugging enabled or start an Android emulator.

### 5. Run the Application

Build and run the application from Android Studio.

## 🔐 Permissions

Because My Gallery accesses media stored on the Android device, the application requires the appropriate Android media permissions.

Depending on the Android version, the application may request permissions for:

- Photos
- Videos
- Media access

Grant the required permissions to allow the gallery to display device media.

## 💾 Offline-First Design

My Gallery is designed primarily for local/offline usage.

The application works with media stored directly on the user's Android device and does not require a cloud backend for its core gallery functionality.

This makes the application suitable for users who want their gallery experience to remain local to their device.

## 🗄️ Local Database

Room Database is used for local application data.

The project contains dedicated:

- DAO classes
- Entity classes
- Database configuration
- Repository layer

This provides structured local persistence for application-managed gallery information.

## 🖼️ Android MediaStore

The application uses Android's MediaStore APIs to access media available on the device.

MediaStore allows the application to discover photos and videos while following Android's media-access framework.

## 🧪 Testing

The project contains Android instrumented tests and unit tests.

Test directories:

```text
app/src/test/
app/src/androidTest/
```

## 🔒 Security & Privacy

My Gallery is designed as an offline gallery application.

The repository does not intentionally include:

- User photos
- User videos
- Private media
- API secrets
- Passwords
- Signing keys
- Local machine configuration

Sensitive local configuration files are excluded through `.gitignore`.

Do not commit private media, credentials, keystores, or other sensitive information to the repository.

## 📸 Screenshots

Screenshots can be added here to demonstrate the application's main interfaces.

Example:

```text
Screenshots
├── Photos
├── Albums
├── Favorites
├── Hidden Vault
├── Recently Deleted
└── Settings
```

## 🎯 Project Goals

The main goals of My Gallery are:

1. Provide a clean Android gallery experience.
2. Allow users to browse photos and videos stored locally.
3. Organize media through albums and categories.
4. Provide favorites and hidden-media functionality.
5. Provide a recently deleted section.
6. Maintain application data locally.
7. Provide a fast and privacy-focused offline experience.

## 🔮 Future Improvements

Potential future improvements include:

- Advanced media search
- Image sorting and filtering
- Improved album management
- Media sharing enhancements
- Better video controls
- Additional privacy features
- Backup and restore options
- Performance improvements for large media libraries
- Additional customization options

## 👨‍💻 Author

**Ashish Mahadev Shedge**

Software Engineer | Android & Full Stack Developer

GitHub: https://github.com/Ashish45188

## 📄 License

This project is currently available for educational and portfolio purposes.

---

⭐ If you find this project useful or interesting, consider giving the repository a star.

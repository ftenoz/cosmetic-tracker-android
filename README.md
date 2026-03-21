# Cosmetic Tracker - Android

Native Android app for tracking cosmetic products, expiry dates, and managing your beauty collection.

## Features

- 📱 **Native Android** - Kotlin + Jetpack Compose + Material 3
- 📸 **Camera Integration** - Product photos & barcode scanning
- 📊 **Barcode Scanner** - ML Kit for automatic product info
- 🎨 **Premium Design** - Based on Google Stitch design system ("The Curated Vanity")
- ☁️ **Cloud Sync** - Real-time sync with backend API
- 🔒 **Secure** - JWT authentication

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM + Repository pattern
- **Networking:** Retrofit + OkHttp
- **Image Loading:** Coil
- **Local Storage:** DataStore
- **Camera:** CameraX
- **Barcode:** ML Kit Barcode Scanning
- **Navigation:** Jetpack Navigation Compose

## Design System

Follows "The Curated Vanity" design philosophy:
- **Primary:** Soft Rose (#994151)
- **Secondary:** Warm Peach (#EF6527)
- **Typography:** Plus Jakarta Sans (display) + Inter (body)
- **Status Colors:** Fresh (green), Expiring (amber), Expired (red)
- **Surface Hierarchy:** Tonal layering, no hard borders
- **Glassmorphism:** Backdrop blur for overlays

## Requirements

- Android SDK 26+ (Android 8.0 Oreo)
- Target SDK 35 (Android 15)
- Kotlin 2.1.0
- Android Studio Ladybug or newer
- JDK 17

## Setup

### 1. Clone Repository
```bash
git clone https://github.com/ftenoz/cosmetic-tracker-android.git
cd cosmetic-tracker-android
```

### 2. Open in Android Studio
- Open Android Studio
- File → Open → Select `cosmetic-tracker-android` folder
- Wait for Gradle sync

### 3. API Configuration
The app connects to:
```
https://cosmetic-tracker-api-1f5360c45085.herokuapp.com
```

Config is in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://cosmetic-tracker-api-1f5360c45085.herokuapp.com\"")
```

### 4. Build & Run
- Connect your Android device or start an emulator
- Click Run (▶️) or `Shift + F10`
- App will install and launch

## Project Structure

```
app/src/main/kotlin/com/cosmetictracker/
├── data/
│   ├── local/           # DataStore (TokenManager)
│   ├── model/           # Data models
│   ├── remote/          # API service, Retrofit client
│   └── repository/      # Repository layer
├── ui/
│   ├── auth/            # Login/Register screens
│   ├── products/        # Products list, Add/Edit
│   ├── profile/         # User profile
│   ├── theme/           # Compose theme (colors, typography)
│   └── components/      # Reusable UI components
├── util/                # Helpers, extensions
├── MainActivity.kt      # Single activity
└── CosmeticTrackerApplication.kt
```

## Screens

1. **Login/Register** - Email/password auth
2. **Dashboard** - Product stats (Total, Active, Expiring Soon)
3. **Products List** - All products with status badges
4. **Add Product** - Camera + barcode scanner + manual entry
5. **Edit Product** - Update dates, notes, image
6. **Profile** - Edit name, email, password

## Features Detail

### Camera & Gallery
- CameraX integration for product photos
- Gallery picker support
- Images uploaded to Cloudinary
- Auto-resize to 800x800

### Barcode Scanner
- ML Kit Barcode Scanning
- Supports: EAN-13, UPC-A, Code 128, QR codes
- Auto-fetch product info from Open Beauty Facts API
- Auto-fill: product name, brand, image

### Status System
- **Fresh** (🟢): Within PAO (Period After Opening)
- **Expiring** (🟡): Last month before expiry
- **Expired** (🔴): Past PAO date

### Offline Support
- Token cached locally (DataStore)
- Graceful error handling
- Retry mechanism

## Building for Release

### 1. Generate Keystore
```bash
keytool -genkey -v -keystore cosmetic-tracker.keystore \
  -alias cosmetictracker -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Configure Signing
Create `keystore.properties` in project root:
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=cosmetictracker
storeFile=../cosmetic-tracker.keystore
```

### 3. Build Release APK
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## API Endpoints Used

- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `GET /users/me` - Get profile
- `PUT /users/me` - Update profile
- `GET /brands` - List brands
- `POST /brands` - Create brand
- `GET /categories` - List categories
- `GET /user-products` - List user products
- `POST /user-products` - Add product
- `PATCH /user-products/:id` - Update product
- `DELETE /user-products/:id` - Delete product
- `POST /upload/image` - Upload image

## Troubleshooting

### Gradle Sync Failed
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Camera Not Working
- Check `AndroidManifest.xml` has `CAMERA` permission
- Ensure device has camera
- Grant permission in app settings

### Barcode Scanner Not Working
- ML Kit downloads models on first use
- Requires internet connection
- Check device has Google Play Services

### API Connection Issues
- Check internet connection
- Verify API_BASE_URL in `build.gradle.kts`
- Check backend server status

## Dependencies

Key libraries and versions in `app/build.gradle.kts`:
- Jetpack Compose BOM 2024.12.01
- Retrofit 2.11.0
- Coil 3.0.4
- CameraX 1.4.1
- ML Kit Barcode 17.3.0
- Navigation Compose 2.8.5
- DataStore 1.1.1

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing`)
5. Open Pull Request

## License

MIT License - see LICENSE file

## Related Projects

- **Web App:** https://github.com/ftenoz/cosmetic-tracker-web
- **Backend API:** https://github.com/ftenoz/cosmetic-tracker-bff

## Support

- **Issues:** https://github.com/ftenoz/cosmetic-tracker-android/issues
- **Email:** ftenoz@gmail.com

---

**Made with ❤️ using Kotlin, Jetpack Compose, and Material 3**

# 🌾 AgriPrice

A modern **Local Crop Price Comparison Android Application** designed to help farmers and users view crop prices, explore agricultural markets, track price trends, manage favorites, set price alerts, and access useful farmer-focused tools from a single mobile platform.

🔗 **Live Demo (MVP)** 

https://gowthamvanjimuthu-cyber.github.io/AgriPrice/

---

## 🎯 Project Goal

To provide a simple, accessible, and farmer-friendly Android application for viewing local crop prices and agricultural market information.

AgriPrice brings **crop price comparison, market exploration, price trends, favorites, price alerts, agricultural updates, and farmer utilities** together in one mobile application powered by Kotlin, Jetpack Compose, Firebase Authentication, and Cloud Firestore.

---

## 🚀 Key Features

* **🔐 Firebase Authentication:** Secure email/password registration, login, authentication state management, and logout functionality.
* **🏠 Home Dashboard:** Centralized dashboard providing quick access to major application features.
* **🌾 Crop Price Comparison:** Browse and compare crop prices across available agricultural markets.
* **🔎 Search & Filtering:** Search crops and markets with sorting and filtering capabilities.
* **🏪 Market Information:** Explore agricultural markets and view the crops and prices available in each market.
* **📈 Price Trends:** Identify rising, falling, and stable crop prices along with average price information.
* **⭐ Favorites:** Save frequently viewed crops for quick and convenient access.
* **🔔 Price Alerts:** Set target prices for crops and save alerts for future monitoring.
* **🧮 Farmer Value Calculator:** Calculate estimated crop value using crop quantity and price per unit.
* **📰 Agriculture Updates:** Access agriculture-related information, updates, and farmer-focused tips.
* **⚙️ Settings:** View application information and manage account logout.
* **📱 Modern Android UI:** Responsive mobile interface built with Kotlin, Jetpack Compose, and Material 3.
* **☁️ Cloud Database:** Store and retrieve crop, market, pricing, and trend information using Cloud Firestore.
* **🔄 Repository-Based Architecture:** Separate data access and application logic for improved maintainability and scalability.

---

## 🔥 Firebase Integration

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Authentication** | Firebase Authentication | Handles user registration, login, authentication state, and logout. |
| **Database** | Cloud Firestore | Stores crop, market, price, and trend information. |
| **Android Integration** | Firebase Android SDK | Connects the Android application with Firebase services. |

### Firestore Crop Data

The application uses a `crops` collection to store crop pricing and market information.

| Field | Example |
| :--- | :--- |
| **name** | Tomato |
| **price** | 32 |
| **unit** | kg |
| **market** | Coimbatore Local Market |
| **trend** | Stable |

Sample crops used during development include **Tomato, Onion, Paddy, and Banana**.

## 🛠️ Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 |
| **IDE** | Android Studio |
| **Authentication** | Firebase Authentication |
| **Database** | Cloud Firestore |
| **Android Integration** | Firebase Android SDK |
| **Build System** | Gradle |
| **Platform** | Android |
| **Architecture** | Repository-Based Android Architecture |

---

## 📱 Application Flow

1. **Login:** Authenticate users using Firebase email and password.
2. **Registration:** Create a new user account through Firebase Authentication.
3. **Home Dashboard:** Access the major AgriPrice features from a centralized dashboard.
4. **Market:** Search, filter, sort, and compare available crop prices.
5. **Crops:** Browse available crops and view detailed crop information.
6. **Price Trends:** Explore rising, falling, and stable crop price trends.
7. **Markets:** Explore agricultural markets and their available crop prices.
8. **Favorites:** Save frequently viewed crops for quick access.
9. **Farmer Tools:** Calculate estimated crop value using quantity and price per unit.
10. **Alerts:** Set and save target-price alerts for selected crops.
11. **Agriculture Updates:** Read agricultural information and farmer-focused tips.
12. **Settings:** View application information and manage account logout.

---

## 📊 Application Modules

| Module | Functionality |
| :--- | :--- |
| **Authentication** | User registration, login, authentication state, and logout |
| **Home** | Central application dashboard |
| **Market** | Crop price comparison and market-based filtering |
| **Crops** | Crop browsing, search, and price information |
| **Price Trends** | Rising, falling, and stable price analysis |
| **Markets** | Agricultural market exploration |
| **Favorites** | Save and manage favorite crops |
| **Farmer Tools** | Crop-value calculation |
| **Alerts** | Target crop-price alert management |
| **Agriculture Updates** | Agricultural information and farmer tips |
| **Settings** | Application information and account management |

---

## 🔧 Setup Instructions

### Prerequisites

- Android Studio
- Android SDK
- JDK
- Git
- Android Emulator or Android Device

### Clone Repository

```bash
git clone https://github.com/gowthamvanjimuthu-cyber/AgriPrice.git
cd AgriPrice
```

### Open Project

1. Open **Android Studio**.
2. Select **Open**.
3. Select the `AgriPrice` project folder.
4. Allow Gradle to complete synchronization.
5. Connect an Android device or start an Android Emulator.
6. Click **Run ▶**.

---

## 🔥 Firebase Setup

### 1. Create Firebase Project

Create a new project using the Firebase Console and add an Android application.

### 2. Configure Android Application

Use the following package name:

```text
com.example.agriprice
```

### 3. Add Firebase Configuration

Download `google-services.json` from Firebase and place it inside:

```text
app/google-services.json
```

### 4. Enable Authentication

Enable the following authentication provider:

```text
Authentication → Sign-in method → Email/Password
```

### 5. Create Firestore Database

Create a **Cloud Firestore** database and add the following collection:

```text
crops
```

Each crop document should contain:

```text
name
price
unit
market
trend
```

### Example Firestore Document

```json
{
  "name": "Tomato",
  "price": 32,
  "unit": "kg",
  "market": "Coimbatore Local Market",
  "trend": "Stable"
}
```

### Sample Crops

- 🍅 **Tomato**
- 🧅 **Onion**
- 🌾 **Paddy**
- 🍌 **Banana**

---

## 🏗️ Project Structure

```text
AgriPrice/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/agriprice/
│   │   │   │   ├── Crop.kt
│   │   │   │   ├── CropRepository.kt
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── RegisterScreen.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── ...
│   │
│   └── build.gradle.kts
│
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

---

## 🔮 Future Improvements

- **🌐 Live Agricultural Market APIs:** Integrate real-time agricultural market pricing services.
- **📍 Nearby Market Discovery:** Find agricultural markets using device location services.
- **📊 Historical Price Charts:** Display historical crop-price movements and long-term trends.
- **🔔 Real-Time Notifications:** Notify farmers when crop prices reach their configured target values.
- **☁️ Cloud-Synchronized Data:** Synchronize favorites and alerts across multiple devices.
- **🌐 Regional Language Support:** Provide the application and agricultural information in regional languages.
- **🤖 AI-Based Crop Price Insights:** Generate intelligent insights and recommendations from historical crop-price data.
- **📷 Crop Image Recognition:** Identify crops using computer vision and image recognition.
- **📱 Production Deployment:** Prepare the application for production release and Play Store deployment.

---

## 👨‍💻 Author

**Gowtham K**

Android Development | Kotlin | Jetpack Compose | Firebase

---

> [!NOTE]
> This is an educational Android application demonstrating mobile application development, Firebase Authentication, Cloud Firestore integration, cloud database usage, and farmer-focused digital tools. AgriPrice demonstrates how mobile technology can make local crop-price and agricultural-market information easier to access through a simple and user-friendly platform.

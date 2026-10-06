# 🌾 AgriPrice — Local Crop Price Comparison App

AgriPrice is an Android application designed to help farmers and users quickly view and compare local crop prices, explore market information, track price trends, and use simple farmer-focused tools.

The application provides crop and market information through a clean, easy-to-use mobile interface powered by **Firebase**.

---

## 📱 Project Overview

Farmers often need quick access to crop prices and market information to make better selling decisions.

AgriPrice brings useful information into one Android application, including:

- 🌾 Local crop prices
- 🏪 Market information
- 📈 Price trends
- ⭐ Favorite crops
- 🔔 Price alerts
- 🧮 Farmer value calculator
- 📰 Agriculture updates
- 🔐 Firebase authentication
- ☁️ Firebase Firestore data storage

The goal is to make crop-price information easier to access through a simple mobile application.

---

## 🎯 Problem Statement

Farmers and agricultural users may need to check crop prices across different markets before making selling decisions.

Information can be difficult to access quickly when it is spread across different sources.

AgriPrice provides a centralized mobile interface where users can:

- View crop prices
- Compare available crops
- Explore markets
- Check price trends
- Set price alerts
- Calculate estimated crop value
- Save favorite crops
- Read agriculture-related updates

---

## 💡 Solution

AgriPrice provides a simple Android application that combines crop pricing, market information, trends, alerts, and farmer utilities in one place.

The application uses **Firebase Authentication** for user login and registration and **Cloud Firestore** for storing and retrieving crop information.

---

## ✨ Features

### 🔐 Authentication

- User registration
- Email and password login
- Firebase Authentication
- Logout functionality
- Persistent authentication session

### 🏠 Home Dashboard

Provides quick access to the main features of the application.

### 📊 Market

Users can:

- View crop prices
- Search crops
- Filter markets
- Sort crop prices
- View crop details
- Check price trends

### 🌾 Crops

Users can:

- Browse available crops
- Search crops
- Filter crop categories
- Sort crops
- View crop details

### ⭐ Favorites

Users can:

- Save favorite crops
- Search favorite crops
- Sort favorites
- View saved crop information

### 📈 Price Trends

Displays:

- Rising crops
- Falling crops
- Stable crops
- Average crop price
- Trend overview
- Price history section

### 🏪 Markets

Users can:

- Search markets
- Explore markets
- View crops available in markets
- View crop prices by market

### 🧮 Farmer Tools

Includes a simple crop-value calculator.

Users can enter:

- Crop quantity
- Price per unit

The application calculates the estimated crop value.

### 🔔 Price Alerts

Users can:

- Select a crop
- Enter a target price
- Create a price alert
- View the saved alert
- Clear the alert

### 📰 Agriculture Updates

Provides:

- Agriculture information
- Latest update cards
- Farmer tips

### ⚙️ Settings

Displays application and configuration information and provides logout functionality.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Jetpack Compose | Modern Android UI |
| Android Studio | Development environment |
| Firebase Authentication | User authentication |
| Cloud Firestore | Crop and market data |
| Gradle | Project build system |
| Material 3 | UI components |

---

## 🏗️ Application Architecture

```text
                    ┌──────────────────────┐
                    │       User           │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   Android App        │
                    │ Kotlin + Compose     │
                    └──────────┬───────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
       ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
       │ Authentication│ │ Crop / Market│ │ Farmer Tools│
       │              │ │ Features     │ │ & Alerts    │
       └──────┬──────┘  └──────┬──────┘  └─────────────┘
              │                │
              ▼                ▼
       ┌─────────────────────────────────┐
       │             Firebase            │
       │                                 │
       │ Firebase Authentication         │
       │ Cloud Firestore                 │
       └─────────────────────────────────┘

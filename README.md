\# ⚡ EcoZap – EV \& CNG Station Locator and Slot Booking App



EcoZap is a mobile application designed to help users locate nearby Electric Vehicle (EV) charging stations and CNG refueling stations. The project aims to simplify the refueling experience by enabling users to discover stations, check availability, and book slots in advance.



\## 🚀 Project Overview



EcoZap aims to reduce waiting times at EV and CNG stations by providing a convenient digital platform for station discovery and slot booking.



The application is built using modern Android development technologies and a Spring Boot backend, with MongoDB for data storage and Redis for caching.



\## ✨ Features



\- 🔍 Discover nearby EV charging and CNG refueling stations.

\- 📍 Explore station locations and details.

\- ⚡ View EV charging and CNG refueling information.

\- 📅 Slot booking for charging and refueling.

\- 👤 User authentication and management.

\- 📱 Modern and responsive Android UI.

\- 🔄 REST API-based frontend and backend communication.



\## 🛠️ Tech Stack



| Component | Technology |

|---|---|

| Frontend | Kotlin, Jetpack Compose |

| UI Design | Material Design 3 |

| Backend | Java, Spring Boot |

| Database | MongoDB Atlas |

| Caching | Redis |

| API | RESTful APIs |

| Build Tools | Gradle, Maven |

| Version Control | Git \& GitHub |

| IDE | Android Studio, IntelliJ IDEA |



\## 🏗️ System Architecture



Android Application (Jetpack Compose)

↓

REST API Communication

↓

Spring Boot Backend

↓

MongoDB Atlas + Redis



The frontend handles user interactions and displays station information, while the backend is designed to manage business logic, user data, station data, and booking operations.



\## 📂 Project Structure



```

EcoZap/

├── app/                     # Android frontend

├── gradle/                  # Android build configuration

├── backend/                 # Spring Boot backend

│   ├── src/

│   │   ├── main/

│   │   │   ├── java/       # Backend source code

│   │   │   └── resources/  # Application configuration

│   │   └── test/           # Backend tests

│   ├── pom.xml

│   └── mvnw.cmd

├── build.gradle.kts

├── settings.gradle.kts

└── README.md

```



## Check Atlas without Docker

Open PowerShell in the repository root with MongoDB Shell (`mongosh`) installed. Set `MONGODB_URI` in this terminal to the same connection used by Spring Boot, with the database name `/EcoZap`. The check reads the database and prints counts.

```powershell
$env:ECOZAP_DB_ACTION = "check"
mongosh --nodb --quiet --file backend/dev/atlas.js
```

To check one reservation, set `$env:ECOZAP_BOOKING_ID = Read-Host "Booking ID from the app"` and repeat the check. After cancellation the same ID should show CANCELLED.

For a demo database missing the Delhi inventory, explicitly import the fixtures from the existing Android JSON:

```powershell
$env:ECOZAP_DB_ACTION = "import"
try { mongosh --nodb --quiet --file backend/dev/atlas.js }
finally { Remove-Item Env:ECOZAP_DB_ACTION }
```

This inserts missing demo IDs (22 stations, 66 slot windows and two vehicle models in an empty database). Existing documents with those IDs keep their values. Bookings and users are never imported or modified. Use the check first if your database already contains stations under other IDs. Redis Cloud is needed when creating a reservation; the Atlas check works independently.

## Run the booking flow locally

From the project root on Windows, start Docker Desktop and run:

```powershell
docker compose up -d
cd backend
.\mvnw.cmd spring-boot:run
```

Use JDK 17 or 21. Open the project root in Android Studio and run the debug app on an emulator. The emulator connects to Spring Boot at `http://10.0.2.2:8081`. For a USB device, use `adb reverse tcp:8081 tcp:8081` and set `ECOZAP_API_URL=http://localhost:8081` in your user Gradle properties. For a hosted API, set that property to the HTTPS origin, without `/api`.

On first startup the local Mongo container imports the existing Android asset `app/src/main/assets/delhi_stations.json`. It uses stable IDs for each station and slot. The sample availability flags stay in the asset; dated availability comes from saved reservations. To import missing demo records into an existing local Compose volume, run `docker compose exec mongo mongosh --quiet /docker-entrypoint-initdb.d/10-ecozap.js`. Existing records are preserved.

1. Open **You**, create an account and add a Demo Electric car or Demo CNG car with a registration number.
2. Open a matching Delhi station, select a future date and an available time, and choose the vehicle. EV bookings also require the current and target energy in kWh.
3. Confirm the booking. Its ID appears after MongoDB saves it. Open **You → My bookings** to view or cancel it.

Bookings use India Standard Time. Stations can still be browsed offline. A station needs a matching name, fuel type and coordinates on the server before reservations are enabled. The local demo is separate from real station operators.

For an existing MongoDB or Redis instance, configure `MONGODB_URI` (including the database name), `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD`, and `REDIS_SSL` as appropriate. Set a random `JWT_SECRET` of at least 32 bytes to keep sessions valid across server restarts. Local development otherwise generates a temporary signing key.

\## 🔮 Future Enhancements



\- Real-time station availability and booking updates.



\- Google Maps integration for navigation.

\- Secure online payments.

\- Booking notifications.

\- Admin dashboard for station management.

\- Personalized station recommendations.



\## 🎯 Project Objective



To build a scalable and user-friendly platform that improves the EV charging and CNG refueling experience while promoting smarter and more sustainable transportation.



\## 👨‍💻 Development



Developed as a Computer Science and Engineering project at KIIT University.



\*\*EcoZap – Charge Smart. Refuel Smarter.\*\*



⭐ If you find this project interesting, consider starring the repository!


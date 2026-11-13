\# ⚡ EcoZap – EV \& CNG Station Locator and Slot Booking App



EcoZap is a mobile application designed to help users locate nearby Electric Vehicle (EV) charging stations and CNG refueling stations. The project aims to simplify the refueling experience by enabling users to discover stations, check availability, and book slots in advance.



\## 🚀 Project Overview



EcoZap aims to reduce waiting times at EV and CNG stations by providing a convenient digital platform for station discovery and slot booking.



The application is built using modern Android development technologies and a Spring Boot backend, with MongoDB for data storage and Redis for caching.



\## ✨ Features



\- 🔍 Discover nearby EV charging and CNG refueling stations.

\- 📍 Explore station locations and details.

\- ⚡ View EV charging and CNG refueling information.

\- 📅 Slot booking for charging and refueling (in development).

\- 👤 User authentication and management (in development).

\- 📱 Modern and responsive Android UI.

\- 🔄 REST API-based frontend and backend communication (integration in progress).



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



\## ⚙️ Getting Started



\### 1. Clone the repository



```

git clone https://github.com/ansh-2612/EcoZap.git

cd EcoZap

```



\### 2. Run the Android frontend



1\. Open the project root in Android Studio.

2\. Sync the Gradle dependencies.

3\. Launch an Android emulator or connect a physical device.

4\. Build and run the application.



\### 3. Run the Spring Boot backend



1\. Open the `backend` folder in IntelliJ IDEA.

2\. Install the required Java version and load the Maven dependencies.

3\. Configure the MongoDB and Redis environment variables:



&#x20;  - `MONGODB\_URI` – MongoDB Atlas connection string

&#x20;  - `REDIS\_PASSWORD` – Redis database password



4\. Run the Spring Boot application.



Alternatively, from the backend directory on Windows:



```

.\\mvnw.cmd spring-boot:run

```



The backend is configured to run at:



```

http://localhost:8081

```



\## 🔐 Security



Database credentials are managed using environment variables and are not included in the repository. Configure your own MongoDB Atlas and Redis credentials before running the backend.



\## 📌 Current Project Status



| Module | Status |

|---|---|

| Android UI | Developed |

| Station Discovery | Implemented |

| Spring Boot Backend | In Development |

| MongoDB Integration | In Development |

| Redis Integration | In Development |

| Frontend–Backend Integration | In Progress |

| Complete Slot Booking System | In Progress |



\## 🔮 Future Enhancements



\- Real-time station availability and booking updates.

\- Complete frontend and backend integration.

\- Google Maps integration for navigation.

\- Secure online payments.

\- Booking history and notifications.

\- Admin dashboard for station management.

\- Personalized station recommendations.



\## 🎯 Project Objective



To build a scalable and user-friendly platform that improves the EV charging and CNG refueling experience while promoting smarter and more sustainable transportation.



\## 👨‍💻 Development



Developed as a Computer Science and Engineering project at KIIT University.



\*\*EcoZap – Charge Smart. Refuel Smarter.\*\*



⭐ If you find this project interesting, consider starring the repository!


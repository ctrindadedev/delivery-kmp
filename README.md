> WORK IN PROGRESS

# Delivery Platform — KMP

A full-stack delivery platform built as an academic project for **DIM0547 — Web II** and **DIM0524 — Mobile** (UFRN/DIMAp, 2026.2). The same domain model — written once in Kotlin Multiplatform — powers both the server and the mobile app, with no duplication.

## 💻 Tech Stack

<div style="display: flex; flex-wrap: wrap; gap: 15px; margin-top: 10px; margin-bottom: 20px;">
  <img align="center" alt="Kotlin" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/kotlin/kotlin-original.svg"/>
  <img align="center" alt="Ktor" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/ktor/ktor-original.svg"/>
  <img align="center" alt="PostgreSQL" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original.svg"/>
  <img align="center" alt="Android" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/android/android-original.svg"/>
  <img align="center" alt="Docker" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg"/>
  <img align="center" alt="Gradle" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/gradle/gradle-original.svg"/>
  <img align="center" alt="Go" height="50" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/go/go-original.svg"/>
</div>

## 📖 About the Repository

This repository contains the source code for both the server (`api/`) and the mobile app (`app/`), built on top of a shared domain module (`shared/`).

The system was designed to solve the core problems of a delivery platform: restaurant discovery, menu browsing and order placement.

## Key Features

### 🏗️ Architecture
- **Kotlin Multiplatform domain:** Business rules defined once in `shared/commonMain` — compiled to JVM for the server and Android for the app. The compiler enforces the boundary.
- **Layered architecture:** Domain → Application → Infrastructure, with dependency inversion throughout.
- **Architecture Decision Records (ADRs):** Every significant decision is documented in `docs/decisoes/` with context, alternatives considered, and consequences.

### 🍽️ Restaurant & Menu (Sprint 1)
- Restaurant registration with address and operating hours.
- Menu management with availability control.
- Business rule enforcement: a restaurant must always have at least one item; prices must be positive.

### 📱 Mobile App (Sprint 2)
- Kotlin Multiplatform + Compose Multiplatform.
- Shared domain imported directly — no duplication.
- Offline cache with stale data indicator.

---

## 📁 Repository structure

```
delivery-kmp/
├── shared/          # Kotlin Multiplatform — pure domain, zero framework
│   └── commonMain/  # Restaurant, MenuItem, Dinheiro, IDs
├── api/             # Ktor server (Kotlin/JVM) — imports shared/
├── services/
│   └── catalogo/    # Go gRPC service — menu cache (read-heavy)
├── docs/
│   └── decisoes/    # ADRs — why each decision was made
└── .github/
    └── workflows/   # GitHub Actions CI
```

## Running the current server setup

Prerequisite: JDK 21, with `JAVA_HOME` pointing to its installation directory.
The Gradle Wrapper downloads Gradle 8.11.1 on its first run; a separate Gradle
installation is not required. Internet access is needed to fetch dependencies.

From the repository root on Windows (PowerShell):

```powershell
.\gradlew.bat build
.\gradlew.bat :shared:jvmTest
.\gradlew.bat :api:run
```

On Linux or macOS:

```sh
./gradlew build
./gradlew :shared:jvmTest
./gradlew :api:run
```

With the server running, `http://localhost:8080/health` should return
`{"status":"ok"}` (JSON whitespace may differ). The current repository does not
yet contain test cases; running a test task alone does not demonstrate coverage.

## 📚 Reference

Professor's reference project: [github.com/fmarquesfilho/musi](https://github.com/fmarquesfilho/musi)

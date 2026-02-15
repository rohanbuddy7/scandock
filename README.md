# 📄 ScanDock

**ScanDock** is an Android document scanner app with **offline editing**, built using **MVVM** and **local storage**.  
Scan documents, enhance them, crop/rotate, and manage everything locally — no internet required.

---

## ✨ Features

- 📸 Document scanning using camera
- ✂️ Crop, rotate, and adjust perspective
- 🎛 Offline image enhancement (brightness, contrast, etc.)
- 🗂 Save scans locally (device storage / local DB)
- 📑 Multi-page document support
- 🔍 Preview and manage saved documents
- ⚡ Fast and lightweight

---

## 📸 Preview

<img width="240" alt="Screenshot 2026-02-16 at 2 15 21 AM" src="https://github.com/user-attachments/assets/b2eac2f9-31fc-42f1-af3b-cab260464513" />
<img width="240" alt="Screenshot 2026-02-16 at 2 15 39 AM" src="https://github.com/user-attachments/assets/bf431346-8411-42ee-80b8-48c337fcf0f9" />
<img width="240" alt="Screenshot 2026-02-16 at 2 15 51 AM" src="https://github.com/user-attachments/assets/ab5e048f-b02d-4e39-a562-95d0a1ba747e" />
<img width="240" alt="Screenshot 2026-02-16 at 2 16 13 AM" src="https://github.com/user-attachments/assets/f6af0993-ac34-46ee-9225-c3c19cb1be39" />
<img width="240" alt="Screenshot 2026-02-16 at 2 16 35 AM" src="https://github.com/user-attachments/assets/a9fafb33-de3c-4b84-9984-ac6970bb395a" />
<img width="240" alt="Screenshot 2026-02-16 at 2 17 02 AM" src="https://github.com/user-attachments/assets/e9c0afb5-d74d-417e-a538-e135981939ad" />

---

## 🧱 Architecture

ScanDock follows **MVVM**:

- **UI (Jetpack Compose / XML)**
- **ViewModel** for state + business logic
- **Repository** for data access
- **Local Storage** (Room / File storage)

---

## 🛠 Tech Stack

- Kotlin
- MVVM Architecture
- Room Database (optional)
- Local File Storage
- Coroutines + Flow / LiveData
- CameraX (for scanning)

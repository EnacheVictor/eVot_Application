# 🗳️ eVot Application

An Android app for managing community associations, announcements, and voting.  
Admins can create associations, post announcements, launch polls, and manage users.  
Residents can join associations, view content, comment, and cast votes easily.

---

## 📸 Screenshots

| Login | Navigation | Create Association |
|-------|------------|--------------------|
| ![Login](screenshots/login.png) | ![Nav](screenshots/nav.png) | ![Create](screenshots/create.png) |

| Announcements | Voting |
|---------------|--------|
| ![Announcements](screenshots/announcements.png) | ![Votes](screenshots/votes.png) |

---

## 🔧 Features

### 👥 User Roles:

#### Admin:
- Create associations
- Generate invitation codes
- Post and manage announcements
- Create and cancel polls
- View vote results

#### Resident:
- Join associations via invite code
- View announcements and post comments
- Vote once in each poll (cannot undo vote)
- Cannot edit or cancel polls

---

## 📦 Built With

- **Kotlin** + Android Jetpack
- **Firebase Authentication**
- **Firebase Firestore**
- ViewPager2 + TabLayout
- RecyclerView + Adapters
- Material Design Components
- Data Binding

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/EnacheVictor/eVot_Application.git
Open it in Android Studio

Create a Firebase project and enable:

🔑 Email/Password Authentication

📄 Cloud Firestore Database

Download your google-services.json and place it in the /app folder

Run the app on an emulator or Android device (API 24+ recommended)

🔐 Example Firestore Rules
js
Copy
Edit
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
✅ You can customize more strict rules based on user roles (admin vs resident)

🤝 Contributing
Contributions are welcome!
Feel free to fork, open issues, or submit a pull request.

👤 Author
Victor Enache
📍 github.com/EnacheVictor

yaml
Copy
Edit


# 🏥 Personal Health Tracker

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=spring)
![SQLite](https://img.shields.io/badge/SQLite-3.44.1-blue?style=flat-square&logo=sqlite)
![JWT](https://img.shields.io/badge/JWT-Auth-black?style=flat-square)

**AI-powered health tracking platform with social features and predictive analytics**

[🌐 Live Demo](https://health-tracker-app-kxgj.onrender.com) 

</div>

---

## 🌐 Live Demo

**🔗 Application URL:** `hhttps://health-tracker-app-kxgj.onrender.com`

**🧪 Test Accounts:**
| Username | Password | Data |
|----------|----------|------|
| `john_doe` | `password123` | 30 days, 7-day streak |
| `jane_smith` | `password123` | 25 days, 5-day streak |
| `bob_wilson` | `password123` | 20 days, 3-day streak |

**🔑 Quick Test:**
```bash
# Login
curl -X POST https://your-replit-url.repl.co/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","password":"password123"}'
````

---

## ✨ Features

### 🎯 Core Features

* **📊 Health Tracking** – Track steps, calories, and meals with daily/weekly goals
* **🔥 Streak System** – Gamified consistency tracking
* **🎯 Goal Management** – Customizable targets with progress monitoring
* **👥 Social Features** – Friend system, leaderboards, activity feeds

### 🤖 AI-Powered Analytics

* **📈 Predictions** – Linear regression for 3-day forecasting (R² confidence scoring)
* **🚨 Anomaly Detection** – Z-score analysis (>2.5σ threshold)
* **💡 Smart Suggestions** – Personalized recommendations
* **📑 Reports** – Monthly analytics with insights

---

## 🛠️ Tech Stack

**Backend:** Java 17, Spring Boot 3.2.0, Spring Security, Spring Data JPA
**Database:** SQLite (embedded)
**Authentication:** JWT (stateless), BCrypt password hashing
**ML/Analytics:** Apache Commons Math (Linear Regression, Statistics)
**Build Tool:** Maven

---

---

## 🧠 AI & Machine Learning Model

Our platform uses a lightweight **AI module** built using `Apache Commons Math` for analytics and predictions.

### 🔍 Model Overview
- **Algorithm:** Linear Regression (Trend Forecasting)
- **Purpose:** Predict next 3 days of activity (steps & calories)
- **Accuracy Metric:** R² Confidence Score (0–100%)
- **Data Used:** Past 14 days of user activity (steps, calories)
- **Library:** Apache Commons Math 3.6.1

### 🧩 Workflow
1. Collect user’s last 14 days of entries  
2. Apply Linear Regression → `y = mx + b`  
3. Generate 3-day predictions (steps & calories)  
4. Calculate confidence score using R²  
5. Detect anomalies using Z-Score (>2.5σ)  
6. Generate smart recommendations based on trends  

### ⚙️ Example Output
```json
{
  "predictions": [
    {"date": "2025-11-13", "steps": 9200, "calories": 2150},
    {"date": "2025-11-14", "steps": 9500, "calories": 2200},
    {"date": "2025-11-15", "steps": 9800, "calories": 2250}
  ],
  "confidence": 87.5
}

```
## 🏗️ Architecture

```
Client → JWT Filter → Controller → Service → Repository → Database
                                ↓
                           MLService (AI & Stats)
```

---

## 🎯 Key Highlights

✅ RESTful API with rich endpoints
✅ JWT-based stateless sessions
✅ AI predictions via regression models
✅ Z-score anomaly detection
✅ Social + gamification elements
✅ Modular, scalable Spring Boot architecture
✅ Sample dataset for demo

---

## 👤 Author

**Simran Bakshi**
🔗 [GitHub @simran-bakshi](https://github.com/simran-bakshi)
📦 [Repository: health-tracker-app](https://github.com/simran-bakshi/health-tracker-app)

---

## 🙏 Acknowledgments

* Spring Boot team
* Apache Commons Math contributors
* All testers and collaborators

---

<div align="center">

**⭐ Star this repo if you find it helpful!**
Made with ❤️ using Spring Boot

</div>





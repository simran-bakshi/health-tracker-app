# Personal Health Tracker

## Overview

A comprehensive personal health tracking application that enables users to monitor daily steps, track calorie intake, analyze health trends, and compete with friends through leaderboards. The system provides AI-powered recommendations, predictive analytics using linear regression, and anomaly detection for health metrics.

**Core Purpose**: Help users maintain healthy habits through data-driven insights, social motivation, and personalized AI suggestions.

**Tech Stack**:
- Backend: Java Spring Boot (REST API)
- Frontend: Vanilla HTML, CSS, JavaScript
- Database: SQLite
- Visualization: Chart.js
- Containerization: Docker

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture

**Single-Page Application (SPA) Pattern**
- Vanilla JavaScript controls all UI interactions without page reloads
- Section-based navigation system with dynamic content switching
- State management through localStorage for authentication tokens and user data
- Chart.js for all data visualizations (steps, calories, predictions, leaderboard)

**UI Component Structure**:
- **Card-based Layout**: Each feature section rendered as a card with consistent padding, shadows, and rounded corners
- **Sidebar Navigation**: Fixed sidebar with dashboard, steps, calories, history, friends/leaderboard, reports, and settings sections
- **Form Validation**: Client-side validation for all inputs (steps ≥ 0, calories ≥ 0, required fields)
- **Toast Notification System**: Positioned top-right for success/error feedback

**Responsive Design Approach**:
- Mobile-first CSS with flexbox/grid layouts
- Gradient background theme (purple tones: #667eea to #764ba2)
- Modern design system with consistent button styling and hover effects

### Backend Architecture

**RESTful API Design**:
- **Layered Architecture**: Controller → Service → Repository pattern
- **Authentication**: Token-based authentication with Bearer tokens
- **Data Access**: Repository pattern with SQLite database

**API Endpoints**:
- `/api/auth/register` - User registration
- `/api/auth/login` - User authentication
- `/api/entries?days=14` - Fetch health entries for specified days
- `/api/entry/{date}` - Get specific date entry
- `/api/entry` - POST to create new entry
- `/api/summary` - Daily summary with AI recommendations
- `/api/predict?days=3` - Predictive analytics for future trends
- `/api/leaderboard` - Friend rankings by activity
- `/api/friends` - POST to add friends
- `/api/friends-activity` - Get friends' activity data
- `/api/targets` - PUT to update health goals
- `/api/monthly-report` - Generate comprehensive monthly analysis

**Machine Learning Features**:
- **Linear Regression**: Predicts future steps/calories for 3-7 day forecasts
- **Z-Score Anomaly Detection**: Identifies unusual spikes or drops in health metrics
- **AI Recommendation Engine**: Generates personalized diet and activity suggestions based on historical patterns

**Data Models**:
- User (id, username, email, displayName, password hash)
- HealthEntry (id, userId, date, steps, calories)
- Friend (userId, friendId)
- Target (userId, dailySteps, weeklySteps, dailyCalories)

### Data Storage

**SQLite Database**:
- Chosen for simplicity and zero-configuration setup
- File-based storage (`healthtracker.db`)
- Supports full CRUD operations through Spring Data JPA
- Includes migration/seed data for testing

**Data Persistence Strategy**:
- ORM mapping via Spring Data JPA
- Repository interfaces for database operations
- Transaction management for data consistency

### Authentication & Authorization

**Token-Based Authentication**:
- Custom Bearer token implementation
- Tokens stored in localStorage on frontend
- Authorization header validation on protected endpoints
- User session management through token lifecycle

**Security Approach**:
- Password hashing (implementation in Spring Security or custom)
- CORS configuration for API access
- Input validation on both client and server sides

### Feature Implementation

**Real-Time Dashboard Updates**:
- After saving steps/meals, dashboard charts update immediately via JavaScript DOM manipulation
- Friends and leaderboard sections refresh dynamically after adding friends
- Progress bars update in real-time based on target completion

**Social Features**:
- Friend search and connection system
- Leaderboard ranking by daily/weekly/monthly activity
- Streak tracking system that updates with daily step entries

**Goal Management**:
- Configurable daily/weekly targets for steps and calories
- Visual progress bars showing target completion
- Smart reminders via toast notifications when targets are not met

**Reporting System**:
- Monthly reports with detailed analytics
- AI-generated health suggestions
- Export functionality for PDF/CSV downloads using jsPDF and PapaParse libraries

## External Dependencies

### Frontend Libraries
- **Chart.js (v4.4.0)**: Data visualization for all health metrics charts
- **jsPDF (v2.5.1)**: PDF report generation
- **PapaParse (v5.4.1)**: CSV export functionality

### Backend Framework
- **Spring Boot**: Core REST API framework
- **Spring Data JPA**: ORM and database access
- **SQLite JDBC Driver**: Database connectivity

### Development & Deployment
- **Docker**: Container runtime for application deployment
- **Docker Compose**: Multi-container orchestration for development environment
- **Maven**: Java dependency management and build tool

### Database
- **SQLite**: Embedded relational database (file: `healthtracker.db`)
- No external database server required
- Includes sample data migrations for testing

### CDN Resources
- Chart.js, jsPDF, and PapaParse loaded via CDN for frontend functionality
- No bundler required for vanilla JavaScript implementation
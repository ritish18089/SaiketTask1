# QuizMaster – Challenge Your Knowledge
A brief description of what this project does and who it's for.

##  🌟 Overview
QuizMaster is an Android-based placement preparation application developed using modern Android technologies and Firebase. The application provides students with an interactive platform to practice aptitude, technical, programming, and general knowledge questions through two different learning modes. The application is designed to help students prepare for campus placements and competitive examinations by providing structured quizzes, detailed explanations, performance tracking, and personalized learning.

## 🚀 Features
### User Authentication
- Email & Password Registration
- Secure Login
- Forgot Password
- Logout
- Firebase Authentication
  
### Home Screen
- Personalized greeting
- Search functionality
- Popular categories
- Quick navigation

### Practice Mode
- 30 MCQs per category
- Instant answer checking
- Detailed explanations
- Unlimited attempts
- Learning-oriented

### Quiz Mode
- Different set of 30 MCQs
- Timer-based quizzes
- Final score calculation
- Result analysis
- Performance tracking

### Categories
- **Aptitude:** NumberSystem,Average,Ratio&Proportion,Calendars,BloodRelation,Coding-Decoding,Seating Arrangement,NumberSeries,Time&Work,Age
- **Technical:** Database,ComputerNetworks,OperatingSystems
- **Programming:** Java,Python,C,React,JavaScript,TypeScript
- **GeneralKnowledge:** Countries,Subjects,CurrentAffairs

### Search
- Search questions
- Search categories
- Fast filtering

### User Profile
- Profile information
- Theme settings
- Quiz statistics
- Performance history

### Dark Mode
- Light Theme
- Dark Theme
- Theme persistence

### Statistics
- Total Quizzes
- Total Questions
- Average Score
- Best Score

### Firebase Integration
- Authentication
- Firestore Database
- User Statistics
- Quiz Results

## Technology Stack

### Frontend
- Kotlin
- Jetpack Compose
- Material Design 3
- Navigation Compose

### Backend
- Firebase

### Database
- **Cloud Firestore:** Collections:users,categories,questions,quizResults

### Libraries
- Jetpack Compose
- Material3
- Firebase Authentication
- Firebase Firestore
- Kotlin Coroutines
- Navigation Compose
- DataStore Preferences

## Architecture

### Presentation Layer
- **Responsible for:** UI Screens,Navigation,User Interaction
- **Examples:** Home Screen,Practice Screen,Quiz Screen,Profile Screen

### ViewModel Layer
- **Responsible for:** Business Logic,State Management,Data Processing
- **Examples:** QuizViewModel,ProfileViewModel

### Repository Layer
- **Responsible for:** Firestore communication,Authentication,CRUD operations

### Firebase Layer
- **Stores:**  Users,Questions,Quiz Results,Statistics

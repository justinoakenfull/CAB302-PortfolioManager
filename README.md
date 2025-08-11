# Portfolio Manager – CAB302 (Software Development, 2025)

## Table of Contents
1. [Overview](#overview)  
2. [Features](#features)  
3. [Technology Stack](#technology-stack)  
4. [Setup & Installation](#setup--installation)  
5. [Usage Guide](#usage-guide)  
6. [Project Structure](#project-structure)  
7. [Team](#team)  

---

## Overview  
The **Portfolio Manager** is a mock desktop application built for CAB302 – Software Development during the semester 2025 (25se1).  
It simulates a stock portfolio platform where users can track investments, test strategies through simulations, and receive AI-generated summaries using **Ollama**.

The app runs as a **JavaFX GUI** program — when compiled and launched, it opens a windowed interface with multiple pages: Dashboard, Watchlist, Simulation, Portfolio, and Login.

---

## Features  
- **Login Page** – Secure user authentication to access personalized content.  
- **Dashboard** – Overview of portfolio performance, recent activity, and key metrics.  
- **Watchlist** – Track selected stocks or assets in real time.  
- **Simulation Module** – Run mock trades and scenarios, with **Ollama-powered** AI summaries to interpret results.  
- **Portfolio Page** – View holdings, analyze performance, and review history.  
- **SQLite Data Storage** – Local persistence for user accounts, portfolios, watchlists, and simulations.  
- **JavaFX Interface** – Styled with **FXML** for structure and **CSS** for theme customization.

---

## Technology Stack  
- **Language**: Java  
- **UI Framework**: JavaFX (FXML + CSS)  
- **Database**: SQLite  
- **AI Integration**: Ollama (for simulation summaries)  
- **Build Tool**: Maven

---

## Setup & Installation

### 1. Prerequisites  
- Java JDK 11+  
- Maven
- SQLite (no separate server required — just the `.db` file)  
- **Ollama** installed and running on your system ([Ollama installation guide](https://ollama.ai/))  

### 2. Clone Repository  
```bash
git clone https://github.com/justinoakenfull/CAB302-PortfolioManager.git
cd CAB302-PortfolioManager
```
### 3. Configure Database
- The application will create the SQLite database on first run, or use the provided database.db file in src/main/resources.
- Ensure the file has write permissions.

### 4. Build & Run Application
```bash
mvn clean install
mvn javafx:run
```
Or run the generated JAR file:
```bash
java -jar target/PortfolioManager-0.1.0.jar
```
When launched, a JavaFX window will open with the login screen.

## Usage Guide
1. Login/Register – Create a new account or log in.
2. Dashboard – View your portfolio summary, recent activity, and quick stats.
3. Watchlist – Add and track assets.
4. Simulation – Run investment scenarios and get Ollama-generated insights.
5. Portfolio – Manage and analyze your holdings.

## Project Structure
```
/
├── src/
│   ├── main/
│   │   ├── java/        # Application logic
│   │   ├── resources/
│   │   │   ├── fxml/    # FXML UI layouts
│   │   │   ├── css/     # Stylesheets
│   │   │   └── database # SQLite DB
│   └── test/            # Tests
├── pom.xml              # Maven build config
└── README.md
```

## Team
Team
- Justin Oakenfull
- Jet Claridge
- William Hocking
- Ramani Singh-Pangly
- Jordan Kitto

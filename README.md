# Fortis Banking System

## Hi there! 👋
This is **Fortis**, a Java-based banking system I built to demonstrate core software engineering principles like **ACID compliance**, **secure transaction processing**, and **clean architecture** without relying on heavy frameworks. 

I wanted to build something that runs purely in the console but feels like a real, robust backend system.

## 🚀 Why I Built This
Most banking tutorials are just simple CRUD apps. I wanted to go deeper.
- **Concurrency**: What happens if two threads try to withdraw money at the same time? (I handled this with synchronized blocks).
- **Data Persistence**: How do we save data without a database? (I implemented a custom CSV-based persistence layer).
- **Architecture**: Separated the code into strictly defined layers (UI, Service, Model, Persistence).

## 🛠️ Key Features
- **Robust CLI**: A menu-driven interface that handles input validation and navigation smoothly.
- **Role-Based Access**:
  - **Admin**: Can create accounts, view immutable audit logs, and manage users.
  - **Customer**: Can deposit, withdraw, transfer funds, and request loans.
- **Simulation Mode**: Features like "Batch processing" animations to simulate real-world delays.
- **SQL Terminal**: I built a mini-SQL engine (`SQLTerminal.java`) that lets you query CSV files using standard `SELECT` syntax.
- **Transaction Safety**: All financial operations are atomic. If a transfer fails halfway, it rolls back.

## 💻 Tech Stack
- **Language**: Core Java (JDK 21+)
- **Storage**: Flat-file CSV (Custom implementation)
- **Interface**: Console/Terminal (with ANSI colors)
- **Scripting**: Windows Batch scripting for automation

## 🏃‍♂️ How to Run
I've made it super easy to get started. You don't need to install anything other than Java.

1. **Double-click** `RUN_FORTIS.bat`
2. That's it! The script compiles the code and launches the app.

**Login Credentials:**
- **Admin**: `admin` / `1234`
- **Customer**: `virat` / `1111`

## 📂 Project Structure
```text
src/
├── model/         # User, BankAccount, TransactionRecord (POJOs)
├── service/       # Business logic (BankingService, LoanService)
├── persistence/   # File handling (AuditLogger, DatabaseManager)
├── ui/            # The CLI handlers (CustomerCommandHandler, etc.)
└── utils/         # Helpers (Security, Colors)
```

---
*Feel free to explore the code! I'm particularly proud of `BankingService.java` where the core transaction logic lives.*

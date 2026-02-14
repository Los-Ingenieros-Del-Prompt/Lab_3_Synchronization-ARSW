# ⚔️ Lab 3: Synchronization - ARSW

> <b>Immortals simulation demonstrating thread synchronization, deadlock prevention, and concurrency control in Java</b>

---

## 📑 Table of Contents

1. [🎯 Project Objective](#-project-objective)
2. [⚡ Main Features](#-main-features)
3. [⚙️ Technologies Used](#️-technologies-used)
4. [🏗️ Architecture](#️-architecture)
5. [🚀 Running the Project](#-running-the-project)
6. [🎮 Running Modes](#-running-modes)
7. [🧪 Testing](#-testing)
8. [📝 Notes on Synchronization](#-notes-on-synchronization)

---

## 🎯 Project Objective

This project simulates a battle arena where immortal fighters continuously engage in combat. The main objective is to demonstrate **thread synchronization** techniques and **deadlock prevention** strategies in a concurrent Java application.

The simulation includes:
- Multiple immortal fighters running as concurrent threads
- Synchronized combat interactions between immortals
- Pause/resume functionality for simulation control
- Health invariant verification to ensure data consistency
- Swing UI for visual monitoring and control

---

## ⚡ Main Features

### 🔹 Immortals Simulation
- **Concurrent Fighters**: Each immortal runs on a separate virtual thread
- **Combat System**: Immortals randomly select opponents and engage in fights
- **Health Management**: Synchronized health updates with damage and healing
- **Fight Modes**: 
  - `ordered`: Prevents deadlock using ordered lock acquisition
  - `naive`: Simple locking strategy (may demonstrate deadlock scenarios)

### 🔹 Synchronization Features
- **Pause/Resume Control**: Safely pause all immortal threads for inspection
- **Health Invariant**: Validates total health conservation across all fights
- **Deadlock Prevention**: Ordered locking strategy to prevent circular wait conditions
- **Thread-Safe Operations**: Proper synchronization for shared resources

### 🔹 Demonstration Modes
- **Deadlock Demo**: Demonstrates potential deadlock scenarios with bank transfers
- **Ordered Transfer Demo**: Shows deadlock prevention using ordered locking
- **TryLock Demo**: Illustrates non-blocking lock acquisition strategies

### 🔹 Swing UI
- **Visual Control Panel**: Real-time monitoring and control interface
- **Statistics Display**: Live updates of fight count, alive immortals, and total health
- **Interactive Controls**: Start, pause, resume, and stop simulation

---

## ⚙️ Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Main programming language with virtual threads |
| **Maven** | - | Dependency management and build |
| **Swing** | - | Graphical user interface |
| **JUnit Jupiter** | 5.10.2 | Unit testing framework |

---

## 🏗️ Architecture

The project is organized in the following packages:

```
src/main/java/edu/eci/arsw/
├── immortals/                     # Core immortals simulation
│   ├── Immortal.java              # Individual fighter thread
│   ├── ImmortalManager.java       # Manages simulation lifecycle
│   └── ScoreBoard.java            # Tracks fight statistics
│
├── concurrency/                   # Synchronization utilities
│   └── PauseController.java       # Pause/resume coordination
│
├── highlandersim/                 # User interface
│   └── ControlFrame.java          # Swing control panel
│
├── core/                          # Supporting examples
│   ├── BankAccount.java           # Account for transfer demos
│   └── TransferService.java       # Transfer logic
│
├── demos/                         # Concurrency demonstrations
│   ├── DeadlockDemo.java          # Deadlock demonstration
│   ├── OrderedTransferDemo.java   # Ordered locking example
│   └── TryLockTransferDemo.java   # TryLock example
│
└── app/
    └── Main.java                  # Application entry point
```

### Main Components

- **Immortal**: Runnable representing a fighter thread with combat logic
- **ImmortalManager**: Orchestrates the simulation with start/stop/pause controls
- **PauseController**: Coordinates thread pausing using CountDownLatch
- **ScoreBoard**: Thread-safe statistics tracking using AtomicLong
- **ControlFrame**: Swing UI for visual monitoring and interaction

---

## 🚀 Running the Project

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Compile the project
```bash
mvn clean compile
```

### Manual
```bash
mvn exec:java \
  -Dexec.mainClass=edu.eci.arsw.app.Main \
  -Dsun.java2d.uiScale=2 \
  -Dmode=ui \
  -Dcount=8 \
  -Dfight=ordered
```

### Run tests
```bash
mvn test
```

### Start the default UI
```bash
mvn exec:java
```

---

## 🎮 Running Modes

### 1. Immortals Simulation (default)
Launches the Swing UI with 8 immortals using ordered fight mode:
```bash
mvn exec:java
```

### 2. Custom Immortals Count
Specify the number of immortals:
```bash
mvn exec:java -Dcount=20
```

### 3. Fight Mode Selection
Choose between `ordered` and `naive` fight modes:
```bash
mvn exec:java -Dfight=naive
```

### 4. Deadlock Demonstration
```bash
mvn exec:java -Dmode=demos -Ddemo=1
```

### 5. Ordered Transfer Demo
```bash
mvn exec:java -Dmode=demos -Ddemo=2
```

### 6. TryLock Transfer Demo
```bash
mvn exec:java -Dmode=demos -Ddemo=3
```

---

## 🧪 Testing

The project includes unit tests to validate:
- ✅ Immortal manager lifecycle operations
- ✅ Thread synchronization correctness
- ✅ Pause/resume functionality
- ✅ Health invariant preservation

### Run all tests
```bash
mvn test
```

---

## 📝 Notes on Synchronization

### Deadlock Prevention Strategy
The `ordered` fight mode prevents deadlock by:
1. Establishing a total ordering of immortals (by name, then identity hash)
2. Always acquiring locks in the same order
3. Eliminating circular wait conditions

### Pause Mechanism
The pause/resume system uses:
- **CountDownLatch**: Ensures all threads reach pause point before inspection
- **Volatile flags**: Safe communication between threads
- **Synchronized blocks**: Protects critical sections during state queries

### Health Invariant
The system maintains the invariant:
```
Total Health = (Initial Health × Population) - (Fights × Net Damage) - Removed Health
```

This validates that no health is lost or gained due to synchronization errors.

### Virtual Threads
The simulation leverages Java 21's virtual threads for efficient concurrency:
- Lightweight thread creation (thousands possible)
- Simplified concurrent programming model
- Automatic scaling with available processors


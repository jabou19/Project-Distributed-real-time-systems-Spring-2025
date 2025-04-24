## Project Structure & Technical Overview

This project simulates and analyzes a real-time ADAS system using polling servers, rate-monotonic (RM), and earliest-deadline-first (EDF) scheduling. It performs schedulability analysis, response time estimation, and compares simulation results to theoretical analysis.

---

### Code Overview: What Each File Does

#### 📁 model/

- `Task.java` — defines a real-time task with period, WCET, priority, etc.
- `Component.java` — a software component that groups tasks, uses RM or EDF, and has a polling server budget/period.
- `Core.java` — a physical CPU core with a speed factor and attached components.

#### 📁 input/

- `TaskLoader.java` — reads `tasks.csv` and builds `Task` objects.
- `ComponentLoader.java` — reads `budgets.csv`, initializes `Component` objects.
- `CoreLoader.java` — reads `architecture.csv`, sets up `Core` objects.

#### 📁 analyzer/

- `Analyzer.java` — performs BDR analysis and WCRT response-time analysis for RM tasks.
  - Outputs `analysis_<testname>.csv`
  - Outputs `rta_analysis_<testname>.csv`
- `Comparison.java` — compares:
  - BDR vs simulation → `comparison_<testname>.csv`
  - RTA WCRT vs simulation → `wcrt_vs_simulation_<testname>.csv`

#### 📁 sim/

- `Simulator.java` — simulates the tasks running under polling server constraints.
  - Tracks job release, budget usage, response time, and deadline misses.
  - Outputs `solution_<testname>.csv`

#### `Main.java`

- The entry point:
  - Loads CSVs
  - Links tasks to components, components to cores
  - Calls: `Simulator`, `Analyzer`, `Comparison`

---

### Output CSVs — What They Contain

| File Name                       | Description                                               |
| ------------------------------- | --------------------------------------------------------- |
| `solution_<test>.csv`           | From Simulator: avg RT, max RT, missed deadlines per task |
| `analysis_<test>.csv`           | From Analyzer: BDR check results per component            |
| `rta_analysis_<test>.csv`       | RTA WCRT values per RM task + schedulability flag         |
| `comparison_<test>.csv`         | Sim schedulable vs Analyzer schedulable per component     |
| `wcrt_vs_simulation_<test>.csv` | Task-wise comparison: WCRT vs Sim max RT, with match flag |

---

### Execution Flow

```plaintext
Main.java
 └── TaskLoader, ComponentLoader, CoreLoader
     └── Link tasks → components → cores
         ├── Simulator.run()         → solution.csv
         ├── Analyzer.run()          → analysis.csv + rta_analysis.csv
         └── Comparison.compare()    → comparison.csv + wcrt_vs_simulation.csv
```

# Real-Time Systems Project: Hierarchical Scheduling Analysis Tool

## 📖 Project Overview

This project focuses on developing a **real-time schedulability analysis tool** for **Advanced Driver Assistance Systems (ADAS)** with hierarchical scheduling structures.

It implements:
- **Worst-Case Response Time (WCRT) analysis**.
- **Budget-based Demand-Resource (BDR) schedulability check**.
- **Simulation of the real-time task system**.
- **Comparison between simulation results and analytical analysis**.
- **Component migration and resource model scaling** (optional extension).

---

## 🎯 Main Features

- **BDR Interface Analysis**: Check if a component is schedulable given a budget (Q) and period (P).
- **Worst-Case Response Time Analysis (RTA)**: Calculate WCRT for RM-scheduled tasks.
- **System Simulation**: Measure runtime response times, detect deadline misses.
- **Validation**:
  - Compare simulation-based WCRT with analytical WCRT.
  - Match schedulability status between simulation and analysis.
- **Optional Extensions**:
  - Migration of components between cores.
  - Budget and WCET rescaling.
- **Structured Outputs** in CSV for easy reporting and visualization.

---

## Project Structure

```plaintext
src/
├── input/              # Loaders for tasks, components, cores (from CSVs)
├── model/              # Core classes: Task, Component, Core
├── sim/                # Discrete event simulation engine
├── analyzer/           # BDR checking, RTA computation, Comparisons
├── utility/            # Migration and scaling utilities
├── Main.java           # Main project entry point
testcases/              # Test case folders with tasks.csv, budgets.csv, architecture.csv


-------

⚙ How to Run?

1. Open terminal inside src/ directory.

2. Compile everything:
javac Main.java

3. Run the main program:
java Main

4. (Optional) Change the test case by modifying this line inside Main.java:
String testFolder = "../testcases/5-huge";
(Change 5-huge to 2-small, 3-medium, etc.)

-------

📂 Input Files

Each test case folder contains:

1. tasks.csv	Task definitions (WCET, Period, Priority, Component).
2. budgets.csv	Component server budgets and periods.
3. architecture.csv	Cores, their speed factors, scheduling type (RM/EDF).

-------

📄 Output Files
Generated automatically inside src folder:

Output File	Meaning
solution_<testcase>.csv	Simulation results: avg/max RT, deadline misses.
analysis_<testcase>.csv	BDR analysis per component.
rta_analysis_<testcase>.csv	RTA (WCRT) results per task (RM only).
comparison_<testcase>.csv	Comparison of simulation vs analysis schedulability.
wcrt_vs_simulation_<testcase>.csv	Detailed WCRT vs Simulated Max RT per task.

--------

📊 Key Features and Extensions Implemented
Feature	Status
BDR Schedulability Checking	✅
Worst-Case Response Time (WCRT) Analysis (RM)	✅
Simulation-Based Validation	✅
WCRT vs Simulation Comparison	✅
Deadline Miss Tracking	✅
Task WCET scaling by Core Speed	✅
Optional Component Migration & Budget Rescaling	✅
Structured CSV Output	✅


------
🛠 System Assumptions

Task WCETs are scaled by core speed (WCET_actual = WCET_nominal / speed_factor).

Component budgets (Q) are taken directly from budgets.csv.

If migration occurs, budgets are rescaled accordingly (Q_new = Q_old * (speed_old / speed_new)).

Periods (T) are not scaled.

Polling server budget renews periodically, no slack stealing.

Both RM and EDF schedulers are supported.


import input.TaskLoader;
import input.ComponentLoader;
import input.CoreLoader;
import model.Task;
import model.Component;
import model.Core;
import sim.Simulator;
import analyzer.Analyzer;
import analyzer.Comparison;
import analyzer.WCRTComparison;
import utility.Migration;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // ONLY CHANGE THIS ONE LINE PER TEST CASE!
           // String testFolder = "../testcases/2-small";
            String testFolder = "files/testcases/4-large"; //
            // Dynamically construct paths
            String taskFile = testFolder + "/tasks.csv";
            String componentFile = testFolder + "/budgets.csv";
            String coreFile = testFolder + "/architecture.csv";

            // Extract test name from folder path
            String[] parts = testFolder.split("/");
            String testName = parts[2];  // e.g., "2-small"

            // === Load data ===
            List<Task> tasks = TaskLoader.loadTasks(taskFile);
            List<Component> components = ComponentLoader.loadComponents(componentFile);
            List<Core> cores = CoreLoader.loadCores(coreFile);

            // === Print loaded tasks ===
            System.out.println("=== Loaded Tasks ===");
            for (Task task : tasks) {
                System.out.println("Task: " + task.name +
                        ", WCET: " + task.wcet +
                        ", Period: " + task.period +
                        ", Component: " + task.componentId +
                        ", Priority: " + task.priority);
            }

            // === Print loaded components ===
            System.out.println("\n=== Loaded Components ===");
            for (Component comp : components) {
                System.out.println("Component: " + comp.id +
                        ", Scheduler: " + comp.scheduler +
                        ", Budget: " + comp.budget +
                        ", Period: " + comp.period +
                        ", Core: " + comp.coreId +
                        ", Priority: " + comp.priority);
            }

            // === Print loaded cores ===
            System.out.println("\n=== Loaded Cores ===");
            for (Core core : cores) {
                System.out.println("Core: " + core.id +
                        ", Speed Factor: " + core.speedFactor +
                        ", Scheduler: " + core.scheduler);
            }

            // === Link tasks to components ===
            for (Task task : tasks) {
                for (Component component : components) {
                    if (task.componentId.equals(component.id)) {
                        component.tasks.add(task);
                        break;
                    }
                }
            }

            // === Link components to cores ===
            for (Component component : components) {
                for (Core core : cores) {
                    if (component.coreId.equals(core.id)) {
                        core.components.add(component);
                        break;
                    }
                }
            }

            // === Print tasks inside each component ===
            System.out.println("\n=== Tasks Inside Each Component ===");
            for (Component comp : components) {
                System.out.println("Component: " + comp.id + " has " + comp.tasks.size() + " task(s):");
                for (Task task : comp.tasks) {
                    System.out.println("  → " + task.name + " (WCET: " + task.wcet + ", Period: " + task.period + ")");
                }
            }

            // === Print components inside each core ===
            System.out.println("\n=== Components Inside Each Core ===");
            for (Core core : cores) {
                System.out.println("Core: " + core.id + " has " + core.components.size() + " component(s):");
                for (Component comp : core.components) {
                    System.out.println("  → " + comp.id + " (Scheduler: " + comp.scheduler + ")");
                }
            }

            // === Perform component migration if needed ===
            Migration.migrateComponents(components, cores);

            // ✅ Run the simulator with dynamic test name
            Simulator simulator = new Simulator();
            simulator.run(cores, 100000, testName);

            Analyzer analyzer = new Analyzer();
            analyzer.run(components, testName);

            Comparison comparison = new Comparison();
            comparison.compare(testName);

            WCRTComparison wcrtCompare = new WCRTComparison();
            wcrtCompare.compare(testName);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

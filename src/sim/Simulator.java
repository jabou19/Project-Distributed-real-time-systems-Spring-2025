package sim;

import model.Task;
import model.Component;
import model.Core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Simulator {

    // Run the simulation for all cores and write results
    public void run(List<Core> cores, double simulationTime, String testName) {
        System.out.println("\n=== SIMULATION START (" + simulationTime + " time units) ===");

        List<String> outputLines = new ArrayList<>();
        outputLines.add("task_name,component_id,task_schedulable,avg_response_time,max_response_time,component_schedulable");

        for (Core core : cores) {
            System.out.println("\n--- Simulating Core: " + core.id + " ---");

            for (Component component : core.components) {
                System.out.println(">>> Component: " + component.id +
                        ", Scheduler: " + component.scheduler +
                        ", Budget: " + component.budget +
                        ", Period: " + component.period);

                List<String> resultLines = simulateComponent(component, simulationTime, core.speedFactor);
                outputLines.addAll(resultLines);
            }
        }

        // Write to CSV
        try (FileWriter writer = new FileWriter("solution_" + testName + ".csv")) {
            for (String line : outputLines) {
                writer.write(line + "\n");
            }
            System.out.println("\n✅ Results written to solution.csv");
        } catch (IOException e) {
            System.out.println("🚨 Error writing to solution.csv: " + e.getMessage());
        }

        System.out.println("\n=== SIMULATION COMPLETE ===");
    }

    // Simulate tasks within a single component
    private List<String> simulateComponent(Component component, double simulationTime, double coreSpeed) {
        List<Task> tasks = component.tasks;
        List<String> outputLines = new ArrayList<>();

        Map<Task, List<Double>> responseTimes = new HashMap<>();
        Map<Task, Double> nextReleaseTime = new HashMap<>();
        Map<Task, Integer> deadlineMisses = new HashMap<>();

        for (Task task : tasks) {
            nextReleaseTime.put(task, 0.0);
            responseTimes.put(task, new ArrayList<>());
            deadlineMisses.put(task, 0);
        }

        double budget = component.budget;
        double period = component.period;
        double currentBudget = budget;
        double budgetResetTime = period;

        double currentTime = 0.0;

        while (currentTime < simulationTime) {
            if (currentTime >= budgetResetTime) {
                currentBudget = budget;
                budgetResetTime += period;
            }

            Task selectedTask = null;
            double minKey = Double.MAX_VALUE;

            for (Task task : tasks) {
                if (currentTime >= nextReleaseTime.get(task)) {
                    double key = component.scheduler.equalsIgnoreCase("RM")
                            ? task.period
                            : nextReleaseTime.get(task); // EDF
                    if (key < minKey) {
                        minKey = key;
                        selectedTask = task;
                    }
                }
            }

            if (selectedTask == null) {
                currentTime += 1;
                continue;
            }

            double execTime = selectedTask.wcet / coreSpeed;
            if (currentBudget >= execTime) {
                double releaseTime = nextReleaseTime.get(selectedTask);
                double deadline = releaseTime + selectedTask.period;
                double finishTime = currentTime + execTime;
                double responseTime = finishTime - releaseTime;

                if (finishTime > deadline) {
                    deadlineMisses.put(selectedTask, deadlineMisses.get(selectedTask) + 1);
                }

                responseTimes.get(selectedTask).add(responseTime);
                currentTime = finishTime;
                currentBudget -= execTime;
                nextReleaseTime.put(selectedTask, releaseTime + selectedTask.period);
            } else {
                currentTime = budgetResetTime;
                currentBudget = budget;
                budgetResetTime += period;
            }
        }

        boolean componentSchedulable = true;

        for (Task task : tasks) {
            List<Double> rtList = responseTimes.get(task);
            double avg = rtList.stream().mapToDouble(d -> d).average().orElse(0.0);
            double max = rtList.stream().mapToDouble(d -> d).max().orElse(0.0);
            int misses = deadlineMisses.get(task);
            boolean schedulable = (misses == 0);

            if (!schedulable) componentSchedulable = false;

            System.out.printf("Task %-10s → Avg RT: %.2f | Max RT: %.2f | Misses: %d\n",
                    task.name, avg, max, misses);

            outputLines.add(String.join(",",
                    task.name,
                    component.id,
                    schedulable ? "1" : "0",
                    String.format("%.2f", avg),
                    String.format("%.2f", max),
                    componentSchedulable ? "1" : "0"
            ));
        }

        return outputLines;
    }
}

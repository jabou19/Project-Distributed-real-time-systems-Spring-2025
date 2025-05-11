package sim;

import model.Component;
import model.Core;
import model.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Simulator {

    public void run(List<Core> cores, double simulationTime, String testName) {
        System.out.println("\n=== SIMULATION START (" + simulationTime + " time units) ===\n");
        // her we are going to simulate the system
        // and write the results to a file CSV
        List<String> lines = new ArrayList<>();
        lines.add("task_name,component_id,task_schedulable,avg_response_time,max_response_time,component_schedulable");
        // Iterate over each core
        for (Core core : cores) {
            System.out.println("--- Simulating Core: " + core.id + " ---");
            // Iterate over each component in the core
            for (Component comp : core.components) {
                System.out.println(">>> Component: " + comp.id + ", Scheduler: " + comp.scheduler + ", Budget: " + comp.budget + ", Period: " + comp.period);

                Map<String, Queue<Job>> taskQueues = new HashMap<>();
                Map<String, List<Double>> responseTimes = new HashMap<>();
                Map<String, Integer> deadlineMisses = new HashMap<>();
                Map<String, Integer> completedJobs = new HashMap<>();
                for (Task task : comp.tasks) {
                    taskQueues.put(task.name, new LinkedList<>());
                    responseTimes.put(task.name, new ArrayList<>());
                    deadlineMisses.put(task.name, 0);
                    completedJobs.put(task.name, 0);
                }

                double budget = comp.budget;
                double period = comp.period;
                double remainingBudget = budget;
                double nextBudgetReset = period;

                for (int t = 0; t <= simulationTime; t++) {
                    if (t == (int) nextBudgetReset) {
                        remainingBudget = budget;
                        nextBudgetReset += period;
                    }
                    for (Task task : comp.tasks) {
                        if (t % task.period == 0) {
                            double adjustedWcet = task.wcet / core.speedFactor;
                            Job job = new Job(task.name, t, adjustedWcet, t + (int) task.period);
                            taskQueues.get(task.name).add(job);
                        }
                    }
                    Task currentTask = selectTask(comp, taskQueues);
                    if (currentTask != null && remainingBudget >= 1.0) {
                        Queue<Job> queue = taskQueues.get(currentTask.name);
                        Job job = queue.peek();
                        if (job != null) {
                            job.remainingTime -= 1.0;
                            remainingBudget -= 1.0;
                            if (job.remainingTime <= 0) {
                                double rt = t - job.releaseTime + 1;
                                responseTimes.get(currentTask.name).add(rt);
                                completedJobs.put(currentTask.name, completedJobs.get(currentTask.name) + 1);
                                queue.poll();
                            }
                        }
                    }
                    // Check if any jobs have missed their deadlines, how ?
                    // by checking if the current time is greater than the job's deadline
                    for (Queue<Job> q : taskQueues.values()) {
                        for (Job job : q) {
                            if (t > job.deadline && !job.countedMiss) {
                                deadlineMisses.put(job.taskName, deadlineMisses.get(job.taskName) + 1);
                                job.countedMiss = true;
                            }
                        }
                    }
                }
                // check if the component is schedulable by checking if all the tasks are schedulable
                boolean componentSchedulable = true;
                for (int misses : deadlineMisses.values()) {
                    if (misses > 0) {
                        componentSchedulable = false;
                        break;
                    }
                }
                for (Task task : comp.tasks) {
                    List<Double> rts = responseTimes.get(task.name);
                    double avg = rts.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double max = rts.stream().mapToDouble(Double::doubleValue).max().orElse(0);
                    int misses = deadlineMisses.get(task.name);

                    System.out.printf("Task %-10s → Avg RT: %.2f | Max RT: %.2f | Misses: %d\n",
                            task.name, avg, max, misses);
                    int taskSchedulable = (misses == 0) ? 1 : 0;

                    // 👇 Locale.US fixes decimal formatting issue
                    lines.add(String.join(",",
                            task.name,
                            comp.id,
                            String.valueOf(taskSchedulable),
                            String.format(Locale.US, "%.2f", avg),
                            String.format(Locale.US, "%.2f", max),
                            componentSchedulable ? "1" : "0")
                    );
                }
            }
        }
        // Write the results to a CSV file
        try (FileWriter writer = new FileWriter("files/Result/Simulator_" + testName + ".csv")) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            System.out.println("✅ Results written to simulator_" + testName + ".csv\n");
        } catch (IOException e) {
            System.out.println("🚨 Error writing simulation results: " + e.getMessage());
        }

        System.out.println("=== SIMULATION COMPLETE ===\n");
    }

    private Task selectTask(Component comp, Map<String, Queue<Job>> taskQueues) {
        return comp.tasks.stream()
                .filter(t -> !taskQueues.get(t.name).isEmpty())
                .sorted((a, b) -> {
                    if (comp.scheduler.equals("RM")) {
                        return Integer.compare(a.priority, b.priority);
                    } else {
                        Job ja = taskQueues.get(a.name).peek();
                        Job jb = taskQueues.get(b.name).peek();
                        return Integer.compare(ja.deadline, jb.deadline);
                    }
                })
                .findFirst().orElse(null);
    }

    static class Job {
        String taskName;// task name
        int releaseTime;// release time means the time when the task is released , it is activation time
        int deadline;
        double remainingTime;
        boolean countedMiss = false;

        public Job(String taskName, int releaseTime, double wcet, int deadline) {
            this.taskName = taskName;
            this.releaseTime = releaseTime;
            this.remainingTime = wcet;
            this.deadline = deadline;
        }
    }
}

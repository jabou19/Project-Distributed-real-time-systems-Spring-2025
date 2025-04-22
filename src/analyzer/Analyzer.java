package analyzer;

import model.Task;
import model.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Analyzer {

    public void run(List<Component> components, String testName) {
        System.out.println("\n=== ANALYSIS TOOL (BDR Check + RTA) ===");

        List<String> lines = new ArrayList<>();
        lines.add("component_id,alpha,delta,lcm,component_schedulable");

        List<String> rtaLines = new ArrayList<>();
        rtaLines.add("task_name,component_id,wcrt,deadline,task_schedulable");

        for (Component comp : components) {
            double alpha = comp.budget / comp.period;
            double delta = comp.period - comp.budget;

            System.out.printf("\nComponent: %s → α = %.3f, Δ = %.3f\n", comp.id, alpha, delta);

            double lcm = lcmOfPeriods(comp.tasks);
            System.out.println("LCM of task periods = " + lcm);

            boolean allPass = true;

            for (int t = 1; t <= lcm; t++) {
                double dbf = 0;

                for (Task task : comp.tasks) {
                    double period = task.period;
                    double wcet = task.wcet;
                    double deadline = period;

                    int jobs = (int) Math.floor((t - deadline) / period) + 1;
                    if (jobs > 0) {
                        dbf += jobs * wcet;
                    }
                }

                double sbf = Math.max(0, alpha * (t - delta));

                if (dbf > sbf) {
                    System.out.printf("❌ Time %d → DBF = %.2f > SBF = %.2f → Not schedulable\n", t, dbf, sbf);
                    allPass = false;
                    break;
                }
            }

            if (allPass) {
                System.out.println("✅ Component " + comp.id + " is schedulable.");
            } else {
                System.out.println("🚨 Component " + comp.id + " is NOT schedulable.");
            }

            String schedulableStr = allPass ? "1" : "0";

            String line = String.join(",",
                    comp.id,
                    String.format(Locale.US, "%.3f", alpha),
                    String.format(Locale.US, "%.3f", delta),
                    String.format(Locale.US, "%.0f", lcm),
                    schedulableStr
            );
            lines.add(line);

            // === RTA Analysis (Fixed-priority only) ===
            if ("RM".equals(comp.scheduler)) {
                for (Task task : comp.tasks) {
                    if (task.priority == null) continue;
                    double wcrt = computeWCRT(task, comp.tasks);
                    boolean schedulable = wcrt != -1;

                    System.out.printf("🔍 RTA - Task %s → WCRT: %.2f, Deadline: %.2f → %s\n",
                            task.name,
                            (wcrt == -1 ? -1 : wcrt),
                            task.period,
                            schedulable ? "Schedulable ✅" : "Not Schedulable ❌");

                    String rtaLine = String.join(",",
                            task.name,
                            comp.id,
                            String.format(Locale.US, "%.2f", (wcrt == -1 ? -1 : wcrt)),
                            String.format(Locale.US, "%.2f", task.period),
                            schedulable ? "1" : "0"
                    );
                    rtaLines.add(rtaLine);
                }
            }
        }

        try (FileWriter writer = new FileWriter("analysis_" + testName + ".csv")) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            System.out.println("\n✅ Analysis results written to analysis_" + testName + ".csv");
        } catch (IOException e) {
            System.out.println("🚨 Error writing analysis CSV: " + e.getMessage());
        }

        try (FileWriter writer = new FileWriter("rta_analysis_" + testName + ".csv")) {
            for (String line : rtaLines) {
                writer.write(line + "\n");
            }
            System.out.println(" RTA results written to rta_analysis_" + testName + ".csv");
        } catch (IOException e) {
            System.out.println("Error writing RTA CSV: " + e.getMessage());
        }
    }

    private double computeWCRT(Task task, List<Task> tasks) {
        List<Task> higherPriority = tasks.stream()
                .filter(t -> t.priority != null && t.priority < task.priority)
                .toList();

        double R = task.wcet;
        double prev = -1;

        while (R != prev) {
            prev = R;
            double interference = 0;
            for (Task hp : higherPriority) {
                interference += Math.ceil(R / hp.period) * hp.wcet;
            }
            R = task.wcet + interference;

            if (R > task.period) return -1;
        }

        return R;
    }

    private double lcmOfPeriods(List<Task> tasks) {
        long lcm = 1;
        for (Task task : tasks) {
            lcm = lcm(lcm, (long) task.period);
        }
        return lcm;
    }

    private long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private long lcm(long a, long b) {
        return a * b / gcd(a, b);
    }
}

package analyzer;

import model.Task;
import model.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Analyzer {

    public void run(List<Component> components, String testName) {
        System.out.println("\n=== ANALYSIS TOOL (BDR Check) ===");

        List<String> lines = new ArrayList<>();
        lines.add("component_id,alpha,delta,lcm,component_schedulable");

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

            String line = String.join(",",
                comp.id,
                String.format("%.3f", alpha),
                String.format("%.3f", delta),
                String.format("%.0f", lcm),
                String.format("%.3f", allPass ? 1.0 : 0.0)  // Use 1.000 or 0.000
            );
            lines.add(line);
        }

        try (FileWriter writer = new FileWriter("analysis_" + testName + ".csv")) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            System.out.println("\n✅ Analysis results written to analysis_" + testName + ".csv");
        } catch (IOException e) {
            System.out.println("🚨 Error writing analysis CSV: " + e.getMessage());
        }
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

//package analyzer;
//
//import model.Task;
//import model.Component;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.*;
//
//public class Analyzer {
//
//    public void run(List<Component> components, String testName) {
//        System.out.println("\n=== ANALYSIS TOOL (BDR Check + RTA Analysis) ===");
//
//        List<String> lines = new ArrayList<>();
//        lines.add("component_id,alpha,delta,lcm,component_schedulable");
//
//        List<String> rtaLines = new ArrayList<>();
//        rtaLines.add("task_name,component_id,wcrt,deadline,task_schedulable");
//
//        for (Component comp : components) {
//            double alpha = comp.budget / comp.period;
//            double delta = comp.period - comp.budget;
//
//            System.out.printf("\nComponent: %s → α = %.3f, Δ = %.3f\n", comp.id, alpha, delta);
//
//            double lcm = lcmOfPeriods(comp.tasks);
//            System.out.println("LCM of task periods = " + lcm);
//
//            boolean allPass = true;
//
////            for (int t = 1; t <= lcm; t++) {
////                double dbf = 0;
////
////                for (Task task : comp.tasks) {
////                    double period = task.period;
////                    double wcet = task.wcet;
////                    double deadline = period;
////
////                    int jobs = (int) Math.floor((t - deadline) / period) + 1;
////                    if (jobs > 0) {
////                        dbf += jobs * wcet;
////                    }
////                }
////
////                double sbf = Math.max(0, alpha * (t - delta));
////
////                if (dbf > sbf) {
////                    System.out.printf(" Time %d → DBF = %.2f > SBF = %.2f → Not schedulable\n", t, dbf, sbf);
////                    allPass = false;
////                    break;
////                }
////            }
//            for (int t = 1; t <= lcm; t++) {
//                boolean schedulableAtT = true;
//
//                if (comp.scheduler.equalsIgnoreCase("EDF")) {
//                    // === EDF: Use standard dbf_EDF
//                    double dbf = 0;
//                    for (Task task : comp.tasks) {
//                        double jobs = Math.floor(t / task.period);
//                        dbf += jobs * task.wcet;
//                    }
//                    double sbf = Math.max(0, alpha * (t - delta));
//                    if (dbf > sbf) {
//                        schedulableAtT = false;
//                    }
//
//                } else if (comp.scheduler.equalsIgnoreCase("RM")) {
//                    // === RM: Use dbf_RM per task with higher-priority interference
//                    for (Task task : comp.tasks) {
//                        if (task.priority == null) continue;
//
//                        double dbf = task.wcet;
//                        for (Task hp : comp.tasks) {
//                            if (hp.priority != null && hp.priority < task.priority) {
//                                dbf += Math.ceil(t / hp.period) * hp.wcet;
//                            }
//                        }
//                        double sbf = Math.max(0, alpha * (t - delta));
//                        if (dbf > sbf) {
//                            System.out.printf(" Time %d → DBF = %.2f > SBF = %.2f → Not schedulable\n", t, dbf, sbf);
//                            schedulableAtT = false;
//                            break;  // No need to check more tasks
//                        }
//                    }
//                }
//
//                if (!schedulableAtT) {
//                    allPass = false;
//                    break;
//                }
//            }
//
//
//            String schedulableStr = allPass ? "Schedulable " : "Not Schedulable ";
//
//            System.out.println("🔹 BDR Result → " + schedulableStr);
//
//            String csvSchedulable = allPass ? "1" : "0";
//
//            // Add to analysis CSV
//            String line = String.join(",",
//                    comp.id,
//                    String.format(Locale.US, "%.3f", alpha),
//                    String.format(Locale.US, "%.3f", delta),
//                    String.format(Locale.US, "%.0f", lcm),
//                    csvSchedulable
//            );
//            lines.add(line);
//
//            // === RTA Analysis (Fixed-Priority Only) ===
//            if ("RM".equalsIgnoreCase(comp.scheduler)) {
//                System.out.println("\n🔍 RTA Analysis for Component: " + comp.id);
//
//                for (Task task : comp.tasks) {
//                    if (task.priority == null) continue;
//
//                    //double wcrt = computeWCRT(task, comp.tasks);
//                    double wcrt = computeBDRWCRT(task, comp.tasks, alpha, delta);
//                    boolean schedulable = wcrt != -1;
//
//                    // Print RTA result in console
//                    System.out.printf("   - Task %-10s | WCRT: %-8.2f | Deadline: %-8.2f | %s\n",
//                            task.name,
//                            (wcrt == -1 ? -1 : wcrt),
//                            task.period,
//                            schedulable ? "Schedulable " : "Not Schedulable ");
//
//                    // Save RTA result into CSV
//                    String rtaLine = String.join(",",
//                            task.name,
//                            comp.id,
//                            String.format(Locale.US, "%.2f", (wcrt == -1 ? -1 : wcrt)),
//                            String.format(Locale.US, "%.2f", task.period),
//                            schedulable ? "1" : "0"
//                    );
//                    rtaLines.add(rtaLine);
//                }
//            }
//        }
//
//        // Write BDR results
//        try (FileWriter writer = new FileWriter("files/Result/analysis_" + testName + ".csv")) {
//            for (String line : lines) {
//                writer.write(line + "\n");
//            }
//            System.out.println("\n BDR Analysis results saved to analysis_" + testName + ".csv");
//        } catch (IOException e) {
//            System.out.println(" Error writing analysis CSV: " + e.getMessage());
//        }
//
//        // Write RTA results
//        try (FileWriter writer = new FileWriter("files/Result/rta_analysis_" + testName + ".csv")) {
//            for (String line : rtaLines) {
//                writer.write(line + "\n");
//            }
//            System.out.println(" RTA results saved to rta_analysis_" + testName + ".csv");
//        } catch (IOException e) {
//            System.out.println(" Error writing RTA CSV: " + e.getMessage());
//        }
//    }
//
////    private double computeWCRT(Task task, List<Task> tasks) {
////        List<Task> higherPriority = tasks.stream()
////                .filter(t -> t.priority != null && t.priority < task.priority)
////                .toList();
////
////        double R = task.wcet;
////        double prev = -1;
////
////        while (R != prev) {
////            prev = R;
////            double interference = 0;
////            for (Task hp : higherPriority) {
////                interference += Math.ceil(R / hp.period) * hp.wcet;
////            }
////            R = task.wcet + interference;
////
////            if (R > task.period) return -1;
////        }
////        return R;
////    }
//private double computeBDRWCRT(Task task, List<Task> tasks, double alpha, double delta) {
//    List<Task> higherPriority = tasks.stream()
//            .filter(t -> t.priority != null && t.priority < task.priority)
//            .toList();
//
//    double R = task.wcet;
//    double prev = -1;
//
//    while (R != prev) {
//        prev = R;
//        double interference = 0;
//        for (Task hp : higherPriority) {
//            interference += Math.ceil(R / hp.period) * hp.wcet;
//        }
//
//        double demand = task.wcet + interference;
//        double supply = alpha * Math.max(0, R - delta); // Apply BDR constraint
//
//        if (supply < demand) {
//            R += 1; // Increase and try again
//        } else {
//            break;
//        }
//
//        if (R > task.period) return -1;
//    }
//
//    return R;
//}
//
//
//    private double lcmOfPeriods(List<Task> tasks) {
//        long lcm = 1;
//        for (Task task : tasks) {
//            lcm = lcm(lcm, (long) task.period);
//        }
//        return lcm;
//    }
//
//    private long gcd(long a, long b) {
//        while (b != 0) {
//            long temp = b;
//            b = a % b;
//            a = temp;
//        }
//        return a;
//    }
//
//    private long lcm(long a, long b) {
//        return a * b / gcd(a, b);
//    }
//}
package analyzer;

import model.Task;
import model.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Analyzer {

    public void run(List<Component> components, String testName) {
        System.out.println("\n=== ANALYSIS TOOL (BDR Check + RTA Analysis) ===");

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
                boolean schedulableAtT = true;

                if (comp.scheduler.equalsIgnoreCase("EDF")) {
                    double dbf = 0;
                    for (Task task : comp.tasks) {
                        double jobs = Math.floor(t / task.period);
                        dbf += jobs * task.wcet;
                    }
                    double sbf = computeSBF(alpha, delta, t);
                    if (dbf > sbf) {
                        schedulableAtT = false;
                    }

                } else if (comp.scheduler.equalsIgnoreCase("RM")) {
                    for (Task task : comp.tasks) {
                        if (task.priority == null) continue;

                        double dbf = task.wcet;
                        for (Task hp : comp.tasks) {
                            if (hp.priority != null && hp.priority < task.priority) {
                                dbf += Math.ceil(t / hp.period) * hp.wcet;
                            }
                        }
                        double sbf = computeSBF(alpha, delta, t);
                        if (dbf > sbf) {
                            System.out.printf(" Time %d → DBF = %.2f > SBF = %.2f → Not schedulable\n", t, dbf, sbf);
                            schedulableAtT = false;
                            break;
                        }
                    }
                }

                if (!schedulableAtT) {
                    allPass = false;
                    break;
                }
            }

            System.out.println("🔹 BDR Result → " + (allPass ? "Schedulable " : "Not Schedulable "));
            String csvSchedulable = allPass ? "1" : "0";

            lines.add(String.join(",",
                    comp.id,
                    String.format(Locale.US, "%.3f", alpha),
                    String.format(Locale.US, "%.3f", delta),
                    String.format(Locale.US, "%.0f", lcm),
                    csvSchedulable
            ));

            if ("RM".equalsIgnoreCase(comp.scheduler)) {
                System.out.println("\n🔍 RTA Analysis for Component: " + comp.id);

                for (Task task : comp.tasks) {
                    if (task.priority == null) continue;

                    double wcrt = computeBDRWCRT(task, comp.tasks, alpha, delta);
                    boolean schedulable = wcrt != -1;

                    System.out.printf("   - Task %-10s | WCRT: %-8.2f | Deadline: %-8.2f | %s\n",
                            task.name,
                            (wcrt == -1 ? -1 : wcrt),
                            task.period,
                            schedulable ? "Schedulable " : "Not Schedulable ");

                    rtaLines.add(String.join(",",
                            task.name,
                            comp.id,
                            String.format(Locale.US, "%.2f", (wcrt == -1 ? -1 : wcrt)),
                            String.format(Locale.US, "%.2f", task.period),
                            schedulable ? "1" : "0"
                    ));
                }
            }
        }

        try (FileWriter writer = new FileWriter("files/Result/analysis_" + testName + ".csv")) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            System.out.println("\n BDR Analysis results saved to analysis_" + testName + ".csv");
        } catch (IOException e) {
            System.out.println(" Error writing analysis CSV: " + e.getMessage());
        }

        try (FileWriter writer = new FileWriter("files/Result/rta_analysis_" + testName + ".csv")) {
            for (String line : rtaLines) {
                writer.write(line + "\n");
            }
            System.out.println(" RTA results saved to rta_analysis_" + testName + ".csv");
        } catch (IOException e) {
            System.out.println(" Error writing RTA CSV: " + e.getMessage());
        }
    }

    private double computeBDRWCRT(Task task, List<Task> tasks, double alpha, double delta) {
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

            double demand = task.wcet + interference;
            double supply = computeSBF(alpha, delta, R);

            if (supply < demand) {
                R += 1;
            } else {
                break;
            }

            if (R > task.period) return -1;
        }

        return R;
    }

    private double computeSBF(double alpha, double delta, double t) {
        return t < delta ? 0.0 : alpha * (t - delta);
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

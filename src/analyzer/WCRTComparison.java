package analyzer;

import java.io.*;
import java.util.*;

public class WCRTComparison {
    public void compare(String testName) throws Exception {
        String rtaFile = "rta_analysis_" + testName + ".csv";
        String simFile = "solution_" + testName + ".csv";
        String outputFile = "wcrt_vs_simulation_" + testName + ".csv";

        Map<String, Double> rtaMap = new HashMap<>();
        Map<String, Double> simMap = new HashMap<>();
        Map<String, String> compMap = new HashMap<>();

        // Read RTA file
        try (BufferedReader br = new BufferedReader(new FileReader(rtaFile))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String taskName = parts[0];
                    String compId = parts[1];
                    double wcrt = Double.parseDouble(parts[2]);
                    rtaMap.put(taskName, wcrt);
                    compMap.put(taskName, compId);
                }
            }
        }

        // Read Simulation file
        try (BufferedReader br = new BufferedReader(new FileReader(simFile))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String taskName = parts[0];
                    double maxRT = Double.parseDouble(parts[3]);
                    simMap.put(taskName, maxRT);
                }
            }
        }

        // Compare and write output
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("task_name,component_id,wcrt,sim_max_rt,match");
            for (String task : rtaMap.keySet()) {
                double wcrt = rtaMap.get(task);
                double simRT = simMap.getOrDefault(task, -1.0);
                String comp = compMap.get(task);
                boolean match = simRT <= wcrt * 1.10; // 10% margin
                writer.printf(Locale.US, "%s,%s,%.2f,%.2f,%s\n",
                        task, comp, wcrt, simRT, match ? "YES" : "NO");
            }
            System.out.println("📊 WCRT vs Simulation comparison written to " + outputFile);
        }
    }
}

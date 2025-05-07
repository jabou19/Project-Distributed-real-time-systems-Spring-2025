package analyzer;

import model.Task;
import model.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class WCRTComparison {
    public void compare(String testName) {
        try {
            System.out.println("\n=== WCRT vs Simulation Comparison ===");

            String rtaFile = "rta_analysis_" + testName + ".csv";
            String simFile = "solution_" + testName + ".csv";

            // Read RTA results
            Map<String, Double> rtaMap = new HashMap<>();
            BufferedReader rtaReader = new BufferedReader(new FileReader(rtaFile));
            String line = rtaReader.readLine(); // skip header
            while ((line = rtaReader.readLine()) != null) {
                String[] parts = line.split(",");
                String taskName = parts[0];
                double wcrt = Double.parseDouble(parts[2]);
                rtaMap.put(taskName, wcrt);
            }
            rtaReader.close();

            // Read Simulation Max RTs
            Map<String, Double> simMap = new HashMap<>();
            BufferedReader simReader = new BufferedReader(new FileReader(simFile));
            line = simReader.readLine(); // skip header
            while ((line = simReader.readLine()) != null) {
                String[] parts = line.split(",");
                String taskName = parts[0];
                double maxRt = Double.parseDouble(parts[2]);
                simMap.put(taskName, maxRt);
            }
            simReader.close();

            List<String> outputLines = new ArrayList<>();
            outputLines.add("task_name,component_id,wcrt,sim_max_rt,match");

            for (String taskName : rtaMap.keySet()) {
                double wcrt = rtaMap.get(taskName);
                double simRt = simMap.getOrDefault(taskName, -1.0);

                String match = "NO";
                if (simRt != -1.0 && wcrt >= 0) {
                    if (simRt <= wcrt * 1.2) { // 20% tolerance
                        match = "YES";
                    }
                }

                // Print to console
                System.out.printf("Task %-10s | Computed WCRT: %-8.2f | Sim Max RT: %-8.2f | Match: %s\n",
                        taskName, wcrt, simRt, match);

                outputLines.add(taskName + ",N/A," + wcrt + "," + simRt + "," + match);
            }

            // Save to CSV
            try (FileWriter writer = new FileWriter("wcrt_vs_simulation_" + testName + ".csv")) {
                for (String outLine : outputLines) {
                    writer.write(outLine + "\n");
                }
            }
            System.out.println(" WCRT vs Simulation comparison saved to wcrt_vs_simulation_" + testName + ".csv");

        } catch (Exception e) {
            System.out.println("🚨 Error during WCRT comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

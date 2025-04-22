package analyzer;

import java.io.*;
import java.util.*;

public class Comparison {

    public void compare(String testName) {
        String solutionFile = "solution_" + testName + ".csv";
        String analysisFile = "analysis_" + testName + ".csv";
        String outputFile = "comparison_" + testName + ".csv";

        Map<String, String> simResults = new HashMap<>();
        Map<String, String> analysisResults = new HashMap<>();

        try {
            // Load simulator results (grouped by component)
            BufferedReader simReader = new BufferedReader(new FileReader(solutionFile));
            simReader.readLine(); // skip header

            Map<String, List<String>> componentToTaskSchedulables = new HashMap<>();
            String line;
            while ((line = simReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 6) continue;

                String compId = tokens[1].trim();
                String taskSchedulable = tokens[2].trim();

                componentToTaskSchedulables
                        .computeIfAbsent(compId, k -> new ArrayList<>())
                        .add(taskSchedulable);
            }
            simReader.close();

            for (Map.Entry<String, List<String>> entry : componentToTaskSchedulables.entrySet()) {
                boolean allOk = entry.getValue().stream().allMatch(s -> s.equals("1") || s.equals("1.000"));
                simResults.put(entry.getKey(), allOk ? "1" : "0");
            }

            // Load analysis results
            BufferedReader aReader = new BufferedReader(new FileReader(analysisFile));
            aReader.readLine(); // skip header
            while ((line = aReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 5) continue;
                String compId = tokens[0].trim();
                String schedulable = tokens[4].trim();

                // Normalize
                if (schedulable.equals("1.000")) schedulable = "1";
                if (schedulable.equals("0.000")) schedulable = "0";
                

                analysisResults.put(compId, schedulable);
            }
            aReader.close();

            // Write and compare
            FileWriter writer = new FileWriter(outputFile);
            writer.write("component_id,sim_schedulable,analysis_schedulable,match\n");

            for (String compId : simResults.keySet()) {
                String sim = simResults.getOrDefault(compId, "N/A");
                String ana = analysisResults.getOrDefault(compId, "N/A");
                String match = sim.equals(ana) ? "YES" : "NO";

                System.out.println(String.join(" | ", compId, sim, ana, match));
                writer.write(String.join(",", compId, sim, ana, match) + "\n");
            }

            writer.close();
            System.out.println("✅ Comparison results saved to " + outputFile);

        } catch (IOException e) {
            System.out.println("🚨 Error comparing results: " + e.getMessage());
        }
    }
}

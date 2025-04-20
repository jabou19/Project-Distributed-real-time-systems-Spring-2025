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
            // === Load simulator results ===
            BufferedReader simReader = new BufferedReader(new FileReader(solutionFile));
            simReader.readLine(); // skip header

            Map<String, List<String>> componentToTaskSchedulables = new HashMap<>();
            String line;
            while ((line = simReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 6) continue;

                String compId = tokens[1];
                String taskSchedulable = tokens[2];

                componentToTaskSchedulables
                        .computeIfAbsent(compId, k -> new ArrayList<>())
                        .add(taskSchedulable);
            }
            simReader.close();

            // Aggregate task schedulability into component-level
            for (Map.Entry<String, List<String>> entry : componentToTaskSchedulables.entrySet()) {
                boolean allOk = entry.getValue().stream().allMatch(s -> s.trim().equals("1"));
                simResults.put(entry.getKey(), allOk ? "1" : "0");
            }

            // === Load analyzer results ===
            BufferedReader aReader = new BufferedReader(new FileReader(analysisFile));
            aReader.readLine(); // skip header

            while ((line = aReader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 5) continue;
                String compId = tokens[0];
                String schedulable = tokens[4].trim(); // should be "1.000" or "0.000"
                analysisResults.put(compId, schedulable);
            }
            aReader.close();

            // === Compare results and write output ===
            FileWriter writer = new FileWriter(outputFile);
            writer.write("component_id,sim_schedulable,analysis_schedulable,match\n");

            for (String compId : simResults.keySet()) {
                String sim = simResults.getOrDefault(compId, "N/A").trim();
                String ana = analysisResults.getOrDefault(compId, "N/A").trim();

                // Use numeric comparison to handle 1 vs 1.000 etc.
                double simVal = Double.parseDouble(sim);
                double anaVal = Double.parseDouble(ana);

                String match = (simVal == anaVal) ? "YES" : "NO";

                // Use original analyzer formatting for display
                String anaDisplay = analysisResults.getOrDefault(compId, ana);
                String resultLine = String.join(",", compId, sim, anaDisplay, match);

                // Print to console
                System.out.println(resultLine);

                // Write to CSV
                writer.write(resultLine + "\n");
            }

            writer.close();
            System.out.println("✅ Comparison results saved to " + outputFile);

        } catch (IOException | NumberFormatException e) {
            System.out.println("🚨 Error comparing results: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

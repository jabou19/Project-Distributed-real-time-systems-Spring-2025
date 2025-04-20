package input;

import model.Task;
import java.io.*;
import java.util.*;

public class TaskLoader {
    public static List<Task> loadTasks(String filePath) throws IOException {
        List<Task> tasks = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // skip header

        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");

            String name = tokens[0];
            double wcet = Double.parseDouble(tokens[1]);
            double period = Double.parseDouble(tokens[2]);
            String componentId = tokens[3];

            Integer priority = null;
            if (tokens.length >= 5 && !tokens[4].isEmpty()) {
                priority = Integer.parseInt(tokens[4]);
            }

            tasks.add(new Task(name, wcet, period, componentId, priority));
        }

        br.close();
        return tasks;
    }
}

package input;

import model.Component;
import java.io.*;
import java.util.*;

public class ComponentLoader {
    public static List<Component> loadComponents(String filePath) throws IOException {
        List<Component> components = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // skip header

        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");

            String id = tokens[0];
            String scheduler = tokens[1];
            double budget = Double.parseDouble(tokens[2]);
            double period = Double.parseDouble(tokens[3]);
            String coreId = tokens[4];

            Integer priority = null;
            if (tokens.length >= 6 && !tokens[5].isEmpty()) {
                priority = Integer.parseInt(tokens[5]);
            }

            components.add(new Component(id, scheduler, budget, period, coreId, priority));
        }

        br.close();
        return components;
    }
}

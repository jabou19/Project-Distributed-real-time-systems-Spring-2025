package input;

import model.Core;
import java.io.*;
import java.util.*;

public class CoreLoader {
    public static List<Core> loadCores(String filePath) throws IOException {
        List<Core> cores = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // skip header

        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            String id = tokens[0];
            double speedFactor = Double.parseDouble(tokens[1]);
            String scheduler = tokens[2];

            cores.add(new Core(id, speedFactor, scheduler));
        }

        br.close();
        return cores;
    }
}

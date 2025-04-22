package model;

import java.util.ArrayList;
import java.util.List;

public class Core {
    public String id;
    public double speedFactor;
    public String scheduler;

    public List<Component> components = new ArrayList<>();

    public Core(String id, double speedFactor, String scheduler) {
        this.id = id;
        this.speedFactor = speedFactor;
        this.scheduler = scheduler;
    }
}

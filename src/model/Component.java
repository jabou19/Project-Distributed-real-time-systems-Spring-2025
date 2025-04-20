package model;

import java.util.ArrayList;
import java.util.List;

public class Component {
    public String id;
    public String scheduler;
    public double budget;
    public double period;
    public String coreId;
    public Integer priority;
    public List<Task> tasks = new ArrayList<>();

    public Component(String id, String scheduler, double budget, double period, String coreId, Integer priority) {
        this.id = id;
        this.scheduler = scheduler;
        this.budget = budget;
        this.period = period;
        this.coreId = coreId;
        this.priority = priority;
    }
}

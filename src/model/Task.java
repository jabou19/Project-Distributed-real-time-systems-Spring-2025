package model;

public class Task {
    public String name;
    public double wcet;
    public double period;
    public String componentId;
    public Integer priority;

    public Task(String name, double wcet, double period, String componentId, Integer priority) {
        this.name = name;
        this.wcet = wcet;
        this.period = period;
        this.componentId = componentId;
        this.priority = priority;
    }
}

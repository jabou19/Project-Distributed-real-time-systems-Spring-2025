package utility;

import model.Component;
import model.Core;

import java.util.List;

public class Migration {
    public static void migrateComponents(List<Component> components, List<Core> cores) {
        System.out.println("\n=== MIGRATION MODULE ===");

        for (Component component : components) {
            for (Core core : cores) {
                if (core.speedFactor > 1.0) {
                    System.out.printf("Moving component %s from core %s to core %s\n",
                        component.id, component.coreId, core.id);

                    // Adjust budget when moving to new core
                    Core oldCore = findCoreById(cores, component.coreId);
                    if (oldCore != null) {
                        double oldSpeed = oldCore.speedFactor;
                        double newSpeed = core.speedFactor;
                        double oldBudget = component.budget;
                        double newBudget = oldBudget * (oldSpeed / newSpeed);

                        component.budget = newBudget;
                        component.coreId = core.id;
                        break;
                    }
                }
            }
        }
        System.out.println("✅ Migration completed!");
    }

    private static Core findCoreById(List<Core> cores, String id) {
        for (Core core : cores) {
            if (core.id.equals(id)) {
                return core;
            }
        }
        return null;
    }
}

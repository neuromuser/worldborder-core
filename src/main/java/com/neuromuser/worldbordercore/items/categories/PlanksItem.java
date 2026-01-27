package com.neuromuser.worldbordercore.items.categories;

import com.neuromuser.worldbordercore.items.RolledItem;
import com.neuromuser.worldbordercore.items.WorldRollContext;
import net.minecraft.item.Item;

public class PlanksItem extends RolledItem {
    private final Item logItem;

    public PlanksItem(Item planksItem, Item logItem, double rarity) {
        super(planksItem, rarity, 0, true, false);  // requiresWorldScan = false
        this.logItem = logItem;
    }

    @Override
    protected boolean checkDependencies(WorldRollContext context) {
        return context.hasItem(logItem);
    }

    @Override
    protected double calculateBaseCount(WorldRollContext context) {
        double A = 100.0;
        double B = 2.5;
        double C = 1.3;
        double baseCount = A / (1.0 + B * Math.pow(getRarity(), C));

        return Math.max(1.0, Math.round(baseCount * 10.0) / 10.0) * 4.0;
    }
}
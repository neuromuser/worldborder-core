package com.neuromuser.worldbordercore.items;

import com.neuromuser.worldbordercore.config.Config;
import com.neuromuser.worldbordercore.config.ConfigManager;
import net.minecraft.item.Item;

public abstract class RolledItem {

    protected final Item minecraftItem;
    protected final double rarity;
    protected final double countMultiplier;
    protected final int minBorderSize;
    protected final boolean renewable;
    protected final boolean requiresWorldScan;

    protected RolledItem(Item item, double rarity, double countMultiplier, int minBorderSize,
                         boolean renewable, boolean requiresWorldScan) {
        this.minecraftItem = item;
        this.rarity = rarity;
        this.countMultiplier = countMultiplier;
        this.minBorderSize = minBorderSize;
        this.renewable = renewable;
        this.requiresWorldScan = requiresWorldScan;
    }

    public boolean canRoll(WorldRollContext context) {
        if (context.getBorderSize() < minBorderSize) {
            return false;
        }



        if (requiresWorldScan) {
            Integer count = context.getScannedResources().get(minecraftItem);
            if (count == null || count <= 5) {
                return false;
            }
        }

        return checkDependencies(context);
    }

    protected boolean checkDependencies(WorldRollContext context) {
        return true;
    }

    public int calculateRequiredCount(WorldRollContext context) {
        double baseCount = calculateBaseCount();
        double progressionFactor = calculateProgressionFactor(context);
        double renewableFactor = calculateRenewableFactor(context);
        double stageFactor = calculateStageFactor();

        double finalCount = baseCount * progressionFactor * renewableFactor * stageFactor;

        if (renewable) {
            Config config = ConfigManager.get();
            finalCount *= (1.0 + (Math.random() * config.randomnessVariation * 2 - config.randomnessVariation));
        }

        finalCount = applyCountCaps(finalCount);

        return Math.max(1, (int) Math.round(finalCount));
    }

    protected double calculateBaseCount() {
        double A = 100.0;
        double B = 2.5;
        double C = 1.8;
        double baseCount = A / (1.0 + B * Math.pow(rarity, C));
        return Math.max(1.0, Math.round(baseCount * 10.0) / 10.0) * countMultiplier;
    }

    protected double calculateProgressionFactor(WorldRollContext context) {
        int completions = context.getCompletionCount();
        double progressionRate = context.getConfig().progressionMultiplier;

        return 1.0 + (completions * (progressionRate - 1.0));
    }

    protected double calculateRenewableFactor(WorldRollContext context) {
        if (renewable) {
            return context.getConfig().renewableMultiplier;
        } else {
            Integer available = context.getScannedResources().get(minecraftItem);
            if (available == null || available <= 0) {
                return 1.0;
            }

            double multiplier = context.getConfig().nonRenewableMultiplier;
            double maxFromWorld = available * multiplier;

            return Math.min(1.0, maxFromWorld / 64.0);
        }
    }

    protected double calculateStageFactor() {
        return 1.0;
    }

    protected double applyCountCaps(double count) {
        return Math.min(count, 512);
    }

    public Item getMinecraftItem() { return minecraftItem; }

}
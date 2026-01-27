package com.neuromuser.worldbordercore.config;

import java.util.List;

public class Config {
    public double renewableMultiplier = 1.5;
    public double nonRenewableMultiplier = 0.3;
    public double progressionMultiplier = 1.05;
    public double randomnessVariation = 0.1;

    public boolean enableCoreRewards = true;
    public double baseRewardChance = 0.005;  // 0.5% base
    public double rewardChancePerLevel = 0.002;  // +0.2% per level
    public int maxRewardLevel = 20;
    public double levelSizeThreshold = 100.0;  // Border size per level (e.g., 100=level1, 200=level2)
    public double minRewardRarity = 3.0;  // Only rare items (tune based on your registry)
    public int maxItemsPerRewardBase = 2;  // Base max stack size
    public double rewardCountMultiplierPerLevel = 0.2;  // +20% items per level
    public boolean allowUnscannedRewards = true;  // Can drop absent items (e.g., diamonds)
    public List<String> rewardBlacklist = java.util.Arrays.asList(
            "elytra", "dragon_egg", "beacon", "netherite_ingot", "netherite_scrap"  // Customize; scraps allowed if rarity ok
    );
    public int minRewardCooldownTicks = 20 * 60 * 5;  // 5min global cooldown
    public boolean showRewardMessage = true;
}
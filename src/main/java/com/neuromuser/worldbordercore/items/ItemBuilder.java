package com.neuromuser.worldbordercore.items;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemBuilder {
    private final Item item;
    private double rarity = 1.0;
    private double count_multiplier = 1.0;
    private int minBorderSize = 0;
    private boolean renewable = false;
    private boolean requiresWorldScan = false;

    private final List<Predicate<WorldRollContext>> dependencies = new ArrayList<>();
    private final List<Predicate<WorldRollContext>> rollConditions = new ArrayList<>();
    private final List<CraftingRequirement> craftingRequirements = new ArrayList<>();

    private ItemBuilder(Item item) {
        this.item = item;
    }

    public record CraftingRequirement(Item requiredItem, int amountNeeded) {
    }

    public static ItemBuilder create(Item item) {
        return new ItemBuilder(item);
    }

    public ItemBuilder rarity(double rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemBuilder minBorder(int size) {
        this.minBorderSize = size;
        return this;
    }

    public ItemBuilder renewable() {
        this.renewable = true;
        return this;
    }

    public  ItemBuilder needScan() {
        this.requiresWorldScan = true;
        return this;
    }

    public ItemBuilder countMultiplier(double multiplier) {
        this.count_multiplier = multiplier;
        return this;
    }

    public ItemBuilder requireItem(Item requiredItem) {
        return requireItem(requiredItem, 1);
    }

    public ItemBuilder requireItem(Item requiredItem, int amountPer) {
        dependencies.add(ctx -> ctx.hasItem(requiredItem));
        craftingRequirements.add(new CraftingRequirement(requiredItem, amountPer));
        return this;
    }

    public ItemBuilder requireAnyFromTag(TagKey<Item> tag) {
        dependencies.add(ctx -> ctx.hasAnyFromTag(tag));
        return this;
    }

    public ItemBuilder requireAtLeastFromTag(TagKey<Item> tag, int count) {
        dependencies.add(ctx -> ctx.hasAtLeastNFromTag(tag, count));
        return this;
    }

    @SafeVarargs
    public final ItemBuilder requireAnyBiome(RegistryKey<Biome>... biomes) {
        rollConditions.add(ctx -> {
            for (RegistryKey<Biome> biome : biomes) {
                if (ctx.hasBiome(biome)) return true;
            }
            return false;
        });
        return this;
    }

    public ItemBuilder requireAchievement(String advancementId) {
        rollConditions.add(ctx -> ctx.hasPlayerAchievement(advancementId));
        return this;
    }

    public ItemBuilder requireCustom(Predicate<WorldRollContext> predicate) {
        dependencies.add(predicate);
        return this;
    }

    public RolledItem build() {
        return new BuiltRolledItem(
                item, rarity, count_multiplier, minBorderSize, renewable, requiresWorldScan,
                dependencies, rollConditions, craftingRequirements
        );
    }

    private static class BuiltRolledItem extends RolledItem {
        private final List<Predicate<WorldRollContext>> dependencies;
        private final List<Predicate<WorldRollContext>> rollConditions;
        private final List<CraftingRequirement> craftingRequirements;

        public BuiltRolledItem(Item item, double rarity, double countMultiplier, int minBorder,
                               boolean renewable, boolean requiresScan,
                               List<Predicate<WorldRollContext>> deps,
                               List<Predicate<WorldRollContext>> conditions,
                               List<CraftingRequirement> craftingReqs) {
            super(item, rarity, countMultiplier, minBorder, renewable, requiresScan);
            this.dependencies = new ArrayList<>(deps);
            this.rollConditions = new ArrayList<>(conditions);
            this.craftingRequirements = new ArrayList<>(craftingReqs);
        }

        @Override
        protected boolean checkDependencies(WorldRollContext context) {
            return dependencies.stream().allMatch(dep -> dep.test(context));
        }

        @Override
        public boolean canRoll(WorldRollContext context) {
            if (!super.canRoll(context)) return false;
            return rollConditions.stream().allMatch(cond -> cond.test(context));
        }

    }
}
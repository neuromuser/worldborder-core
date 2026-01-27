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
    private int minBorderSize = 20;
    private boolean renewable = false;
    private boolean requiresWorldScan = false;

    private final List<Predicate<WorldRollContext>> dependencies = new ArrayList<>();
    private final List<Predicate<WorldRollContext>> rollConditions = new ArrayList<>();

    private ItemBuilder(Item item) {
        this.item = item;
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

    public ItemBuilder requireItem(Item requiredItem) {
        dependencies.add(ctx -> ctx.hasItem(requiredItem));
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

    public ItemBuilder requireItems(Item... items) {
        for (Item item : items) {
            dependencies.add(ctx -> ctx.hasItem(item));
        }
        return this;
    }

    public ItemBuilder requireBiome(RegistryKey<Biome> biome) {
        rollConditions.add(ctx -> ctx.hasBiome(biome));
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

    public ItemBuilder requireCompletions(int min) {
        rollConditions.add(ctx -> ctx.getCompletionCount() >= min);
        return this;
    }

    public ItemBuilder requireBorderSize(double min) {
        rollConditions.add(ctx -> ctx.getBorderSize() >= min);
        return this;
    }

    public ItemBuilder requireCustom(Predicate<WorldRollContext> predicate) {
        dependencies.add(predicate);
        return this;
    }

    public ItemBuilder whenCustom(Predicate<WorldRollContext> predicate) {
        rollConditions.add(predicate);
        return this;
    }

    public RolledItem build() {
        return new BuiltRolledItem(
                item, rarity, minBorderSize, renewable, requiresWorldScan,
                dependencies, rollConditions
        );
    }

    private static class BuiltRolledItem extends RolledItem {
        private final List<Predicate<WorldRollContext>> dependencies;
        private final List<Predicate<WorldRollContext>> rollConditions;

        public BuiltRolledItem(Item item, double rarity, int minBorder,
                               boolean renewable, boolean requiresScan,
                               List<Predicate<WorldRollContext>> deps,
                               List<Predicate<WorldRollContext>> conditions) {
            super(item, rarity, minBorder, renewable, requiresScan);
            this.dependencies = new ArrayList<>(deps);
            this.rollConditions = new ArrayList<>(conditions);
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
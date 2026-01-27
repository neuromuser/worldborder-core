package com.neuromuser.worldbordercore.items;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import java.util.*;
import java.util.function.Predicate;

public class DependencyChecker {
    private final WorldRollContext context;
    private final List<Requirement> requirements = new ArrayList<>();

    private DependencyChecker(WorldRollContext context) {
        this.context = context;
    }

    public static DependencyChecker create(WorldRollContext context) {
        return new DependencyChecker(context);
    }

    public DependencyChecker requireItem(Item item) {
        requirements.add(() -> context.hasItem(item));
        return this;
    }

    public DependencyChecker requireOneOf(Item... items) {
        requirements.add(() ->
                Arrays.stream(items).anyMatch(context::hasItem)
        );
        return this;
    }

    public DependencyChecker requireAnyFromTag(TagKey<Item> tag) {
        requirements.add(() -> context.hasAnyFromTag(tag));
        return this;
    }

    public DependencyChecker requireAtLeastFromTag(TagKey<Item> tag, int n) {
        requirements.add(() -> context.hasAtLeastNFromTag(tag, n));
        return this;
    }

    public DependencyChecker requireItemInTag(Item item, TagKey<Item> tag) {
        requirements.add(() -> context.itemIsInTag(item, tag));
        return this;
    }

    public DependencyChecker requireAny(Set<Item> items) {
        requirements.add(() -> context.hasAnyItem(items));
        return this;
    }

    public DependencyChecker requireAll(Set<Item> items) {
        requirements.add(() -> context.hasAllItems(items));
        return this;
    }

    public DependencyChecker requireAtLeastTotal(Set<Item> items, int n) {
        requirements.add(() -> context.hasAtLeastNTotal(items, n));
        return this;
    }

    public DependencyChecker requireBiome(RegistryKey<Biome> biome) {
        requirements.add(() -> context.hasBiome(biome));
        return this;
    }

    @SafeVarargs
    public final DependencyChecker requireAnyBiome(RegistryKey<Biome>... biomes) {
        requirements.add(() ->
                Arrays.stream(biomes).anyMatch(context::hasBiome)
        );
        return this;
    }

    public DependencyChecker requireAchievement(String advancementId) {
        requirements.add(() -> context.hasPlayerAchievement(advancementId));
        return this;
    }

    public DependencyChecker requireBorderSize(double minSize) {
        requirements.add(() -> context.getBorderSize() >= minSize);
        return this;
    }

    public DependencyChecker requireCompletions(int minCompletions) {
        requirements.add(() -> context.getCompletionCount() >= minCompletions);
        return this;
    }

    public DependencyChecker requireCustom(Requirement requirement) {
        requirements.add(requirement);
        return this;
    }

    public DependencyChecker whenCustom(Predicate<WorldRollContext> predicate) {
        requirements.add(() -> predicate.test(context));
        return this;
    }

    public boolean check() {
        return requirements.stream().allMatch(Requirement::isMet);
    }

    public int countMet() {
        return (int) requirements.stream().filter(Requirement::isMet).count();
    }

    public boolean checkAtLeastN(int n) {
        return countMet() >= n;
    }

    @FunctionalInterface
    public interface Requirement {
        boolean isMet();
    }
}
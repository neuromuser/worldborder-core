package com.neuromuser.worldbordercore.items;

import com.neuromuser.worldbordercore.config.Config;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import java.util.*;
import java.util.stream.Collectors;

public class WorldRollContext {
    private final ServerWorld world;
    private final double borderSize;
    private final int completionCount;
    private final Map<Item, Integer> scannedResources;
    private final Set<RegistryKey<Biome>> availableBiomes;
    private final Config config;
    private final net.minecraft.util.math.random.Random random;

    private final Map<String, Boolean> playerAchievementCache = new HashMap<>();

    public WorldRollContext(ServerWorld world, double borderSize, int completionCount,
                            Map<Item, Integer> scannedResources,
                            Set<RegistryKey<Biome>> availableBiomes,
                            Config config, net.minecraft.util.math.random.Random random) {
        this.world = world;
        this.borderSize = borderSize;
        this.completionCount = completionCount;
        this.scannedResources = scannedResources;
        this.availableBiomes = availableBiomes;
        this.config = config;
        this.random = random;
    }

    public ServerWorld getWorld() { return world; }
    public double getBorderSize() { return borderSize; }
    public int getCompletionCount() { return completionCount; }
    public Map<Item, Integer> getScannedResources() { return scannedResources; }
    public Set<RegistryKey<Biome>> getAvailableBiomes() { return availableBiomes; }
    public Config getConfig() { return config; }
    public net.minecraft.util.math.random.Random getRandom() { return random; }

    public boolean hasPlayerAchievement(String advancementId) {
        if (playerAchievementCache.containsKey(advancementId)) {
            return playerAchievementCache.get(advancementId);
        }

        boolean hasIt = world.getPlayers().stream().anyMatch(player -> {
            var advancement = world.getServer().getAdvancementLoader()
                    .get(Identifier.of(advancementId));
            if (advancement == null) return false;
            return player.getAdvancementTracker().getProgress(advancement).isDone();
        });

        playerAchievementCache.put(advancementId, hasIt);
        return hasIt;
    }

    public boolean hasBiome(RegistryKey<Biome> biome) {
        return availableBiomes.contains(biome);
    }

    public boolean hasItem(Item item) {
        Integer count = scannedResources.get(item);
        return count != null && count > 0;
    }

    public int getItemCount(Item item) {
        return scannedResources.getOrDefault(item, 0);
    }

    public boolean hasAnyItem(Set<Item> itemGroup) {
        return itemGroup.stream().anyMatch(this::hasItem);
    }

    public boolean hasAllItems(Set<Item> itemGroup) {
        return itemGroup.stream().allMatch(this::hasItem);
    }

    public boolean hasAtLeastN(Set<Item> itemGroup, int n) {
        return itemGroup.stream().filter(this::hasItem).count() >= n;
    }

    public int getTotalCount(Set<Item> itemGroup) {
        return itemGroup.stream()
                .mapToInt(this::getItemCount)
                .sum();
    }

    public boolean hasAtLeastNTotal(Set<Item> itemGroup, int n) {
        return getTotalCount(itemGroup) >= n;
    }

    public Set<Item> getAvailableFromGroup(Set<Item> itemGroup) {
        return itemGroup.stream()
                .filter(this::hasItem)
                .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasAnyFromTag(TagKey<Item> tag) {
        return Registries.ITEM.getEntryList(tag)
                .stream()
                .flatMap(entries -> entries.stream())
                .map(entry -> entry.value())
                .anyMatch(this::hasItem);
    }

    public int getTotalCountFromTag(TagKey<Item> tag) {
        return Registries.ITEM.getEntryList(tag)
                .stream()
                .flatMap(entries -> entries.stream())
                .map(entry -> entry.value())
                .mapToInt(this::getItemCount)
                .sum();
    }

    public boolean hasAtLeastNFromTag(TagKey<Item> tag, int n) {
        return getTotalCountFromTag(tag) >= n;
    }

    public boolean itemIsInTag(Item item, TagKey<Item> tag) {
        return Registries.ITEM.getEntryList(tag)
                .stream()
                .flatMap(entries -> entries.stream())
                .anyMatch(entry -> entry.value() == item);
    }

    public Set<Item> getAvailableFromTag(TagKey<Item> tag) {
        return Registries.ITEM.getEntryList(tag)
                .stream()
                .flatMap(entries -> entries.stream())
                .map(entry -> entry.value())
                .filter(this::hasItem)
                .collect(Collectors.toSet());
    }
}
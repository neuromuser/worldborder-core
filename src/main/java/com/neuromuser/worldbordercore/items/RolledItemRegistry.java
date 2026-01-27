package com.neuromuser.worldbordercore.items;

import com.neuromuser.worldbordercore.config.Config;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import java.util.*;
import java.util.stream.Collectors;

public class RolledItemRegistry {
    private static final Map<Item, RolledItem> REGISTRY = new HashMap<>();
    private static final List<RolledItem> ALL_ITEMS = new ArrayList<>();

    public static void register(RolledItem item) {
        REGISTRY.put(item.getMinecraftItem(), item);
        ALL_ITEMS.add(item);
    }

    public static RolledItem get(Item item) {
        return REGISTRY.get(item);
    }

    public static Collection<RolledItem> getAll() {
        return Collections.unmodifiableList(ALL_ITEMS);
    }

    public static List<RolledItem> getItemsForContext(WorldRollContext context) {
        List<RolledItem> eligible = new ArrayList<>();
        for (RolledItem item : ALL_ITEMS) {
            if (item.canRoll(context)) {
                eligible.add(item);
            }
        }
        return eligible;
    }

    public static List<RolledItem> getEligibleRewards(WorldRollContext context, Config config) {
        return ALL_ITEMS.stream()
                .filter(item -> item.getRarity() >= config.minRewardRarity)
                .filter(item -> item.getMinBorderSize() <= context.getBorderSize())
                .filter(item -> {
                    String path = Registries.ITEM.getId(item.getMinecraftItem()).getPath();
                    return !config.rewardBlacklist.contains(path);
                })
                .filter(item -> config.allowUnscannedRewards || !item.requiresWorldScan() ||
                        context.getScannedResources().getOrDefault(item.getMinecraftItem(), 0) > 0)
                .collect(Collectors.toList());
    }

    private static RolledItem pickWeightedReward(List<RolledItem> eligible, net.minecraft.util.math.random.Random random) {
        if (eligible.isEmpty()) return null;
        double totalWeight = eligible.stream().mapToDouble(item -> 10.0 / item.getRarity()).sum();
        double pick = random.nextDouble() * totalWeight;
        double current = 0;
        for (RolledItem item : eligible) {
            current += 10.0 / item.getRarity();
            if (current >= pick) return item;
        }
        return eligible.get(eligible.size() - 1);  // Fallback
    }

    public static void registerAll() {

        // ============= WOODEN THINGS =============
        register(ItemBuilder.create(Items.OAK_LOG)
                .rarity(0.5)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SPRUCE_LOG)
                .rarity(0.6)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BIRCH_LOG)
                .rarity(0.5)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.JUNGLE_LOG)
                .rarity(0.6)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.ACACIA_LOG)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.DARK_OAK_LOG)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MANGROVE_LOG)
                .rarity(0.9)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.CHERRY_LOG)
                .rarity(1.1)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.CRIMSON_STEM)
                .rarity(1.5)
                .renewable()
                .needScan()
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.WARPED_STEM)
                .rarity(1.5)
                .renewable()
                .needScan()
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.OAK_PLANKS)
                .rarity(0.5)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.OAK_LOG)
                .build());
        register(ItemBuilder.create(Items.SPRUCE_PLANKS)
                .rarity(0.6)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.SPRUCE_LOG)
                .build());
        register(ItemBuilder.create(Items.BIRCH_PLANKS)
                .rarity(0.5)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.BIRCH_LOG)
                .build());
        register(ItemBuilder.create(Items.JUNGLE_PLANKS)
                .rarity(0.6)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.JUNGLE_LOG)
                .build());
        register(ItemBuilder.create(Items.ACACIA_PLANKS)
                .rarity(0.8)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.ACACIA_LOG)
                .build());
        register(ItemBuilder.create(Items.DARK_OAK_PLANKS)
                .rarity(0.8)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.DARK_OAK_LOG)
                .build());
        register(ItemBuilder.create(Items.MANGROVE_PLANKS)
                .rarity(0.9)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.MANGROVE_LOG)
                .build());
        register(ItemBuilder.create(Items.CHERRY_PLANKS)
                .rarity(1.1)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.CHERRY_LOG)
                .build());
        register(ItemBuilder.create(Items.CRIMSON_PLANKS)
                .rarity(1.5)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.CRIMSON_STEM)
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.WARPED_PLANKS)
                .rarity(1.5)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.WARPED_STEM)
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.STICK)
                .rarity(0.2)
                .renewable()
                .requireCustom(ctx -> {
                    boolean hasWood = ctx.hasAnyFromTag(ItemTags.LOGS);
                    boolean hasBamboo = ctx.hasItem(Items.BAMBOO);
                    return hasWood || hasBamboo;
                })
                .build());
        register(ItemBuilder.create(Items.CRAFTING_TABLE)
                .rarity(0.7)
                .renewable()
                .requireAnyFromTag(ItemTags.LOGS)
                .build());
        register(ItemBuilder.create(Items.CHEST)
                .rarity(1.0)
                .renewable()
                .requireAtLeastFromTag(ItemTags.LOGS, 2)
                .build());
        register(ItemBuilder.create(Items.BARREL)
                .rarity(0.9)
                .renewable()
                .requireAtLeastFromTag(ItemTags.LOGS, 2)
                .build());

        // --- MINING THINGS ---
        register(ItemBuilder.create(Items.STONE)
                .rarity(0.1)
                .needScan()
                .renewable()
                .build());
        register(ItemBuilder.create(Items.COBBLESTONE)
                .rarity(0.09)
                .renewable()
                .requireItem(Items.STONE)
                .build());
        register(ItemBuilder.create(Items.SMOOTH_STONE)
                .rarity(0.45)
                .renewable()
                .requireItem(Items.STONE)
                .build());
        register(ItemBuilder.create(Items.GRANITE)
                .rarity(0.25)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.DIORITE)
                .rarity(0.25)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.ANDESITE)
                .rarity(0.25)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.DEEPSLATE)
                .rarity(0.45)
                .requireAchievement("minecraft:story/upgrade_tools")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.COBBLED_DEEPSLATE)
                .rarity(0.3)
                .requireAchievement("minecraft:story/upgrade_tools")
                .requireItem(Items.DEEPSLATE)
                .build());
        register(ItemBuilder.create(Items.CHISELED_DEEPSLATE)
                .rarity(0.6)
                .requireAchievement("minecraft:story/upgrade_tools")
                .requireItem(Items.DEEPSLATE)
                .build());

        // DIRT
        register(ItemBuilder.create(Items.DIRT)
                .rarity(0.1)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.COARSE_DIRT)
                .rarity(0.3)
                .requireItem(Items.DIRT)
                .build());
        register(ItemBuilder.create(Items.PODZOL)
                .rarity(0.6)
                .renewable()
                .requireAchievement("minecraft:story/enchant_item")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.ROOTED_DIRT)
                .rarity(0.7)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MUD)
                .rarity(0.5)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.PACKED_MUD)
                .rarity(0.6)
                .requireItem(Items.MUD, 1)
                .build());
        register(ItemBuilder.create(Items.MUD_BRICKS)
                .rarity(0.7)
                .requireItem(Items.PACKED_MUD, 1)
                .build());

        register(ItemBuilder.create(Items.GRAVEL)
                .rarity(0.3)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SAND)
                .rarity(0.4)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.RED_SAND)
                .rarity(0.8)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SANDSTONE)
                .rarity(0.5)
                .renewable()
                .requireItem(Items.SAND, 4)
                .build());
        register(ItemBuilder.create(Items.RED_SANDSTONE)
                .rarity(0.9)
                .renewable()
                .requireItem(Items.RED_SAND, 4)
                .build());
        register(ItemBuilder.create(Items.SMOOTH_SANDSTONE)
                .rarity(0.6)
                .renewable()
                .requireItem(Items.SANDSTONE, 1)
                .build());

        register(ItemBuilder.create(Items.MOSSY_COBBLESTONE)
                .rarity(0.7)
                .renewable()
                .requireItem(Items.STONE, 1)
                .requireItem(Items.VINE, 1)
                .build());
        register(ItemBuilder.create(Items.MOSSY_STONE_BRICKS)
                .rarity(0.8)
                .renewable()
                .requireItem(Items.STONE, 1)
                .requireItem(Items.VINE, 1)
                .build());

        register(ItemBuilder.create(Items.CLAY)
                .rarity(0.6)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BRICKS)
                .rarity(0.8)
                .renewable()
                .requireItem(Items.CLAY, 1)
                .build());

        register(ItemBuilder.create(Items.PRISMARINE)
                .rarity(2.5)
                .minBorder(80)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.PRISMARINE_BRICKS)
                .rarity(2.6)
                .minBorder(80)
                .requireItem(Items.PRISMARINE)
                .build());
        register(ItemBuilder.create(Items.DARK_PRISMARINE)
                .rarity(2.7)
                .minBorder(80)
                .requireItem(Items.PRISMARINE)
                .build());
        register(ItemBuilder.create(Items.SEA_LANTERN)
                .rarity(6.0)
                .minBorder(300)
                .requireCustom(ctx -> {
                            boolean seaLanternBlock = ctx.hasItem(Items.SEA_LANTERN);
                            boolean pShards = ctx.hasItem(Items.PRISMARINE_SHARD);
                            boolean pCrystals = ctx.hasItem(Items.PRISMARINE_CRYSTALS);
                            return seaLanternBlock || (pShards && pCrystals);
                        })
                .build());

        register(ItemBuilder.create(Items.COAL)
                .rarity(0.8)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.RAW_IRON)
                .rarity(1.2)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.IRON_INGOT)
                .rarity(1.4)
                .renewable()
                .build());
        register(ItemBuilder.create(Items.RAW_COPPER)
                .rarity(1.0)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.COPPER_INGOT)
                .rarity(1.2)
                .build());
        register(ItemBuilder.create(Items.RAW_GOLD)
                .rarity(2.0)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.GOLD_INGOT)
                .rarity(2.2)
                .build());
        register(ItemBuilder.create(Items.REDSTONE)
                .rarity(1.5)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.LAPIS_LAZULI)
                .rarity(1.8)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.DIAMOND)
                .rarity(4.0)
                .minBorder(60)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.EMERALD)
                .rarity(5.0)
                .minBorder(80)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.NETHERRACK)
                .rarity(1.2)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.NETHER_BRICKS)
                .rarity(1.4)
                .requireItem(Items.NETHERRACK, 1)
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.RED_NETHER_BRICKS)
                .rarity(1.5)
                .requireItem(Items.NETHER_BRICKS, 2)
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.SOUL_SAND)
                .rarity(1.3)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SOUL_SOIL)
                .rarity(1.3)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BASALT)
                .rarity(1.4)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SMOOTH_BASALT)
                .rarity(1.5)
                .requireItem(Items.BASALT, 1)
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.BLACKSTONE)
                .rarity(1.5)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.POLISHED_BLACKSTONE)
                .rarity(1.6)
                .requireItem(Items.BLACKSTONE, 4)
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.GILDED_BLACKSTONE)
                .rarity(2.0)
                .minBorder(100)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.GLOWSTONE)
                .rarity(1.6)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MAGMA_BLOCK)
                .rarity(1.5)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BLAZE_ROD)
                .rarity(4.0)
                .minBorder(120)
                .renewable()
                .requireAchievement("minecraft:nether/find_fortress")
                .build());
        register(ItemBuilder.create(Items.NETHER_WART)
                .rarity(2.5)
                .minBorder(100)
                .renewable()
                .requireAchievement("minecraft:nether/find_fortress")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.ANCIENT_DEBRIS)
                .rarity(8.0)
                .minBorder(200)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.NETHERITE_SCRAP)
                .rarity(9.0)
                .minBorder(1000)
                .requireItem(Items.ANCIENT_DEBRIS, 1)
                .build());
        register(ItemBuilder.create(Items.NETHERITE_INGOT)
                .rarity(10.0)
                .minBorder(1000)
                .requireItem(Items.NETHERITE_SCRAP, 4)
                .requireItem(Items.GOLD_INGOT, 4)
                .build());

        register(ItemBuilder.create(Items.END_STONE)
                .rarity(6.0)
                .minBorder(200)
                .requireAchievement("minecraft:end/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.END_STONE_BRICKS)
                .rarity(6.2)
                .minBorder(200)
                .requireItem(Items.END_STONE, 4)
                .requireAchievement("minecraft:end/root")
                .build());
        register(ItemBuilder.create(Items.PURPUR_BLOCK)
                .rarity(6.5)
                .minBorder(200)
                .requireItem(Items.CHORUS_FRUIT, 4)
                .requireAchievement("minecraft:end/root")
                .build());
        register(ItemBuilder.create(Items.CHORUS_FRUIT)
                .rarity(6.0)
                .minBorder(200)
                .renewable()
                .requireAchievement("minecraft:end/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.ELYTRA)
                .rarity(15.0)
                .minBorder(500)
                .requireAchievement("minecraft:end/elytra")
                .build());
        register(ItemBuilder.create(Items.SHULKER_SHELL)
                .rarity(8.0)
                .minBorder(300)
                .renewable()
                .requireAchievement("minecraft:end/root")
                .build());
        register(ItemBuilder.create(Items.SHULKER_BOX)
                .rarity(8.5)
                .minBorder(300)
                .requireItem(Items.SHULKER_SHELL, 2)
                .requireItem(Items.CHEST, 1)
                .build());
        register(ItemBuilder.create(Items.DRAGON_EGG)
                .rarity(20.0)
                .minBorder(1000)
                .requireAchievement("minecraft:end/kill_dragon")
                .build());
        register(ItemBuilder.create(Items.DRAGON_HEAD)
                .rarity(20.0)
                .minBorder(1000)
                .requireAchievement("minecraft:end/dragon_breath")
                .build());

        register(ItemBuilder.create(Items.AMETHYST_SHARD)
                .rarity(2.5)
                .minBorder(60)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.AMETHYST_BLOCK)
                .rarity(2.8)
                .minBorder(60)
                .requireItem(Items.AMETHYST_SHARD, 4)
                .build());
        register(ItemBuilder.create(Items.CALCITE)
                .rarity(1.5)
                .minBorder(50)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.TUFF)
                .rarity(1.4)
                .minBorder(50)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.EXPOSED_COPPER)
                .rarity(1.8)
                .minBorder(80)
                .renewable()
                .requireItem(Items.COPPER_INGOT)
                .build());
        register(ItemBuilder.create(Items.WEATHERED_COPPER)
                .rarity(2.0)
                .minBorder(100)
                .renewable()
                .requireItem(Items.COPPER_INGOT)
                .build());
        register(ItemBuilder.create(Items.OXIDIZED_COPPER)
                .rarity(2.2)
                .minBorder(120)
                .renewable()
                .requireItem(Items.COPPER_INGOT)
                .build());

        register(ItemBuilder.create(Items.WHITE_WOOL)
                .rarity(1.0)
                .minBorder(30)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.ORANGE_WOOL)
                .rarity(1.1)
                .minBorder(35)
                .renewable()
                .requireItem(Items.WHITE_WOOL)
                .build());
        register(ItemBuilder.create(Items.BLACK_WOOL)
                .rarity(1.1)
                .minBorder(35)
                .renewable()
                .requireItem(Items.WHITE_WOOL)
                .build());

        register(ItemBuilder.create(Items.TERRACOTTA)
                .rarity(1.2)
                .requireItem(Items.CLAY, 1)
                .build());
        register(ItemBuilder.create(Items.WHITE_TERRACOTTA)
                .rarity(1.3)
                .requireItem(Items.TERRACOTTA, 8)
                .build());
        register(ItemBuilder.create(Items.WHITE_GLAZED_TERRACOTTA)
                .rarity(1.5)
                .requireItem(Items.WHITE_TERRACOTTA, 1)
                .build());

        register(ItemBuilder.create(Items.WHITE_CONCRETE)
                .rarity(1.4)
                .renewable()
                .requireItem(Items.SAND, 4)
                .requireItem(Items.GRAVEL, 4)
                .build());

        register(ItemBuilder.create(Items.GLASS)
                .rarity(0.6)
                .requireItem(Items.SAND, 1)
                .build());
        register(ItemBuilder.create(Items.GLASS_PANE)
                .rarity(0.7)
                .countMultiplier(1.5)
                .requireItem(Items.GLASS, 6)
                .build());
        register(ItemBuilder.create(Items.WHITE_STAINED_GLASS)
                .rarity(1.0)
                .requireItem(Items.GLASS, 8)
                .build());
        register(ItemBuilder.create(Items.TINTED_GLASS)
                .rarity(2.6)
                .minBorder(60)
                .requireItem(Items.GLASS, 1)
                .requireItem(Items.AMETHYST_SHARD, 4)
                .build());

        register(ItemBuilder.create(Items.CANDLE)
                .rarity(2.5)
                .minBorder(80)
                .renewable()
                .requireCustom(ctx -> ctx.hasItem(Items.HONEYCOMB) && ctx.hasItem(Items.STRING))
                .build());

        register(ItemBuilder.create(Items.OBSIDIAN)
                .rarity(2.5)
                .minBorder(60)
                .requireAchievement("minecraft:story/form_obsidian")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.CRYING_OBSIDIAN)
                .rarity(3.5)
                .minBorder(100)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());

        register(ItemBuilder.create(Items.ICE)
                .rarity(1.2)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.PACKED_ICE)
                .rarity(1.5)
                .requireItem(Items.ICE)
                .build());
        register(ItemBuilder.create(Items.BLUE_ICE)
                .rarity(2.0)
                .minBorder(80)
                .requireItem(Items.PACKED_ICE)
                .build());
        register(ItemBuilder.create(Items.SNOW_BLOCK)
                .rarity(1.0)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.MOSS_BLOCK)
                .rarity(1.8)
                .minBorder(60)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MOSS_CARPET)
                .rarity(1.9)
                .minBorder(60)
                .renewable()
                .requireItem(Items.MOSS_BLOCK)
                .build());

        register(ItemBuilder.create(Items.DRIPSTONE_BLOCK)
                .rarity(1.6)
                .minBorder(50)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.POINTED_DRIPSTONE)
                .rarity(1.7)
                .minBorder(50)
                .renewable()
                .requireItem(Items.DRIPSTONE_BLOCK)
                .build());

        register(ItemBuilder.create(Items.RAW_IRON_BLOCK)
                .rarity(1.8)
                .countMultiplier(0.11)
                .requireItem(Items.RAW_IRON)
                .build());
        register(ItemBuilder.create(Items.RAW_GOLD_BLOCK)
                .rarity(2.5)
                .countMultiplier(0.11)
                .requireItem(Items.RAW_GOLD)
                .build());
        register(ItemBuilder.create(Items.RAW_COPPER_BLOCK)
                .rarity(1.5)
                .countMultiplier(0.11)
                .requireItem(Items.RAW_COPPER)
                .build());

        register(ItemBuilder.create(Items.DANDELION)
                .rarity(0.4)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.POPPY)
                .rarity(0.5)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BLUE_ORCHID)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SUNFLOWER)
                .rarity(0.9)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.BROWN_MUSHROOM)
                .rarity(0.7)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.RED_MUSHROOM)
                .rarity(0.7)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.CRIMSON_FUNGUS)
                .rarity(1.5)
                .renewable()
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.WARPED_FUNGUS)
                .rarity(1.5)
                .renewable()
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());

        register(ItemBuilder.create(Items.SUGAR_CANE)
                .rarity(0.6)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.CACTUS)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BAMBOO)
                .rarity(1.2)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.GLOW_BERRIES)
                .rarity(1.8)
                .minBorder(50)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SWEET_BERRIES)
                .rarity(1.0)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.TUBE_CORAL)
                .rarity(2.5)
                .minBorder(100)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BRAIN_CORAL)
                .rarity(2.5)
                .minBorder(100)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SEA_PICKLE)
                .rarity(2.0)
                .minBorder(80)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.PUMPKIN)
                .rarity(1.0)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.CARVED_PUMPKIN)
                .rarity(1.1)
                .renewable()
                .requireItem(Items.PUMPKIN)
                .build());
        register(ItemBuilder.create(Items.MELON)
                .rarity(1.0)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.OCHRE_FROGLIGHT)
                .rarity(4.0)
                .minBorder(150)
                .renewable()
                .requireAchievement("minecraft:husbandry/froglights")
                .build());
        register(ItemBuilder.create(Items.VERDANT_FROGLIGHT)
                .rarity(4.0)
                .minBorder(150)
                .renewable()
                .requireAchievement("minecraft:husbandry/froglights")
                .build());
        register(ItemBuilder.create(Items.PEARLESCENT_FROGLIGHT)
                .rarity(4.0)
                .minBorder(150)
                .renewable()
                .requireAchievement("minecraft:husbandry/froglights")
                .build());

        register(ItemBuilder.create(Items.SCULK)
                .rarity(3.0)
                .minBorder(120)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SCULK_CATALYST)
                .rarity(3.5)
                .minBorder(150)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.SCULK_SENSOR)
                .rarity(3.2)
                .minBorder(130)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.FURNACE)
                .rarity(0.9)
                .renewable()
                .requireItem(Items.COBBLESTONE)
                .build());
        register(ItemBuilder.create(Items.BLAST_FURNACE)
                .rarity(1.8)
                .renewable()
                .requireItem(Items.FURNACE)
                .requireItem(Items.IRON_INGOT)
                .build());
        register(ItemBuilder.create(Items.SMOKER)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.FURNACE)
                .requireAnyFromTag(ItemTags.LOGS)
                .build());

        register(ItemBuilder.create(Items.DISPENSER)
                .rarity(2.0)
                .minBorder(50)
                .renewable()
                .requireItem(Items.COBBLESTONE)
                .requireItem(Items.REDSTONE)
                .build());
        register(ItemBuilder.create(Items.DROPPER)
                .rarity(2.0)
                .minBorder(50)
                .renewable()
                .requireItem(Items.COBBLESTONE)
                .requireItem(Items.REDSTONE)
                .build());

        register(ItemBuilder.create(Items.PISTON)
                .rarity(2.5)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.REDSTONE)
                .requireItem(Items.COBBLESTONE)
                .build());
        register(ItemBuilder.create(Items.STICKY_PISTON)
                .rarity(2.8)
                .minBorder(70)
                .renewable()
                .requireItem(Items.PISTON)
                .requireItem(Items.SLIME_BALL)
                .build());

        register(ItemBuilder.create(Items.HOPPER)
                .rarity(2.2)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.CHEST)
                .build());

        register(ItemBuilder.create(Items.OBSERVER)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.REDSTONE)
                .requireItem(Items.QUARTZ)
                .build());

        register(ItemBuilder.create(Items.REDSTONE_LAMP)
                .rarity(2.3)
                .minBorder(60)
                .renewable()
                .requireItem(Items.REDSTONE)
                .requireItem(Items.GLOWSTONE)
                .build());
        register(ItemBuilder.create(Items.REDSTONE_TORCH)
                .rarity(1.6)
                .renewable()
                .requireItem(Items.REDSTONE)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.REPEATER)
                .rarity(1.7)
                .renewable()
                .requireItem(Items.REDSTONE)
                .requireItem(Items.STONE)
                .build());
        register(ItemBuilder.create(Items.COMPARATOR)
                .rarity(2.5)
                .minBorder(70)
                .renewable()
                .requireItem(Items.REDSTONE)
                .requireItem(Items.QUARTZ)
                .build());

        register(ItemBuilder.create(Items.LIGHTNING_ROD)
                .rarity(1.8)
                .minBorder(50)
                .renewable()
                .requireItem(Items.COPPER_INGOT)
                .build());

        register(ItemBuilder.create(Items.LECTERN)
                .rarity(1.5)
                .renewable()
                .requireAnyFromTag(ItemTags.PLANKS)
                .requireItem(Items.BOOKSHELF)
                .build());
        register(ItemBuilder.create(Items.BOOKSHELF)
                .rarity(1.2)
                .renewable()
                .requireAnyFromTag(ItemTags.PLANKS)
                .requireItem(Items.BOOK)
                .build());

        register(ItemBuilder.create(Items.ANVIL)
                .rarity(3.0)
                .minBorder(80)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());
        register(ItemBuilder.create(Items.SMITHING_TABLE)
                .rarity(1.8)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireAnyFromTag(ItemTags.PLANKS)
                .build());
        register(ItemBuilder.create(Items.GRINDSTONE)
                .rarity(1.6)
                .renewable()
                .requireItem(Items.STONE)
                .requireAnyFromTag(ItemTags.PLANKS)
                .build());

        register(ItemBuilder.create(Items.ENCHANTING_TABLE)
                .rarity(6.0)
                .minBorder(100)
                .requireItem(Items.DIAMOND)
                .requireItem(Items.OBSIDIAN)
                .requireItem(Items.BOOK)
                .build());
        register(ItemBuilder.create(Items.BREWING_STAND)
                .rarity(4.0)
                .minBorder(100)
                .renewable()
                .requireItem(Items.BLAZE_ROD)
                .requireItem(Items.COBBLESTONE)
                .build());
        register(ItemBuilder.create(Items.BEACON)
                .rarity(10.0)
                .minBorder(300)
                .requireItem(Items.NETHER_STAR)
                .requireItem(Items.OBSIDIAN)
                .requireItem(Items.GLASS)
                .build());
        register(ItemBuilder.create(Items.CONDUIT)
                .rarity(8.0)
                .minBorder(150)
                .requireItem(Items.HEART_OF_THE_SEA)
                .requireItem(Items.NAUTILUS_SHELL)
                .build());

        register(ItemBuilder.create(Items.TNT)
                .rarity(2.5)
                .minBorder(70)
                .renewable()
                .requireItem(Items.GUNPOWDER)
                .requireItem(Items.SAND)
                .build());

        register(ItemBuilder.create(Items.SKELETON_SKULL)
                .rarity(5.0)
                .minBorder(150)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_mob_near_sculk_catalyst")
                .build());
        register(ItemBuilder.create(Items.WITHER_SKELETON_SKULL)
                .rarity(8.0)
                .minBorder(200)
                .renewable()
                .requireAchievement("minecraft:nether/find_fortress")
                .build());
        register(ItemBuilder.create(Items.ZOMBIE_HEAD)
                .rarity(5.0)
                .minBorder(150)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_mob_near_sculk_catalyst")
                .build());
        register(ItemBuilder.create(Items.CREEPER_HEAD)
                .rarity(6.0)
                .minBorder(180)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_mob_near_sculk_catalyst")
                .build());

        register(ItemBuilder.create(Items.ARMOR_STAND)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.STICK)
                .requireItem(Items.STONE)
                .build());

        register(ItemBuilder.create(Items.COMPASS)
                .rarity(2.5)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.REDSTONE)
                .build());
        register(ItemBuilder.create(Items.CLOCK)
                .rarity(3.0)
                .minBorder(70)
                .renewable()
                .requireItem(Items.GOLD_INGOT)
                .requireItem(Items.REDSTONE)
                .build());
        register(ItemBuilder.create(Items.RECOVERY_COMPASS)
                .rarity(4.0)
                .minBorder(150)
                .requireItem(Items.COMPASS)
                .requireItem(Items.ECHO_SHARD)
                .build());

        register(ItemBuilder.create(Items.BUCKET)
                .rarity(1.8)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());
        register(ItemBuilder.create(Items.WATER_BUCKET)
                .rarity(2.0)
                .minBorder(50)
                .renewable()
                .requireItem(Items.BUCKET)
                .build());
        register(ItemBuilder.create(Items.LAVA_BUCKET)
                .rarity(2.5)
                .minBorder(60)
                .renewable()
                .requireItem(Items.BUCKET)
                .build());
        register(ItemBuilder.create(Items.POWDER_SNOW_BUCKET)
                .rarity(2.2)
                .minBorder(60)
                .renewable()
                .requireItem(Items.BUCKET)
                .build());
        register(ItemBuilder.create(Items.COD_BUCKET)
                .rarity(2.5)
                .minBorder(70)
                .renewable()
                .requireItem(Items.BUCKET)
                .requireAchievement("minecraft:husbandry/fishy_business")
                .build());
        register(ItemBuilder.create(Items.TROPICAL_FISH_BUCKET)
                .rarity(3.0)
                .minBorder(80)
                .renewable()
                .requireItem(Items.BUCKET)
                .requireAchievement("minecraft:husbandry/tactical_fishing")
                .build());
        register(ItemBuilder.create(Items.AXOLOTL_BUCKET)
                .rarity(3.5)
                .minBorder(100)
                .renewable()
                .requireItem(Items.BUCKET)
                .requireAchievement("minecraft:husbandry/axolotl_in_a_bucket")
                .build());

        register(ItemBuilder.create(Items.MUSIC_DISC_13)
                .rarity(6.0)
                .minBorder(150)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MUSIC_DISC_CAT)
                .rarity(6.0)
                .minBorder(150)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MUSIC_DISC_OTHERSIDE)
                .rarity(7.0)
                .minBorder(200)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.MUSIC_DISC_PIGSTEP)
                .rarity(8.0)
                .minBorder(250)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());

        register(ItemBuilder.create(Items.NETHER_STAR)
                .rarity(10.0)
                .minBorder(300)
                .renewable()
                .requireAchievement("minecraft:nether/summon_wither")
                .build());
        register(ItemBuilder.create(Items.TOTEM_OF_UNDYING)
                .rarity(7.0)
                .minBorder(200)
                .renewable()
                .requireAchievement("minecraft:adventure/totem_of_undying")
                .build());
        register(ItemBuilder.create(Items.HEART_OF_THE_SEA)
                .rarity(6.0)
                .minBorder(150)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.ECHO_SHARD)
                .rarity(5.0)
                .minBorder(150)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.TURTLE_EGG)
                .rarity(4.0)
                .minBorder(120)
                .renewable()
                .requireAchievement("minecraft:husbandry/breed_all_animals")
                .build());
        register(ItemBuilder.create(Items.SCUTE)
                .rarity(4.5)
                .minBorder(150)
                .renewable()
                .requireItem(Items.TURTLE_EGG)
                .build());
        register(ItemBuilder.create(Items.TURTLE_HELMET)
                .rarity(5.0)
                .minBorder(150)
                .renewable()
                .requireItem(Items.SCUTE)
                .build());

        register(ItemBuilder.create(Items.WHEAT)
                .rarity(0.6)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BREAD)
                .rarity(0.8)
                .renewable()
                .requireItem(Items.WHEAT)
                .build());
        register(ItemBuilder.create(Items.CARROT)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.POTATO)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BAKED_POTATO)
                .rarity(0.9)
                .renewable()
                .requireItem(Items.POTATO)
                .build());
        register(ItemBuilder.create(Items.BEETROOT)
                .rarity(0.9)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.APPLE)
                .rarity(0.8)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.GOLDEN_APPLE)
                .rarity(3.0)
                .minBorder(80)
                .renewable()
                .requireItem(Items.APPLE)
                .requireItem(Items.GOLD_INGOT)
                .build());
        register(ItemBuilder.create(Items.ENCHANTED_GOLDEN_APPLE)
                .rarity(12.0)
                .minBorder(500)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.BEEF)
                .rarity(1.2)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.COOKED_BEEF)
                .rarity(1.3)
                .renewable()
                .requireItem(Items.BEEF)
                .build());
        register(ItemBuilder.create(Items.PORKCHOP)
                .rarity(1.2)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.COOKED_PORKCHOP)
                .rarity(1.3)
                .renewable()
                .requireItem(Items.PORKCHOP)
                .build());
        register(ItemBuilder.create(Items.CHICKEN)
                .rarity(1.0)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.COOKED_CHICKEN)
                .rarity(1.1)
                .renewable()
                .requireItem(Items.CHICKEN)
                .build());
        register(ItemBuilder.create(Items.MUTTON)
                .rarity(1.2)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.COOKED_MUTTON)
                .rarity(1.3)
                .renewable()
                .requireItem(Items.MUTTON)
                .build());
        register(ItemBuilder.create(Items.RABBIT)
                .rarity(1.5)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.COOKED_RABBIT)
                .rarity(1.6)
                .renewable()
                .requireItem(Items.RABBIT)
                .build());

        register(ItemBuilder.create(Items.COD)
                .rarity(1.3)
                .renewable()
                .requireAchievement("minecraft:husbandry/fishy_business")
                .build());
        register(ItemBuilder.create(Items.COOKED_COD)
                .rarity(1.4)
                .renewable()
                .requireItem(Items.COD)
                .build());
        register(ItemBuilder.create(Items.SALMON)
                .rarity(1.4)
                .renewable()
                .requireAchievement("minecraft:husbandry/fishy_business")
                .build());
        register(ItemBuilder.create(Items.COOKED_SALMON)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.SALMON)
                .build());
        register(ItemBuilder.create(Items.TROPICAL_FISH)
                .rarity(2.0)
                .renewable()
                .requireAchievement("minecraft:husbandry/tactical_fishing")
                .build());

        register(ItemBuilder.create(Items.EGG)
                .rarity(0.9)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.MILK_BUCKET)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.BUCKET)
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.HONEY_BOTTLE)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.HONEYCOMB)
                .build());
        register(ItemBuilder.create(Items.HONEYCOMB)
                .rarity(1.8)
                .minBorder(50)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.COOKIE)
                .rarity(1.2)
                .renewable()
                .requireItem(Items.WHEAT)
                .requireItem(Items.COCOA_BEANS)
                .build());
        register(ItemBuilder.create(Items.CAKE)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.WHEAT)
                .requireItem(Items.EGG)
                .requireItem(Items.MILK_BUCKET)
                .build());
        register(ItemBuilder.create(Items.PUMPKIN_PIE)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.PUMPKIN)
                .requireItem(Items.EGG)
                .build());

        register(ItemBuilder.create(Items.SUSPICIOUS_STEW)
                .rarity(2.5)
                .minBorder(70)
                .renewable()
                .requireItem(Items.BROWN_MUSHROOM)
                .requireItem(Items.RED_MUSHROOM)
                .build());
        register(ItemBuilder.create(Items.MUSHROOM_STEW)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.BROWN_MUSHROOM)
                .requireItem(Items.RED_MUSHROOM)
                .build());
        register(ItemBuilder.create(Items.RABBIT_STEW)
                .rarity(2.5)
                .minBorder(80)
                .renewable()
                .requireItem(Items.RABBIT)
                .requireItem(Items.CARROT)
                .requireItem(Items.POTATO)
                .build());

        register(ItemBuilder.create(Items.DRIED_KELP)
                .rarity(1.2)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.COCOA_BEANS)
                .rarity(1.5)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.LEATHER)
                .rarity(1.3)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.FEATHER)
                .rarity(1.0)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.STRING)
                .rarity(1.2)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());
        register(ItemBuilder.create(Items.SLIME_BALL)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.BONE)
                .rarity(1.1)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());
        register(ItemBuilder.create(Items.BONE_MEAL)
                .rarity(1.0)
                .countMultiplier(3.0)
                .renewable()
                .requireItem(Items.BONE)
                .build());

        register(ItemBuilder.create(Items.PAPER)
                .rarity(0.9)
                .countMultiplier(3.0)
                .renewable()
                .requireItem(Items.SUGAR_CANE)
                .build());
        register(ItemBuilder.create(Items.BOOK)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.PAPER)
                .requireItem(Items.LEATHER)
                .build());
        register(ItemBuilder.create(Items.WRITABLE_BOOK)
                .rarity(1.8)
                .renewable()
                .requireItem(Items.BOOK)
                .requireItem(Items.FEATHER)
                .build());

        register(ItemBuilder.create(Items.ENDER_PEARL)
                .rarity(3.0)
                .minBorder(80)
                .renewable()
                .requireAchievement("minecraft:story/enter_the_nether")
                .build());
        register(ItemBuilder.create(Items.ENDER_EYE)
                .rarity(3.5)
                .minBorder(100)
                .renewable()
                .requireItem(Items.ENDER_PEARL)
                .requireItem(Items.BLAZE_POWDER)
                .build());

        register(ItemBuilder.create(Items.GUNPOWDER)
                .rarity(1.5)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());
        register(ItemBuilder.create(Items.BLAZE_POWDER)
                .rarity(4.2)
                .countMultiplier(2.0)
                .minBorder(120)
                .renewable()
                .requireItem(Items.BLAZE_ROD)
                .build());
        register(ItemBuilder.create(Items.MAGMA_CREAM)
                .rarity(2.5)
                .minBorder(80)
                .renewable()
                .requireAchievement("minecraft:nether/root")
                .build());
        register(ItemBuilder.create(Items.GHAST_TEAR)
                .rarity(4.0)
                .minBorder(120)
                .renewable()
                .requireAchievement("minecraft:nether/root")
                .build());

        register(ItemBuilder.create(Items.PRISMARINE_SHARD)
                .rarity(2.6)
                .minBorder(80)
                .renewable()
                .needScan()
                .build());
        register(ItemBuilder.create(Items.PRISMARINE_CRYSTALS)
                .rarity(2.7)
                .minBorder(80)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.PHANTOM_MEMBRANE)
                .rarity(3.5)
                .minBorder(100)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());

        register(ItemBuilder.create(Items.INK_SAC)
                .rarity(1.2)
                .renewable()
                .requireAchievement("minecraft:husbandry/fishy_business")
                .build());
        register(ItemBuilder.create(Items.GLOW_INK_SAC)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());

        register(ItemBuilder.create(Items.WHITE_DYE)
                .rarity(0.8)
                .renewable()
                .requireCustom(ctx -> ctx.hasItem(Items.BONE_MEAL) || ctx.hasItem(Items.LILY_OF_THE_VALLEY))
                .build());
        register(ItemBuilder.create(Items.BLACK_DYE)
                .rarity(0.9)
                .renewable()
                .requireCustom(ctx -> ctx.hasItem(Items.INK_SAC) || ctx.hasItem(Items.WITHER_ROSE))
                .build());
        register(ItemBuilder.create(Items.RED_DYE)
                .rarity(0.7)
                .renewable()
                .requireCustom(ctx -> ctx.hasItem(Items.POPPY) || ctx.hasItem(Items.BEETROOT))
                .build());
        register(ItemBuilder.create(Items.GREEN_DYE)
                .rarity(1.0)
                .renewable()
                .requireItem(Items.CACTUS)
                .build());
        register(ItemBuilder.create(Items.BLUE_DYE)
                .rarity(1.2)
                .requireCustom(ctx -> ctx.hasItem(Items.LAPIS_LAZULI) || ctx.hasItem(Items.CORNFLOWER))
                .build());
        register(ItemBuilder.create(Items.YELLOW_DYE)
                .rarity(0.7)
                .renewable()
                .requireCustom(ctx -> ctx.hasItem(Items.DANDELION) || ctx.hasItem(Items.SUNFLOWER))
                .build());

        register(ItemBuilder.create(Items.SADDLE)
                .rarity(3.0)
                .minBorder(80)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.NAME_TAG)
                .rarity(3.5)
                .minBorder(100)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.LEAD)
                .rarity(1.8)
                .renewable()
                .requireItem(Items.STRING)
                .requireItem(Items.SLIME_BALL)
                .build());

        register(ItemBuilder.create(Items.QUARTZ)
                .rarity(1.8)
                .requireAchievement("minecraft:nether/root")
                .needScan()
                .build());
        register(ItemBuilder.create(Items.QUARTZ_BLOCK)
                .rarity(2.0)
                .countMultiplier(0.25)
                .requireItem(Items.QUARTZ)
                .build());

        register(ItemBuilder.create(Items.NAUTILUS_SHELL)
                .rarity(4.0)
                .minBorder(120)
                .renewable()
                .requireAchievement("minecraft:husbandry/fishy_business")
                .build());

        register(ItemBuilder.create(Items.RABBIT_FOOT)
                .rarity(3.0)
                .minBorder(80)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());
        register(ItemBuilder.create(Items.RABBIT_HIDE)
                .rarity(1.4)
                .renewable()
                .requireAchievement("minecraft:husbandry/root")
                .build());

        register(ItemBuilder.create(Items.WOODEN_PICKAXE)
                .rarity(0.8)
                .renewable()
                .requireAnyFromTag(ItemTags.PLANKS)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.STONE_PICKAXE)
                .rarity(1.0)
                .renewable()
                .requireItem(Items.COBBLESTONE)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.IRON_PICKAXE)
                .rarity(2.0)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.DIAMOND_PICKAXE)
                .rarity(4.5)
                .minBorder(100)
                .renewable()
                .requireItem(Items.DIAMOND)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.NETHERITE_PICKAXE)
                .rarity(11.0)
                .minBorder(1000)
                .renewable()
                .requireItem(Items.DIAMOND_PICKAXE)
                .requireItem(Items.NETHERITE_INGOT)
                .build());

        register(ItemBuilder.create(Items.SHEARS)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());
        register(ItemBuilder.create(Items.FLINT_AND_STEEL)
                .rarity(1.8)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.FLINT)
                .build());
        register(ItemBuilder.create(Items.FLINT)
                .rarity(0.8)
                .renewable()
                .requireItem(Items.GRAVEL)
                .build());

        register(ItemBuilder.create(Items.FISHING_ROD)
                .rarity(1.2)
                .renewable()
                .requireItem(Items.STICK)
                .requireItem(Items.STRING)
                .build());
        register(ItemBuilder.create(Items.CARROT_ON_A_STICK)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.FISHING_ROD)
                .requireItem(Items.CARROT)
                .build());

        register(ItemBuilder.create(Items.WHITE_BED)
                .rarity(1.5)
                .minBorder(40)
                .renewable()
                .requireItem(Items.WHITE_WOOL)
                .requireAnyFromTag(ItemTags.PLANKS)
                .build());
        register(ItemBuilder.create(Items.RED_BED)
                .rarity(1.6)
                .minBorder(40)
                .renewable()
                .requireCustom(ctx -> ctx.hasAnyFromTag(ItemTags.WOOL) && ctx.hasAnyFromTag(ItemTags.PLANKS))
                .build());

        register(ItemBuilder.create(Items.WHITE_BANNER)
                .rarity(1.8)
                .renewable()
                .requireItem(Items.WHITE_WOOL)
                .requireItem(Items.STICK)
                .build());

        register(ItemBuilder.create(Items.PAINTING)
                .rarity(1.2)
                .renewable()
                .requireItem(Items.STICK)
                .requireItem(Items.WHITE_WOOL)
                .build());
        register(ItemBuilder.create(Items.ITEM_FRAME)
                .rarity(1.3)
                .renewable()
                .requireItem(Items.STICK)
                .requireItem(Items.LEATHER)
                .build());
        register(ItemBuilder.create(Items.GLOW_ITEM_FRAME)
                .rarity(2.2)
                .minBorder(60)
                .renewable()
                .requireItem(Items.ITEM_FRAME)
                .requireItem(Items.GLOW_INK_SAC)
                .build());

        register(ItemBuilder.create(Items.GLASS_BOTTLE)
                .rarity(1.0)
                .countMultiplier(3.0)
                .renewable()
                .requireItem(Items.GLASS)
                .build());
        register(ItemBuilder.create(Items.EXPERIENCE_BOTTLE)
                .rarity(5.0)
                .minBorder(150)
                .needScan()
                .build());
        register(ItemBuilder.create(Items.DRAGON_BREATH)
                .rarity(8.0)
                .minBorder(300)
                .requireAchievement("minecraft:end/dragon_breath")
                .build());

        register(ItemBuilder.create(Items.SPIDER_EYE)
                .rarity(1.5)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());
        register(ItemBuilder.create(Items.FERMENTED_SPIDER_EYE)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.SPIDER_EYE)
                .requireItem(Items.BROWN_MUSHROOM)
                .build());
        register(ItemBuilder.create(Items.GLISTERING_MELON_SLICE)
                .rarity(2.5)
                .minBorder(70)
                .renewable()
                .requireItem(Items.MELON_SLICE)
                .requireItem(Items.GOLD_NUGGET)
                .build());

        register(ItemBuilder.create(Items.ROTTEN_FLESH)
                .rarity(1.0)
                .renewable()
                .requireAchievement("minecraft:adventure/kill_a_mob")
                .build());
        register(ItemBuilder.create(Items.POISONOUS_POTATO)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.POTATO)
                .build());

        register(ItemBuilder.create(Items.TORCH)
                .rarity(0.8)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.COAL)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.SOUL_TORCH)
                .rarity(1.5)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.COAL)
                .requireItem(Items.SOUL_SAND)
                .build());
        register(ItemBuilder.create(Items.LANTERN)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.TORCH)
                .requireItem(Items.IRON_NUGGET)
                .build());
        register(ItemBuilder.create(Items.SOUL_LANTERN)
                .rarity(2.0)
                .renewable()
                .requireItem(Items.SOUL_TORCH)
                .requireItem(Items.IRON_NUGGET)
                .build());

        register(ItemBuilder.create(Items.RAIL)
                .rarity(1.8)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.POWERED_RAIL)
                .rarity(2.2)
                .minBorder(60)
                .renewable()
                .requireItem(Items.GOLD_INGOT)
                .requireItem(Items.REDSTONE)
                .build());
        register(ItemBuilder.create(Items.DETECTOR_RAIL)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.REDSTONE)
                .build());
        register(ItemBuilder.create(Items.ACTIVATOR_RAIL)
                .rarity(2.1)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.REDSTONE)
                .build());

        register(ItemBuilder.create(Items.MINECART)
                .rarity(2.0)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());
        register(ItemBuilder.create(Items.CHEST_MINECART)
                .rarity(2.3)
                .minBorder(60)
                .renewable()
                .requireItem(Items.MINECART)
                .requireItem(Items.CHEST)
                .build());
        register(ItemBuilder.create(Items.FURNACE_MINECART)
                .rarity(2.3)
                .minBorder(60)
                .renewable()
                .requireItem(Items.MINECART)
                .requireItem(Items.FURNACE)
                .build());

        register(ItemBuilder.create(Items.OAK_DOOR)
                .rarity(0.8)
                .renewable()
                .requireItem(Items.OAK_PLANKS)
                .build());
        register(ItemBuilder.create(Items.IRON_DOOR)
                .rarity(2.0)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());

        register(ItemBuilder.create(Items.OAK_TRAPDOOR)
                .rarity(0.9)
                .renewable()
                .requireItem(Items.OAK_PLANKS)
                .build());
        register(ItemBuilder.create(Items.IRON_TRAPDOOR)
                .rarity(2.1)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());

        register(ItemBuilder.create(Items.OAK_FENCE)
                .rarity(0.7)
                .renewable()
                .requireItem(Items.OAK_PLANKS)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.OAK_FENCE_GATE)
                .rarity(0.8)
                .renewable()
                .requireItem(Items.OAK_PLANKS)
                .requireItem(Items.STICK)
                .build());

        register(ItemBuilder.create(Items.OAK_SIGN)
                .rarity(0.9)
                .renewable()
                .requireItem(Items.OAK_PLANKS)
                .requireItem(Items.STICK)
                .build());
        register(ItemBuilder.create(Items.OAK_HANGING_SIGN)
                .rarity(1.2)
                .renewable()
                .requireItem(Items.OAK_LOG)
                .requireItem(Items.CHAIN)
                .build());

        register(ItemBuilder.create(Items.LADDER)
                .rarity(0.9)
                .renewable()
                .requireItem(Items.STICK)
                .build());

        register(ItemBuilder.create(Items.CHAIN)
                .rarity(1.8)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());
        register(ItemBuilder.create(Items.IRON_BARS)
                .rarity(1.9)
                .minBorder(50)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .build());

        register(ItemBuilder.create(Items.FLOWER_POT)
                .rarity(1.0)
                .renewable()
                .requireItem(Items.BRICK)
                .build());

        register(ItemBuilder.create(Items.MAP)
                .rarity(2.0)
                .minBorder(60)
                .renewable()
                .requireItem(Items.PAPER)
                .requireItem(Items.COMPASS)
                .build());

        register(ItemBuilder.create(Items.SPYGLASS)
                .rarity(2.8)
                .minBorder(80)
                .renewable()
                .requireItem(Items.COPPER_INGOT)
                .requireItem(Items.AMETHYST_SHARD)
                .build());

        register(ItemBuilder.create(Items.SHIELD)
                .rarity(2.5)
                .minBorder(60)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireAnyFromTag(ItemTags.PLANKS)
                .build());

        register(ItemBuilder.create(Items.BOW)
                .rarity(1.5)
                .renewable()
                .requireItem(Items.STICK)
                .requireItem(Items.STRING)
                .build());
        register(ItemBuilder.create(Items.ARROW)
                .rarity(1.3)
                .countMultiplier(4.0)
                .renewable()
                .requireItem(Items.FLINT)
                .requireItem(Items.STICK)
                .requireItem(Items.FEATHER)
                .build());
        register(ItemBuilder.create(Items.SPECTRAL_ARROW)
                .rarity(3.0)
                .minBorder(80)
                .renewable()
                .requireItem(Items.ARROW)
                .requireItem(Items.GLOWSTONE_DUST)
                .build());
        register(ItemBuilder.create(Items.CROSSBOW)
                .rarity(2.5)
                .minBorder(70)
                .renewable()
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.STRING)
                .requireItem(Items.TRIPWIRE_HOOK)
                .build());

        register(ItemBuilder.create(Items.TRIDENT)
                .rarity(7.0)
                .minBorder(200)
                .renewable()
                .requireAchievement("minecraft:adventure/throw_trident")
                .build());

        register(ItemBuilder.create(Items.BELL)
                .rarity(2.5)
                .minBorder(70)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                .rarity(9.0)
                .minBorder(800)
                .needScan()
                .build());
    }
}
        //TODO: ITEM COMBINATIONS
        //TODO: BAMBOO ITEMS
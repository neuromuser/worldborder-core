package com.neuromuser.worldbordercore.items;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.world.biome.BiomeKeys;
import java.util.*;

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

    public static void registerAll() {
        register(ItemBuilder.create(Items.STONE)
                .rarity(0.1)
                .minBorder(20)
                .needScan()
                .build());

        register(ItemBuilder.create(Items.COBBLESTONE)
                .rarity(0.15)
                .minBorder(20)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.DIRT)
                .rarity(0.2)
                .minBorder(20)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.OAK_LOG)
                .rarity(0.5)
                .minBorder(20)
                .renewable()
                .needScan()
                .build());

        register(ItemBuilder.create(Items.SPRUCE_LOG)
                .rarity(0.6)
                .minBorder(25)
                .renewable()
                .needScan()
                .requireAnyBiome(BiomeKeys.TAIGA, BiomeKeys.SNOWY_TAIGA)
                .build());

        register(ItemBuilder.create(Items.BIRCH_LOG)
                .rarity(0.6)
                .minBorder(25)
                .renewable()
                .needScan()
                .requireBiome(BiomeKeys.BIRCH_FOREST)
                .build());

        register(ItemBuilder.create(Items.CRAFTING_TABLE)
                .rarity(0.7)
                .minBorder(25)
                .renewable()
                .requireAnyFromTag(ItemTags.LOGS)
                .build());

        register(ItemBuilder.create(Items.CHEST)
                .rarity(1.0)
                .minBorder(30)
                .renewable()
                .requireAtLeastFromTag(ItemTags.LOGS, 8)
                .build());

        register(ItemBuilder.create(Items.STICK)
                .rarity(0.3)
                .minBorder(20)
                .renewable()
                .requireAnyFromTag(ItemTags.LOGS)
                .build());

        register(ItemBuilder.create(Items.WHEAT)
                .rarity(0.6)
                .minBorder(25)
                .renewable()
                .build());

        register(ItemBuilder.create(Items.CARROT)
                .rarity(0.8)
                .minBorder(30)
                .renewable()
                .build());

        register(ItemBuilder.create(Items.POTATO)
                .rarity(0.8)
                .minBorder(30)
                .renewable()
                .build());

        register(ItemBuilder.create(Items.RABBIT_FOOT)
                .rarity(5.0)
                .minBorder(60)
                .renewable()
                .requireAnyBiome(
                        BiomeKeys.DESERT,
                        BiomeKeys.FLOWER_FOREST,
                        BiomeKeys.TAIGA,
                        BiomeKeys.SNOWY_TAIGA,
                        BiomeKeys.SNOWY_PLAINS
                )
                .build());

        register(ItemBuilder.create(Items.COAL)
                .rarity(1.5)
                .minBorder(30)
                .build());

        register(ItemBuilder.create(Items.RAW_IRON)
                .rarity(2.0)
                .minBorder(35)
                .build());

        register(ItemBuilder.create(Items.IRON_INGOT)
                .rarity(2.5)
                .minBorder(40)
                .requireItem(Items.RAW_IRON)
                .build());

        register(ItemBuilder.create(Items.RAW_GOLD)
                .rarity(3.0)
                .minBorder(45)
                .build());

        register(ItemBuilder.create(Items.GOLD_INGOT)
                .rarity(3.5)
                .minBorder(50)
                .requireItem(Items.RAW_GOLD)
                .build());

        register(ItemBuilder.create(Items.DIAMOND)
                .rarity(5.0)
                .minBorder(60)
                .build());

        register(ItemBuilder.create(Items.EMERALD)
                .rarity(6.0)
                .minBorder(80)
                .requireAnyBiome(
                        BiomeKeys.WINDSWEPT_HILLS,
                        BiomeKeys.WINDSWEPT_FOREST,
                        BiomeKeys.WINDSWEPT_GRAVELLY_HILLS
                )
                .build());

        register(ItemBuilder.create(Items.PISTON)
                .rarity(2.5)
                .minBorder(50)
                .requireAnyFromTag(ItemTags.PLANKS)
                .requireItem(Items.COBBLESTONE)
                .requireItem(Items.IRON_INGOT)
                .requireItem(Items.REDSTONE)
                .build());

        register(ItemBuilder.create(Items.WHITE_BED)
                .rarity(1.5)
                .minBorder(40)
                .renewable()
                .requireAnyFromTag(ItemTags.WOOL)
                .requireAnyFromTag(ItemTags.PLANKS)
                .build());

        register(ItemBuilder.create(Items.COMPASS)
                .rarity(2.5)
                .minBorder(50)
                .renewable()
                .requireAnyFromTag(MaterialGroups.INGOTS_IRON)
                .requireItem(Items.REDSTONE)
                .build());

        register(ItemBuilder.create(Items.CLOCK)
                .rarity(3.5)
                .minBorder(60)
                .renewable()
                .requireAnyFromTag(MaterialGroups.INGOTS_GOLD)
                .requireItem(Items.REDSTONE)
                .build());

        register(ItemBuilder.create(Items.FURNACE)
                .rarity(0.9)
                .minBorder(25)
                .renewable()
                .requireItem(Items.COBBLESTONE)
                .build());

        register(ItemBuilder.create(Items.BLAZE_ROD)
                .rarity(4.0)
                .minBorder(100)
                .renewable()
                .requireAchievement("minecraft:nether/root")
                .build());

        register(ItemBuilder.create(Items.NETHER_STAR)
                .rarity(10.0)
                .minBorder(200)
                .renewable()
                .requireAchievement("minecraft:nether/summon_wither")
                .requireItem(Items.WITHER_SKELETON_SKULL)
                .build());

        register(ItemBuilder.create(Items.BREWING_STAND)
                .rarity(4.0)
                .minBorder(80)
                .renewable()
                .requireAchievement("minecraft:nether/root")
                .requireItem(Items.BLAZE_ROD)
                .requireItem(Items.COBBLESTONE)
                .build());

        register(ItemBuilder.create(Items.CONDUIT)
                .rarity(8.0)
                .minBorder(150)
                .requireItem(Items.HEART_OF_THE_SEA)
                .requireItem(Items.NAUTILUS_SHELL)
                .requireAnyBiome(
                        BiomeKeys.OCEAN,
                        BiomeKeys.DEEP_OCEAN,
                        BiomeKeys.WARM_OCEAN
                )
                .build());

        register(new EnchantingTableItem());
        register(new ElytraItem());
        register(new BeaconItem());
        register(new DragonEggItem());
    }
}

class EnchantingTableItem extends RolledItem {
    public EnchantingTableItem() {
        super(Items.ENCHANTING_TABLE, 6.0, 100, true, false);
    }

    @Override
    protected boolean checkDependencies(WorldRollContext context) {
        return DependencyChecker.create(context)
                .requireItem(Items.BOOK)
                .requireAtLeastFromTag(MaterialGroups.GEMS_DIAMOND, 2)
                .requireAtLeastTotal(Set.of(Items.OBSIDIAN), 4)
                .check();
    }

    @Override
    public boolean canRoll(WorldRollContext context) {
        if (!super.canRoll(context)) return false;
        return context.getCompletionCount() >= 5;
    }

    @Override
    protected double calculateStageFactor(WorldRollContext context) {
        int completions = context.getCompletionCount();
        if (completions < 10) {
            return 0.2;
        } else if (completions < 30) {
            return 0.5;
        } else {
            return 1.0;
        }
    }
}

class ElytraItem extends RolledItem {
    public ElytraItem() {
        super(Items.ELYTRA, 15.0, 500, false, false);
    }

    @Override
    public boolean canRoll(WorldRollContext context) {
        if (!super.canRoll(context)) return false;
        return context.hasPlayerAchievement("minecraft:end/root");
    }

    @Override
    protected double calculateBaseCount(WorldRollContext context) {
        return 1.0;
    }
}

class BeaconItem extends RolledItem {
    public BeaconItem() {
        super(Items.BEACON, 10.0, 300, true, false);
    }

    @Override
    protected boolean checkDependencies(WorldRollContext context) {
        return DependencyChecker.create(context)
                .requireItem(Items.NETHER_STAR)
                .requireAtLeastTotal(Set.of(Items.OBSIDIAN), 3)
                .requireAtLeastTotal(Set.of(Items.GLASS), 5)
                .check();
    }

    @Override
    public boolean canRoll(WorldRollContext context) {
        if (!super.canRoll(context)) return false;
        return DependencyChecker.create(context)
                .requireAchievement("minecraft:nether/summon_wither")
                .requireCompletions(20)
                .requireBorderSize(300)
                .check();
    }

    @Override
    protected double calculateBaseCount(WorldRollContext context) {
        return 1.0;
    }
}

class DragonEggItem extends RolledItem {
    public DragonEggItem() {
        super(Items.DRAGON_EGG, 20.0, 1000, false, false);
    }

    @Override
    public boolean canRoll(WorldRollContext context) {
        if (!super.canRoll(context)) return false;
        return context.hasPlayerAchievement("minecraft:end/kill_dragon");
    }

    @Override
    protected double calculateBaseCount(WorldRollContext context) {
        return 1.0;
    }
}
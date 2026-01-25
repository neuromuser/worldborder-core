package com.neuromuser.worldbordercore;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;

import java.util.*;

public class ItemMultiplierSystem {

    private static final Map<Item, Double> MULTIPLIERS = new HashMap<>();
    private static final List<Item> REROLL_ITEMS = new ArrayList<>();

    public static void initialize() {
        MULTIPLIERS.put(Items.DIRT, 64.0);
        MULTIPLIERS.put(Items.COARSE_DIRT, 64.0);
        MULTIPLIERS.put(Items.GRASS_BLOCK, 64.0);
        MULTIPLIERS.put(Items.PODZOL, 48.0);
        MULTIPLIERS.put(Items.MYCELIUM, 32.0);
        MULTIPLIERS.put(Items.COBBLESTONE, 64.0);
        MULTIPLIERS.put(Items.STONE, 64.0);
        MULTIPLIERS.put(Items.DEEPSLATE, 48.0);
        MULTIPLIERS.put(Items.COBBLED_DEEPSLATE, 48.0);
        MULTIPLIERS.put(Items.SAND, 64.0);
        MULTIPLIERS.put(Items.RED_SAND, 48.0);
        MULTIPLIERS.put(Items.GRAVEL, 64.0);
        MULTIPLIERS.put(Items.NETHERRACK, 64.0);
        MULTIPLIERS.put(Items.SOUL_SAND, 32.0);
        MULTIPLIERS.put(Items.SOUL_SOIL, 32.0);
        MULTIPLIERS.put(Items.BASALT, 48.0);
        MULTIPLIERS.put(Items.SMOOTH_BASALT, 32.0);
        MULTIPLIERS.put(Items.BLACKSTONE, 48.0);
        MULTIPLIERS.put(Items.ANDESITE, 64.0);
        MULTIPLIERS.put(Items.DIORITE, 64.0);
        MULTIPLIERS.put(Items.GRANITE, 64.0);
        MULTIPLIERS.put(Items.TUFF, 64.0);
        MULTIPLIERS.put(Items.CALCITE, 48.0);
        MULTIPLIERS.put(Items.DRIPSTONE_BLOCK, 32.0);
        MULTIPLIERS.put(Items.POINTED_DRIPSTONE, 24.0);

        MULTIPLIERS.put(Items.OAK_LOG, 48.0);
        MULTIPLIERS.put(Items.SPRUCE_LOG, 48.0);
        MULTIPLIERS.put(Items.BIRCH_LOG, 48.0);
        MULTIPLIERS.put(Items.JUNGLE_LOG, 48.0);
        MULTIPLIERS.put(Items.ACACIA_LOG, 48.0);
        MULTIPLIERS.put(Items.DARK_OAK_LOG, 48.0);
        MULTIPLIERS.put(Items.MANGROVE_LOG, 48.0);
        MULTIPLIERS.put(Items.CHERRY_LOG, 48.0);
        MULTIPLIERS.put(Items.OAK_WOOD, 32.0);
        MULTIPLIERS.put(Items.SPRUCE_WOOD, 32.0);
        MULTIPLIERS.put(Items.BIRCH_WOOD, 32.0);
        MULTIPLIERS.put(Items.JUNGLE_WOOD, 32.0);
        MULTIPLIERS.put(Items.ACACIA_WOOD, 32.0);
        MULTIPLIERS.put(Items.DARK_OAK_WOOD, 32.0);
        MULTIPLIERS.put(Items.MANGROVE_WOOD, 32.0);
        MULTIPLIERS.put(Items.CHERRY_WOOD, 32.0);
        MULTIPLIERS.put(Items.OAK_PLANKS, 48.0);
        MULTIPLIERS.put(Items.SPRUCE_PLANKS, 48.0);
        MULTIPLIERS.put(Items.BIRCH_PLANKS, 48.0);
        MULTIPLIERS.put(Items.JUNGLE_PLANKS, 48.0);
        MULTIPLIERS.put(Items.ACACIA_PLANKS, 48.0);
        MULTIPLIERS.put(Items.DARK_OAK_PLANKS, 48.0);
        MULTIPLIERS.put(Items.MANGROVE_PLANKS, 48.0);
        MULTIPLIERS.put(Items.CHERRY_PLANKS, 48.0);
        MULTIPLIERS.put(Items.BAMBOO, 32.0);
        MULTIPLIERS.put(Items.BAMBOO_BLOCK, 24.0);
        MULTIPLIERS.put(Items.BAMBOO_PLANKS, 32.0);

        MULTIPLIERS.put(Items.STONE_BRICKS, 48.0);
        MULTIPLIERS.put(Items.MOSSY_STONE_BRICKS, 32.0);
        MULTIPLIERS.put(Items.CRACKED_STONE_BRICKS, 32.0);
        MULTIPLIERS.put(Items.CHISELED_STONE_BRICKS, 32.0);
        MULTIPLIERS.put(Items.DEEPSLATE_BRICKS, 32.0);
        MULTIPLIERS.put(Items.DEEPSLATE_TILES, 32.0);
        MULTIPLIERS.put(Items.POLISHED_ANDESITE, 48.0);
        MULTIPLIERS.put(Items.POLISHED_DIORITE, 48.0);
        MULTIPLIERS.put(Items.POLISHED_GRANITE, 48.0);
        MULTIPLIERS.put(Items.POLISHED_DEEPSLATE, 32.0);
        MULTIPLIERS.put(Items.POLISHED_BLACKSTONE, 32.0);
        MULTIPLIERS.put(Items.POLISHED_BASALT, 32.0);

        MULTIPLIERS.put(Items.WHEAT, 32.0);
        MULTIPLIERS.put(Items.WHEAT_SEEDS, 32.0);
        MULTIPLIERS.put(Items.POTATO, 32.0);
        MULTIPLIERS.put(Items.CARROT, 32.0);
        MULTIPLIERS.put(Items.BEETROOT, 32.0);
        MULTIPLIERS.put(Items.BEETROOT_SEEDS, 32.0);
        MULTIPLIERS.put(Items.PUMPKIN, 24.0);
        MULTIPLIERS.put(Items.CARVED_PUMPKIN, 24.0);
        MULTIPLIERS.put(Items.MELON, 24.0);
        MULTIPLIERS.put(Items.MELON_SLICE, 32.0);
        MULTIPLIERS.put(Items.SUGAR_CANE, 32.0);
        MULTIPLIERS.put(Items.CACTUS, 24.0);
        MULTIPLIERS.put(Items.KELP, 32.0);
        MULTIPLIERS.put(Items.DRIED_KELP, 32.0);
        MULTIPLIERS.put(Items.SWEET_BERRIES, 24.0);
        MULTIPLIERS.put(Items.GLOW_BERRIES, 24.0);
        MULTIPLIERS.put(Items.COCOA_BEANS, 16.0);
        MULTIPLIERS.put(Items.NETHER_WART, 16.0);

        MULTIPLIERS.put(Items.COAL, 24.0);
        MULTIPLIERS.put(Items.CHARCOAL, 24.0);
        MULTIPLIERS.put(Items.RAW_IRON, 20.0);
        MULTIPLIERS.put(Items.IRON_INGOT, 16.0);
        MULTIPLIERS.put(Items.IRON_BLOCK, 12.0);
        MULTIPLIERS.put(Items.RAW_COPPER, 24.0);
        MULTIPLIERS.put(Items.COPPER_INGOT, 20.0);
        MULTIPLIERS.put(Items.COPPER_BLOCK, 16.0);
        MULTIPLIERS.put(Items.RAW_GOLD, 12.0);
        MULTIPLIERS.put(Items.GOLD_INGOT, 8.0);
        MULTIPLIERS.put(Items.GOLD_BLOCK, 6.0);
        MULTIPLIERS.put(Items.FLINT, 24.0);

        MULTIPLIERS.put(Items.REDSTONE, 16.0);
        MULTIPLIERS.put(Items.REDSTONE_BLOCK, 12.0);
        MULTIPLIERS.put(Items.LAPIS_LAZULI, 16.0);
        MULTIPLIERS.put(Items.LAPIS_BLOCK, 12.0);
        MULTIPLIERS.put(Items.DIAMOND, 4.0);
        MULTIPLIERS.put(Items.DIAMOND_BLOCK, 3.0);
        MULTIPLIERS.put(Items.EMERALD, 8.0);
        MULTIPLIERS.put(Items.EMERALD_BLOCK, 6.0);
        MULTIPLIERS.put(Items.QUARTZ, 16.0);
        MULTIPLIERS.put(Items.QUARTZ_BLOCK, 12.0);
        MULTIPLIERS.put(Items.AMETHYST_SHARD, 16.0);
        MULTIPLIERS.put(Items.AMETHYST_BLOCK, 12.0);

        MULTIPLIERS.put(Items.CLAY_BALL, 24.0);
        MULTIPLIERS.put(Items.CLAY, 20.0);
        MULTIPLIERS.put(Items.BRICK, 20.0);
        MULTIPLIERS.put(Items.BRICKS, 16.0);
        MULTIPLIERS.put(Items.TERRACOTTA, 24.0);
        MULTIPLIERS.put(Items.WHITE_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.ORANGE_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.MAGENTA_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.LIGHT_BLUE_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.YELLOW_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.LIME_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.PINK_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.GRAY_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.LIGHT_GRAY_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.CYAN_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.PURPLE_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.BLUE_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.BROWN_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.GREEN_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.RED_TERRACOTTA, 20.0);
        MULTIPLIERS.put(Items.BLACK_TERRACOTTA, 20.0);

        MULTIPLIERS.put(Items.SNOWBALL, 32.0);
        MULTIPLIERS.put(Items.SNOW_BLOCK, 24.0);
        MULTIPLIERS.put(Items.ICE, 24.0);
        MULTIPLIERS.put(Items.PACKED_ICE, 20.0);
        MULTIPLIERS.put(Items.BLUE_ICE, 12.0);
        MULTIPLIERS.put(Items.MOSS_BLOCK, 24.0);
        MULTIPLIERS.put(Items.MOSS_CARPET, 24.0);

        MULTIPLIERS.put(Items.BONE, 16.0);
        MULTIPLIERS.put(Items.BONE_MEAL, 20.0);
        MULTIPLIERS.put(Items.BONE_BLOCK, 12.0);
        MULTIPLIERS.put(Items.ROTTEN_FLESH, 32.0);
        MULTIPLIERS.put(Items.STRING, 20.0);
        MULTIPLIERS.put(Items.SPIDER_EYE, 16.0);
        MULTIPLIERS.put(Items.GUNPOWDER, 12.0);
        MULTIPLIERS.put(Items.ARROW, 24.0);
        MULTIPLIERS.put(Items.FEATHER, 16.0);
        MULTIPLIERS.put(Items.EGG, 24.0);
        MULTIPLIERS.put(Items.LEATHER, 6.5);
        MULTIPLIERS.put(Items.RABBIT_HIDE, 12.0);
        MULTIPLIERS.put(Items.PHANTOM_MEMBRANE, 4.0);

        MULTIPLIERS.put(Items.SLIME_BALL, 8.0);
        MULTIPLIERS.put(Items.SLIME_BLOCK, 6.0);
        MULTIPLIERS.put(Items.MAGMA_CREAM, 8.0);
        MULTIPLIERS.put(Items.BLAZE_ROD, 6.0);
        MULTIPLIERS.put(Items.BLAZE_POWDER, 12.0);
        MULTIPLIERS.put(Items.ENDER_PEARL, 8.0);
        MULTIPLIERS.put(Items.ENDER_EYE, 6.0);
        MULTIPLIERS.put(Items.GHAST_TEAR, 4.0);

        MULTIPLIERS.put(Items.PRISMARINE_SHARD, 12.0);
        MULTIPLIERS.put(Items.PRISMARINE_CRYSTALS, 10.0);
        MULTIPLIERS.put(Items.PRISMARINE, 10.0);
        MULTIPLIERS.put(Items.PRISMARINE_BRICKS, 8.0);
        MULTIPLIERS.put(Items.DARK_PRISMARINE, 8.0);
        MULTIPLIERS.put(Items.SEA_LANTERN, 6.0);
        MULTIPLIERS.put(Items.NAUTILUS_SHELL, 3.0);
        MULTIPLIERS.put(Items.HEART_OF_THE_SEA, 1.0);

        MULTIPLIERS.put(Items.SCUTE, 6.0);
        MULTIPLIERS.put(Items.TURTLE_EGG, 4.0);
        MULTIPLIERS.put(Items.SNIFFER_EGG, 2.0);
        MULTIPLIERS.put(Items.RABBIT_FOOT, 6.0);

        MULTIPLIERS.put(Items.OBSIDIAN, 12.0);
        MULTIPLIERS.put(Items.CRYING_OBSIDIAN, 8.0);
        MULTIPLIERS.put(Items.GLOWSTONE_DUST, 16.0);
        MULTIPLIERS.put(Items.GLOWSTONE, 12.0);

        MULTIPLIERS.put(Items.HONEYCOMB, 12.0);
        MULTIPLIERS.put(Items.HONEY_BOTTLE, 8.0);
        MULTIPLIERS.put(Items.HONEY_BLOCK, 6.0);
        MULTIPLIERS.put(Items.HONEYCOMB_BLOCK, 8.0);

        MULTIPLIERS.put(Items.INK_SAC, 12.0);
        MULTIPLIERS.put(Items.GLOW_INK_SAC, 8.0);

        MULTIPLIERS.put(Items.WARPED_FUNGUS, 16.0);
        MULTIPLIERS.put(Items.CRIMSON_FUNGUS, 16.0);
        MULTIPLIERS.put(Items.WARPED_ROOTS, 20.0);
        MULTIPLIERS.put(Items.CRIMSON_ROOTS, 20.0);
        MULTIPLIERS.put(Items.NETHER_SPROUTS, 16.0);
        MULTIPLIERS.put(Items.WEEPING_VINES, 16.0);
        MULTIPLIERS.put(Items.TWISTING_VINES, 16.0);
        MULTIPLIERS.put(Items.SHROOMLIGHT, 12.0);

        MULTIPLIERS.put(Items.CHORUS_FRUIT, 12.0);
        MULTIPLIERS.put(Items.POPPED_CHORUS_FRUIT, 10.0);
        MULTIPLIERS.put(Items.CHORUS_FLOWER, 8.0);

        MULTIPLIERS.put(Items.ECHO_SHARD, 2.0);
        MULTIPLIERS.put(Items.DISC_FRAGMENT_5, 3.0);

        MULTIPLIERS.put(Items.ANCIENT_DEBRIS, 2.0);
        MULTIPLIERS.put(Items.NETHERITE_SCRAP, 1.5);
        MULTIPLIERS.put(Items.NETHERITE_INGOT, 1.0);

        MULTIPLIERS.put(Items.SHULKER_SHELL, 2.0);
        MULTIPLIERS.put(Items.DRAGON_BREATH, 4.0);
        MULTIPLIERS.put(Items.DRAGON_EGG, 1.0);

        MULTIPLIERS.put(Items.TOTEM_OF_UNDYING, 1.0);
        MULTIPLIERS.put(Items.NETHER_STAR, 1.0);
        MULTIPLIERS.put(Items.ELYTRA, 1.0);
        MULTIPLIERS.put(Items.TRIDENT, 1.0);

        MULTIPLIERS.put(Items.ENCHANTED_GOLDEN_APPLE, 1.0);
        MULTIPLIERS.put(Items.GOLDEN_APPLE, 8.0);
        MULTIPLIERS.put(Items.APPLE, 20.0);

        MULTIPLIERS.put(Items.NAME_TAG, 4.0);
        MULTIPLIERS.put(Items.SADDLE, 4.0);
        MULTIPLIERS.put(Items.ENCHANTED_BOOK, 6.0);

        MULTIPLIERS.put(Items.MUSIC_DISC_13, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_CAT, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_BLOCKS, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_CHIRP, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_FAR, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_MALL, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_MELLOHI, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_STAL, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_STRAD, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_WARD, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_11, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_WAIT, 2.0);
        MULTIPLIERS.put(Items.MUSIC_DISC_OTHERSIDE, 1.5);
        MULTIPLIERS.put(Items.MUSIC_DISC_5, 1.5);
        MULTIPLIERS.put(Items.MUSIC_DISC_PIGSTEP, 1.5);

        MULTIPLIERS.put(Items.GLASS, 24.0);
        MULTIPLIERS.put(Items.GLASS_PANE, 32.0);
        MULTIPLIERS.put(Items.WHITE_STAINED_GLASS, 20.0);
        MULTIPLIERS.put(Items.BLACK_STAINED_GLASS, 20.0);
        MULTIPLIERS.put(Items.TINTED_GLASS, 16.0);

        MULTIPLIERS.put(Items.WHITE_WOOL, 16.0);
        MULTIPLIERS.put(Items.BLACK_WOOL, 16.0);
        MULTIPLIERS.put(Items.BLUE_WOOL, 16.0);
        MULTIPLIERS.put(Items.RED_WOOL, 16.0);
        MULTIPLIERS.put(Items.GREEN_WOOL, 16.0);
        MULTIPLIERS.put(Items.YELLOW_WOOL, 16.0);

        MULTIPLIERS.put(Items.WHITE_CONCRETE, 20.0);
        MULTIPLIERS.put(Items.BLACK_CONCRETE, 20.0);
        MULTIPLIERS.put(Items.BLUE_CONCRETE, 20.0);
        MULTIPLIERS.put(Items.RED_CONCRETE, 20.0);

        MULTIPLIERS.put(Items.PAPER, 20.0);
        MULTIPLIERS.put(Items.BOOK, 12.0);
        MULTIPLIERS.put(Items.BOOKSHELF, 10.0);

        MULTIPLIERS.put(Items.END_STONE, 32.0);
        MULTIPLIERS.put(Items.END_STONE_BRICKS, 24.0);
        MULTIPLIERS.put(Items.PURPUR_BLOCK, 16.0);
        MULTIPLIERS.put(Items.PURPUR_PILLAR, 16.0);

        MULTIPLIERS.put(Items.MAGMA_BLOCK, 16.0);
        MULTIPLIERS.put(Items.NETHER_BRICKS, 24.0);
        MULTIPLIERS.put(Items.RED_NETHER_BRICKS, 20.0);
        MULTIPLIERS.put(Items.CHISELED_NETHER_BRICKS, 16.0);

        setupRerollItems();
    }

    private static void setupRerollItems() {
        REROLL_ITEMS.add(Items.NETHER_STAR);
        REROLL_ITEMS.add(Items.TOTEM_OF_UNDYING);
        REROLL_ITEMS.add(Items.DRAGON_EGG);
        REROLL_ITEMS.add(Items.ELYTRA);
        REROLL_ITEMS.add(Items.ENCHANTED_GOLDEN_APPLE);
        REROLL_ITEMS.add(Items.HEART_OF_THE_SEA);
    }

    public static boolean isRerollItem(Item item) {
        return REROLL_ITEMS.contains(item);
    }

    public static Item getRandomItem(Random random) {
        List<Item> items = new ArrayList<>(MULTIPLIERS.keySet());
        if (items.isEmpty()) return Items.DIAMOND;
        return items.get(random.nextInt(items.size()));
    }

    public static int getRequiredCount(Item item, int completionCount) {
        Double multiplier = MULTIPLIERS.get(item);
        if (multiplier == null) return 16;

        int powerIncrease = completionCount / 10;
        int baseCount = (int) Math.ceil(multiplier);

        int finalCount = baseCount * (int)Math.pow(2, powerIncrease);

        return Math.min(finalCount, 4096);
    }
}
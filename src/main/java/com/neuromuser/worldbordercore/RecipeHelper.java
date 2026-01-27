package com.neuromuser.worldbordercore;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeHelper {
    private static final Map<Item, Set<Item>> BLOCK_DROP_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Set<Item>> SMELTING_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Set<Item>> BLASTING_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Set<Item>> SMOKING_CACHE = new ConcurrentHashMap<>();
    private static final Map<Block, Item> BLOCK_TO_ITEM_OVERRIDE = new HashMap<>();

    private static ServerWorld cachedWorld;
    private static boolean initialized = false;

    public static void initialize(ServerWorld world) {
        if (initialized && cachedWorld == world) return;

        cachedWorld = world;
        initialized = true;

        SMELTING_CACHE.clear();
        BLASTING_CACHE.clear();
        SMOKING_CACHE.clear();

        buildBlockToItemOverrides();
        buildRecipeCaches(world);
    }

    private static void buildBlockToItemOverrides() {
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DIAMOND_ORE, Items.DIAMOND);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.IRON_ORE, Items.RAW_IRON);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_IRON_ORE, Items.RAW_IRON);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.GOLD_ORE, Items.RAW_GOLD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.COPPER_ORE, Items.RAW_COPPER);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.COAL_ORE, Items.COAL);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_COAL_ORE, Items.COAL);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.REDSTONE_ORE, Items.REDSTONE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.LAPIS_ORE, Items.LAPIS_LAZULI);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.EMERALD_ORE, Items.EMERALD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DEEPSLATE_EMERALD_ORE, Items.EMERALD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.NETHER_QUARTZ_ORE, Items.QUARTZ);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.NETHER_GOLD_ORE, Items.GOLD_NUGGET);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.ANCIENT_DEBRIS, Items.ANCIENT_DEBRIS);

        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.AMETHYST_CLUSTER, Items.AMETHYST_SHARD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.LARGE_AMETHYST_BUD, Items.AMETHYST_SHARD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.MEDIUM_AMETHYST_BUD, Items.AMETHYST_SHARD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SMALL_AMETHYST_BUD, Items.AMETHYST_SHARD);

        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.GLOWSTONE, Items.GLOWSTONE_DUST);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SEA_LANTERN, Items.PRISMARINE_CRYSTALS);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.MELON, Items.MELON_SLICE);

        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.HAY_BLOCK, Items.WHEAT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.COAL_BLOCK, Items.COAL);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.IRON_BLOCK, Items.IRON_INGOT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.GOLD_BLOCK, Items.GOLD_INGOT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.COPPER_BLOCK, Items.COPPER_INGOT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DIAMOND_BLOCK, Items.DIAMOND);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.EMERALD_BLOCK, Items.EMERALD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.LAPIS_BLOCK, Items.LAPIS_LAZULI);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.REDSTONE_BLOCK, Items.REDSTONE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.NETHERITE_BLOCK, Items.NETHERITE_INGOT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.RAW_IRON_BLOCK, Items.RAW_IRON);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.RAW_COPPER_BLOCK, Items.RAW_COPPER);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.RAW_GOLD_BLOCK, Items.RAW_GOLD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.AMETHYST_BLOCK, Items.AMETHYST_SHARD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.BONE_BLOCK, Items.BONE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SLIME_BLOCK, Items.SLIME_BALL);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.HONEY_BLOCK, Items.HONEY_BOTTLE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DRIED_KELP_BLOCK, Items.DRIED_KELP);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SNOW_BLOCK, Items.SNOWBALL);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SNOW, Items.SNOWBALL);

        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.WHEAT, Items.WHEAT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CARROTS, Items.CARROT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.POTATOES, Items.POTATO);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.BEETROOTS, Items.BEETROOT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SUGAR_CANE, Items.SUGAR_CANE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.BAMBOO, Items.BAMBOO);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.COCOA, Items.COCOA_BEANS);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.NETHER_WART, Items.NETHER_WART);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CACTUS, Items.CACTUS);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.KELP, Items.KELP);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.KELP_PLANT, Items.KELP);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SEA_PICKLE, Items.SEA_PICKLE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.VINE, Items.VINE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.LILY_PAD, Items.LILY_PAD);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.BIG_DRIPLEAF, Items.BIG_DRIPLEAF);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.BIG_DRIPLEAF_STEM, Items.BIG_DRIPLEAF);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SMALL_DRIPLEAF, Items.SMALL_DRIPLEAF);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.HANGING_ROOTS, Items.HANGING_ROOTS);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.GLOW_LICHEN, Items.GLOW_LICHEN);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SPORE_BLOSSOM, Items.SPORE_BLOSSOM);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CHORUS_FLOWER, Items.CHORUS_FRUIT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CHORUS_PLANT, Items.CHORUS_FRUIT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CAVE_VINES, Items.GLOW_BERRIES);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CAVE_VINES_PLANT, Items.GLOW_BERRIES);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.PINK_PETALS, Items.PINK_PETALS);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.TORCHFLOWER, Items.TORCHFLOWER);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.TORCHFLOWER_CROP, Items.TORCHFLOWER);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.PITCHER_CROP, Items.PITCHER_PLANT);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.PITCHER_PLANT, Items.PITCHER_PLANT);

        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.OAK_LEAVES, Items.OAK_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.SPRUCE_LEAVES, Items.SPRUCE_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.BIRCH_LEAVES, Items.BIRCH_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.JUNGLE_LEAVES, Items.JUNGLE_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.ACACIA_LEAVES, Items.ACACIA_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.DARK_OAK_LEAVES, Items.DARK_OAK_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.MANGROVE_LEAVES, Items.MANGROVE_PROPAGULE);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CHERRY_LEAVES, Items.CHERRY_SAPLING);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.AZALEA_LEAVES, Items.AZALEA);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.FLOWERING_AZALEA_LEAVES, Items.FLOWERING_AZALEA);

        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.CRIMSON_NYLIUM, Items.CRIMSON_FUNGUS);
        BLOCK_TO_ITEM_OVERRIDE.put(Blocks.WARPED_NYLIUM, Items.WARPED_FUNGUS);
    }

    private static void buildRecipeCaches(ServerWorld world) {
        RecipeManager recipeManager = world.getServer().getRecipeManager();

        for (Recipe<?> recipe : recipeManager.values()) {

            if (recipe instanceof SmeltingRecipe smeltingRecipe) {
                ItemStack output = smeltingRecipe.getOutput(world.getRegistryManager());
                for (Ingredient ingredient : smeltingRecipe.getIngredients()) {
                    for (ItemStack inputStack : ingredient.getMatchingStacks()) {
                        Item input = inputStack.getItem();
                        SMELTING_CACHE.computeIfAbsent(input, k -> new HashSet<>()).add(output.getItem());
                    }
                }
            }
            else if (recipe instanceof BlastingRecipe blastingRecipe) {
                ItemStack output = blastingRecipe.getOutput(world.getRegistryManager());
                for (Ingredient ingredient : blastingRecipe.getIngredients()) {
                    for (ItemStack inputStack : ingredient.getMatchingStacks()) {
                        Item input = inputStack.getItem();
                        BLASTING_CACHE.computeIfAbsent(input, k -> new HashSet<>()).add(output.getItem());
                    }
                }
            }
            else if (recipe instanceof SmokingRecipe smokingRecipe) {
                ItemStack output = smokingRecipe.getOutput(world.getRegistryManager());
                for (Ingredient ingredient : smokingRecipe.getIngredients()) {
                    for (ItemStack inputStack : ingredient.getMatchingStacks()) {
                        Item input = inputStack.getItem();
                        SMOKING_CACHE.computeIfAbsent(input, k -> new HashSet<>()).add(output.getItem());
                    }
                }
            }
        }
    }

    public static Item getItemFromBlock(BlockState state) {
        Block block = state.getBlock();

        if (BLOCK_TO_ITEM_OVERRIDE.containsKey(block)) {
            return BLOCK_TO_ITEM_OVERRIDE.get(block);
        }

        return block.asItem();
    }

    public static Set<Item> getProcessedForms(Item rawItem) {
        Set<Item> results = new HashSet<>();

        if (SMELTING_CACHE.containsKey(rawItem)) {
            results.addAll(SMELTING_CACHE.get(rawItem));
        }

        if (BLASTING_CACHE.containsKey(rawItem)) {
            results.addAll(BLASTING_CACHE.get(rawItem));
        }

        if (SMOKING_CACHE.containsKey(rawItem)) {
            results.addAll(SMOKING_CACHE.get(rawItem));
        }

        addManualProcessedForms(rawItem, results);

        return results;
    }

    private static void addManualProcessedForms(Item rawItem, Set<Item> results) {
        if (rawItem == Items.GOLD_NUGGET) {
            results.add(Items.GOLD_INGOT);
        }

        if (rawItem == Items.CACTUS) {
            results.add(Items.GREEN_DYE);
        }

        if (rawItem == Items.SAND) {
            results.add(Items.GLASS);
        }

        if (rawItem == Items.CLAY_BALL) {
            results.add(Items.BRICK);
        }

        String itemName = Registries.ITEM.getId(rawItem).getPath();
        if (itemName.endsWith("_log") || itemName.endsWith("_stem") || itemName.equals("wood")) {
            results.add(Items.CHARCOAL);
        }
    }

    public static Map<Item, Integer> getProcessedItemsWithCounts(Map<Item, Integer> baseResources) {
        Map<Item, Integer> processed = new HashMap<>(baseResources);

        for (Map.Entry<Item, Integer> entry : baseResources.entrySet()) {
            Item item = entry.getKey();
            int count = entry.getValue();

            Set<Item> processedForms = getProcessedForms(item);
            for (Item processedItem : processedForms) {
                if (processedItem != item) {
                    processed.merge(processedItem, count, Integer::sum);
                }
            }
        }

        return processed;
    }

    public static void clearCaches() {
        SMELTING_CACHE.clear();
        BLASTING_CACHE.clear();
        SMOKING_CACHE.clear();
        BLOCK_DROP_CACHE.clear();
        initialized = false;
    }
}
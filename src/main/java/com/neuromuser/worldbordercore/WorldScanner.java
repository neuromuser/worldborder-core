package com.neuromuser.worldbordercore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldScanner {
    private static final Map<Item, Integer> availableResources = new ConcurrentHashMap<>();
    private static final Set<Item> unobtainableItems = new HashSet<>();
    private static final Set<Item> rerollItems = new HashSet<>();

    private static boolean isScanning = false;
    private static boolean scanned = false;
    private static boolean scanningNether = false;
    private static double lastScannedSize = 0;
    private static double lastScannedCenterX = 0;
    private static double lastScannedCenterZ = 0;

    private static int currentX, currentY, currentZ;
    private static int maxX;
    private static int minY;
    private static int maxY;
    private static int minZ;
    private static int maxZ;
    private static int innerMinX, innerMaxX, innerMinZ, innerMaxZ;
    private static boolean hasInnerBounds = false;
    private static long totalBlocks = 0;
    private static long scannedBlocks = 0;
    private static final int BLOCKS_PER_TICK = 5000;
    private static final int CHESTS_PER_TICK = 100;

    private static WorldBorder currentBorder;
    private static final List<BlockPos> chestPositions = new ArrayList<>();
    private static int currentChestIndex = 0;
    private static boolean scanningChests = false;
    private static boolean scanningPlayers = false;
    private static final List<ServerPlayerEntity> playersToScan = new ArrayList<>();
    private static int currentPlayerIndex = 0;

    private static final Map<Item, Integer> persistentResources = new ConcurrentHashMap<>();
    private static int currentPhase = 0;
    private static boolean persistentScanned = false;
    private static double persistentLastScannedSize = 0;
    private static double persistentLastScannedCenterX = 0;
    private static double persistentLastScannedCenterZ = 0;

    public static void initialize() {
        setupUnobtainableItems();
        setupRerollItems();
        ItemConfig.load();
    }

    private static void setupUnobtainableItems() {
        unobtainableItems.add(Items.INFESTED_STONE);
        unobtainableItems.add(Items.INFESTED_COBBLESTONE);
        unobtainableItems.add(Items.INFESTED_STONE_BRICKS);
        unobtainableItems.add(Items.INFESTED_MOSSY_STONE_BRICKS);
        unobtainableItems.add(Items.INFESTED_CRACKED_STONE_BRICKS);
        unobtainableItems.add(Items.INFESTED_CHISELED_STONE_BRICKS);
        unobtainableItems.add(Items.INFESTED_DEEPSLATE);
        unobtainableItems.add(Items.BEDROCK);
        unobtainableItems.add(Items.BARRIER);
        unobtainableItems.add(Items.STRUCTURE_VOID);
        unobtainableItems.add(Items.STRUCTURE_BLOCK);
        unobtainableItems.add(Items.COMMAND_BLOCK);
        unobtainableItems.add(Items.CHAIN_COMMAND_BLOCK);
        unobtainableItems.add(Items.REPEATING_COMMAND_BLOCK);
        unobtainableItems.add(Items.JIGSAW);
        unobtainableItems.add(Items.LIGHT);
        unobtainableItems.add(Items.SPAWNER);
        unobtainableItems.add(Items.BUDDING_AMETHYST);
        unobtainableItems.add(Items.REINFORCED_DEEPSLATE);
        unobtainableItems.add(Items.AIR);
    }

    private static void setupRerollItems() {
        rerollItems.add(Items.DIAMOND_BLOCK);
        rerollItems.add(Items.NETHERITE_INGOT);
        rerollItems.add(Items.BEACON);
        rerollItems.add(Items.ENCHANTED_GOLDEN_APPLE);
    }

    public static void startScan(ServerWorld world) {
        WorldBorder border = world.getWorldBorder();
        double size = border.getSize();

        boolean isIncrementalScan = (lastScannedSize > 0 && lastScannedSize < size);

        savePersistentState();
        if (!isIncrementalScan) {
            availableResources.clear();
        }

        currentBorder = border;
        scanningNether = false;
        currentPhase = 0;

        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();

        if (size > 59999900) {
            return;
        }

        double radius = size / 2.0;
        int minX = (int) Math.floor(centerX - radius);
        maxX = (int) Math.ceil(centerX + radius);
        minZ = (int) Math.floor(centerZ - radius);
        maxZ = (int) Math.ceil(centerZ + radius);
        minY = world.getBottomY();
        maxY = world.getTopY();

        if (lastScannedSize > 0) {
            double lastRadius = lastScannedSize / 2.0;
            innerMinX = (int) Math.floor(lastScannedCenterX - lastRadius);
            innerMaxX = (int) Math.ceil(lastScannedCenterX + lastRadius);
            innerMinZ = (int) Math.floor(lastScannedCenterZ - lastRadius);
            innerMaxZ = (int) Math.ceil(lastScannedCenterZ + lastRadius);
            hasInnerBounds = true;
        } else {
            hasInnerBounds = false;
        }

        currentX = minX;
        currentY = minY;
        currentZ = minZ;
        scannedBlocks = 0;

        chestPositions.clear();
        currentChestIndex = 0;
        scanningChests = false;
        scanningPlayers = false;
        playersToScan.clear();
        currentPlayerIndex = 0;

        long rangeX = maxX - minX;
        long rangeY = maxY - minY;
        long rangeZ = maxZ - minZ;
        totalBlocks = rangeX * rangeY * rangeZ;

        if (hasInnerBounds) {
            long innerRangeX = innerMaxX - innerMinX;
            long innerRangeZ = innerMaxZ - innerMinZ;
            long skippedBlocks = innerRangeX * rangeY * innerRangeZ;
            totalBlocks -= skippedBlocks;
        }

        isScanning = true;
        scanned = false;

        lastScannedSize = size;
        lastScannedCenterX = centerX;
        lastScannedCenterZ = centerZ;
    }

    public static void tick(ServerWorld world) {
        if (!isScanning) return;

        if (scanningPlayers) {
            currentPhase = 3;
            scanPlayerInventories();
            return;
        }

        if (scanningChests) {
            currentPhase = 2;
            scanChests(world);
            return;
        }

        currentPhase = 1;
        int blocksThisTick = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (blocksThisTick < BLOCKS_PER_TICK && isScanning && !scanningChests) {
            if (shouldScanBlock(currentX, currentZ)) {
                boolean shouldCount = scanningNether ||
                        (currentBorder != null && currentBorder.contains(currentX, currentZ));

                if (shouldCount) {
                    int chunkX = currentX >> 4;
                    int chunkZ = currentZ >> 4;

                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        pos.set(currentX, currentY, currentZ);

                        try {
                            Chunk chunk = world.getChunk(chunkX, chunkZ);
                            BlockState state = chunk.getBlockState(pos);

                            Item item = getItemFromBlock(state);
                            if (item != null && !unobtainableItems.contains(item)) {
                                availableResources.merge(item, 1, Integer::sum);

                                Item processed = getProcessedForm(item);
                                if (processed != null && processed != item) {
                                    availableResources.merge(processed, 1, Integer::sum);
                                }
                            }

                            if (isContainerBlock(state)) {
                                chestPositions.add(pos.toImmutable());
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                blocksThisTick++;
                scannedBlocks++;
            }

            currentY++;
            if (currentY >= maxY) {
                currentY = minY;
                currentZ++;
                if (currentZ >= maxZ) {
                    currentZ = minZ;
                    currentX++;
                    if (currentX >= maxX) {
                        startChestScanning();
                        return;
                    }
                }
            }
        }
    }

    private static void startChestScanning() {
        scanningChests = true;
        currentChestIndex = 0;
        currentPhase = 2;
    }

    private static void scanChests(ServerWorld world) {
        int chestsThisTick = 0;

        while (currentChestIndex < chestPositions.size() && chestsThisTick < CHESTS_PER_TICK) {
            BlockPos pos = chestPositions.get(currentChestIndex);

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            if (world.isChunkLoaded(chunkX, chunkZ)) {
                try {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity != null) {
                        scanContainer(blockEntity);
                    }
                } catch (Exception ignored) {
                }
            }

            currentChestIndex++;
            chestsThisTick++;
        }

        if (currentChestIndex >= chestPositions.size()) {
            scanningChests = false;
            startPlayerScanning(world);
        }
    }

    private static void startPlayerScanning(ServerWorld world) {
        scanningPlayers = true;
        currentPlayerIndex = 0;
        currentPhase = 3;

        playersToScan.clear();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (currentBorder == null || currentBorder.contains(player.getX(), player.getZ())) {
                playersToScan.add(player);
            }
        }
    }

    private static void scanPlayerInventories() {
        if (currentPlayerIndex >= playersToScan.size()) {
            finishScan();
            return;
        }

        ServerPlayerEntity player = playersToScan.get(currentPlayerIndex);

        try {
            PlayerInventory inventory = player.getInventory();
            scanInventory(inventory);

            Inventory enderChest = player.getEnderChestInventory();
            if (enderChest != null) {
                scanInventory(enderChest);
            }
        } catch (Exception ignored) {
        }

        currentPlayerIndex++;
    }

    private static void scanContainer(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity chest) {
            scanInventory(chest);
        } else if (blockEntity instanceof BarrelBlockEntity barrel) {
            scanInventory(barrel);
        } else if (blockEntity instanceof ShulkerBoxBlockEntity shulker) {
            scanInventory(shulker);
        } else if (blockEntity instanceof DispenserBlockEntity dispenser) {
            scanInventory(dispenser);
        } else if (blockEntity instanceof HopperBlockEntity hopper) {
            scanInventory(hopper);
        } else if (blockEntity instanceof FurnaceBlockEntity furnace) {
            scanInventory(furnace);
        } else if (blockEntity instanceof BrewingStandBlockEntity brewing) {
            scanInventory(brewing);
        } else if (blockEntity instanceof BlastFurnaceBlockEntity blast) {
            scanInventory(blast);
        } else if (blockEntity instanceof SmokerBlockEntity smoker) {
            scanInventory(smoker);
        }
    }

    private static void scanInventory(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            try {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (!unobtainableItems.contains(item)) {
                        availableResources.merge(item, stack.getCount(), Integer::sum);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean isContainerBlock(BlockState state) {
        return state.getBlock() instanceof net.minecraft.block.ChestBlock ||
                state.getBlock() instanceof net.minecraft.block.BarrelBlock ||
                state.getBlock() instanceof net.minecraft.block.ShulkerBoxBlock ||
                state.getBlock() instanceof net.minecraft.block.DispenserBlock ||
                state.getBlock() instanceof net.minecraft.block.DropperBlock ||
                state.getBlock() instanceof net.minecraft.block.HopperBlock ||
                state.getBlock() instanceof net.minecraft.block.FurnaceBlock ||
                state.getBlock() instanceof net.minecraft.block.BlastFurnaceBlock ||
                state.getBlock() instanceof net.minecraft.block.SmokerBlock ||
                state.getBlock() instanceof net.minecraft.block.BrewingStandBlock;
    }

    private static boolean shouldScanBlock(int x, int z) {
        if (!hasInnerBounds) return true;
        return x < innerMinX || x >= innerMaxX || z < innerMinZ || z >= innerMaxZ;
    }

    private static void finishScan() {
        isScanning = false;
        scanned = true;
        scanningChests = false;
        scanningPlayers = false;
        currentBorder = null;
        currentPhase = 4;

        if (scanningNether) {
            scanningNether = false;
        }

        savePersistentState();
    }

    private static Item getItemFromBlock(BlockState state) {
        if (state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) return Items.DIAMOND;
        if (state.isOf(Blocks.IRON_ORE) || state.isOf(Blocks.DEEPSLATE_IRON_ORE)) return Items.RAW_IRON;
        if (state.isOf(Blocks.GOLD_ORE) || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)) return Items.RAW_GOLD;
        if (state.isOf(Blocks.COPPER_ORE) || state.isOf(Blocks.DEEPSLATE_COPPER_ORE)) return Items.RAW_COPPER;
        if (state.isOf(Blocks.COAL_ORE) || state.isOf(Blocks.DEEPSLATE_COAL_ORE)) return Items.COAL;
        if (state.isOf(Blocks.REDSTONE_ORE) || state.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)) return Items.REDSTONE;
        if (state.isOf(Blocks.LAPIS_ORE) || state.isOf(Blocks.DEEPSLATE_LAPIS_ORE)) return Items.LAPIS_LAZULI;
        if (state.isOf(Blocks.EMERALD_ORE) || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)) return Items.EMERALD;
        if (state.isOf(Blocks.NETHER_QUARTZ_ORE)) return Items.QUARTZ;
        if (state.isOf(Blocks.NETHER_GOLD_ORE)) return Items.GOLD_NUGGET;
        if (state.isOf(Blocks.ANCIENT_DEBRIS)) return Items.ANCIENT_DEBRIS;
        if (state.isOf(Blocks.AMETHYST_CLUSTER) || state.isOf(Blocks.LARGE_AMETHYST_BUD) ||
                state.isOf(Blocks.MEDIUM_AMETHYST_BUD) || state.isOf(Blocks.SMALL_AMETHYST_BUD)) {
            return Items.AMETHYST_SHARD;
        }

        if (state.isOf(Blocks.GLOWSTONE)) return Items.GLOWSTONE_DUST;
        if (state.isOf(Blocks.SEA_LANTERN)) return Items.PRISMARINE_CRYSTALS;
        if (state.isOf(Blocks.SPONGE)) return Items.SPONGE;
        if (state.isOf(Blocks.WET_SPONGE)) return Items.WET_SPONGE;
        if (state.isOf(Blocks.MELON)) return Items.MELON_SLICE;
        if (state.isOf(Blocks.PUMPKIN) || state.isOf(Blocks.CARVED_PUMPKIN)) return Items.PUMPKIN;

        if (state.isOf(Blocks.HAY_BLOCK)) return Items.WHEAT;
        if (state.isOf(Blocks.COAL_BLOCK)) return Items.COAL;
        if (state.isOf(Blocks.IRON_BLOCK)) return Items.IRON_INGOT;
        if (state.isOf(Blocks.GOLD_BLOCK)) return Items.GOLD_INGOT;
        if (state.isOf(Blocks.COPPER_BLOCK)) return Items.COPPER_INGOT;
        if (state.isOf(Blocks.DIAMOND_BLOCK)) return Items.DIAMOND;
        if (state.isOf(Blocks.EMERALD_BLOCK)) return Items.EMERALD;
        if (state.isOf(Blocks.LAPIS_BLOCK)) return Items.LAPIS_LAZULI;
        if (state.isOf(Blocks.REDSTONE_BLOCK)) return Items.REDSTONE;
        if (state.isOf(Blocks.NETHERITE_BLOCK)) return Items.NETHERITE_INGOT;
        if (state.isOf(Blocks.RAW_IRON_BLOCK)) return Items.RAW_IRON;
        if (state.isOf(Blocks.RAW_COPPER_BLOCK)) return Items.RAW_COPPER;
        if (state.isOf(Blocks.RAW_GOLD_BLOCK)) return Items.RAW_GOLD;
        if (state.isOf(Blocks.AMETHYST_BLOCK)) return Items.AMETHYST_SHARD;
        if (state.isOf(Blocks.BONE_BLOCK)) return Items.BONE;
        if (state.isOf(Blocks.SLIME_BLOCK)) return Items.SLIME_BALL;
        if (state.isOf(Blocks.HONEY_BLOCK)) return Items.HONEY_BOTTLE;
        if (state.isOf(Blocks.DRIED_KELP_BLOCK)) return Items.DRIED_KELP;
        if (state.isOf(Blocks.SNOW_BLOCK) || state.isOf(Blocks.SNOW)) return Items.SNOWBALL;

        if (state.isOf(Blocks.WHEAT)) return Items.WHEAT;
        if (state.isOf(Blocks.CARROTS)) return Items.CARROT;
        if (state.isOf(Blocks.POTATOES)) return Items.POTATO;
        if (state.isOf(Blocks.BEETROOTS)) return Items.BEETROOT;
        if (state.isOf(Blocks.SUGAR_CANE)) return Items.SUGAR_CANE;
        if (state.isOf(Blocks.BAMBOO)) return Items.BAMBOO;
        if (state.isOf(Blocks.SWEET_BERRY_BUSH)) return Items.SWEET_BERRIES;
        if (state.isOf(Blocks.COCOA)) return Items.COCOA_BEANS;
        if (state.isOf(Blocks.NETHER_WART)) return Items.NETHER_WART;
        if (state.isOf(Blocks.CACTUS)) return Items.CACTUS;
        if (state.isOf(Blocks.KELP) || state.isOf(Blocks.KELP_PLANT)) return Items.KELP;
        if (state.isOf(Blocks.SEA_PICKLE)) return Items.SEA_PICKLE;
        if (state.isOf(Blocks.VINE)) return Items.VINE;
        if (state.isOf(Blocks.LILY_PAD)) return Items.LILY_PAD;
        if (state.isOf(Blocks.BIG_DRIPLEAF) || state.isOf(Blocks.BIG_DRIPLEAF_STEM)) return Items.BIG_DRIPLEAF;
        if (state.isOf(Blocks.SMALL_DRIPLEAF)) return Items.SMALL_DRIPLEAF;
        if (state.isOf(Blocks.HANGING_ROOTS)) return Items.HANGING_ROOTS;
        if (state.isOf(Blocks.GLOW_LICHEN)) return Items.GLOW_LICHEN;
        if (state.isOf(Blocks.SPORE_BLOSSOM)) return Items.SPORE_BLOSSOM;
        if (state.isOf(Blocks.CHORUS_FLOWER) || state.isOf(Blocks.CHORUS_PLANT)) return Items.CHORUS_FRUIT;
        if (state.isOf(Blocks.CAVE_VINES) || state.isOf(Blocks.CAVE_VINES_PLANT)) return Items.GLOW_BERRIES;

        if (state.isOf(Blocks.OAK_LEAVES)) return Items.OAK_SAPLING;
        if (state.isOf(Blocks.SPRUCE_LEAVES)) return Items.SPRUCE_SAPLING;
        if (state.isOf(Blocks.BIRCH_LEAVES)) return Items.BIRCH_SAPLING;
        if (state.isOf(Blocks.JUNGLE_LEAVES)) return Items.JUNGLE_SAPLING;
        if (state.isOf(Blocks.ACACIA_LEAVES)) return Items.ACACIA_SAPLING;
        if (state.isOf(Blocks.DARK_OAK_LEAVES)) return Items.DARK_OAK_SAPLING;
        if (state.isOf(Blocks.MANGROVE_LEAVES)) return Items.MANGROVE_PROPAGULE;
        if (state.isOf(Blocks.AZALEA_LEAVES)) return Items.AZALEA;
        if (state.isOf(Blocks.FLOWERING_AZALEA_LEAVES)) return Items.FLOWERING_AZALEA;

        if (state.isOf(Blocks.CRIMSON_NYLIUM)) return Items.CRIMSON_FUNGUS;
        if (state.isOf(Blocks.WARPED_NYLIUM)) return Items.WARPED_FUNGUS;

        return state.getBlock().asItem();
    }

    private static Item getProcessedForm(Item rawItem) {
        if (rawItem == Items.RAW_IRON) return Items.IRON_INGOT;
        if (rawItem == Items.RAW_COPPER) return Items.COPPER_INGOT;
        if (rawItem == Items.RAW_GOLD) return Items.GOLD_INGOT;

        if (rawItem == Items.ANCIENT_DEBRIS) return Items.NETHERITE_SCRAP;

        if (rawItem == Items.GOLD_NUGGET) return Items.GOLD_INGOT;

        if (rawItem == Items.BEEF) return Items.COOKED_BEEF;
        if (rawItem == Items.PORKCHOP) return Items.COOKED_PORKCHOP;
        if (rawItem == Items.CHICKEN) return Items.COOKED_CHICKEN;
        if (rawItem == Items.MUTTON) return Items.COOKED_MUTTON;
        if (rawItem == Items.RABBIT) return Items.COOKED_RABBIT;
        if (rawItem == Items.COD) return Items.COOKED_COD;
        if (rawItem == Items.SALMON) return Items.COOKED_SALMON;
        if (rawItem == Items.POTATO) return Items.BAKED_POTATO;
        if (rawItem == Items.KELP) return Items.DRIED_KELP;

        if (rawItem == Items.CACTUS) return Items.GREEN_DYE;

        if (rawItem == Items.SAND) return Items.GLASS;

        if (rawItem == Items.CLAY_BALL) return Items.BRICK;

        if (rawItem.toString().contains("_log")) return Items.CHARCOAL;

        return null;
    }

    public static Item getRandomAvailableItem(Random random, double borderSize) {
        List<Item> eligibleItems = new ArrayList<>();

        if (!scanned) {
            return Items.STONE;
        }

        for (Map.Entry<Item, ItemConfig.ItemUnlockData> entry : ItemConfig.getAllData().entrySet()) {
            ItemConfig.ItemUnlockData data = entry.getValue();

            if (borderSize < data.minBorderSize()) {
                continue;
            }

            if (data.requiresWorldCheck()) {
                Integer count = availableResources.get(data.item());
                if (count != null && count > 0) {
                    eligibleItems.add(data.item());
                }
            } else {
                eligibleItems.add(data.item());
            }
        }

        if (eligibleItems.isEmpty()) {
            return Items.STONE;
        }

        return eligibleItems.get(random.nextInt(eligibleItems.size()));
    }

    public static int getRequiredCount(Item item, int completionCount) {
        Config config = ConfigManager.get();
        ItemConfig.ItemUnlockData data = ItemConfig.getData(item);

        if (data == null) {
            return 16;
        }

        int availableInWorld = availableResources.getOrDefault(item, 0);

        double baseCount = data.baseCount() * data.multiplier();

        if (data.renewable()) {
            baseCount *= config.renewableMultiplier;
        } else {
            baseCount *= config.nonRenewableMultiplier;

            if (availableInWorld > 0) {
                double maxFromWorld = availableInWorld * config.nonRenewableMultiplier;
                baseCount = Math.min(baseCount, maxFromWorld);
            }
        }

        double progression = 1.0 + (completionCount * (config.progressionMultiplier - 1.0));
        double calculatedCount = baseCount * progression;

        if (data.renewable()) {
            double variation = config.randomnessVariation;
            double randomFactor = 1.0 + (Math.random() * variation * 2 - variation);
            calculatedCount *= randomFactor;
        }

        int finalCount = (int) Math.max(1, Math.round(calculatedCount));

        if (!data.renewable() && availableInWorld > 0) {
            int maxAllowed = (int) Math.ceil(availableInWorld * config.nonRenewableMultiplier);
            finalCount = Math.min(finalCount, maxAllowed);
        }

        if (isBuildingMaterial(item)) {
            finalCount = Math.min(finalCount, 512);
        } else if (isFood(item)) {
            finalCount = Math.min(finalCount, 256);
        } else if (isValuable(item)) {
            finalCount = Math.min(finalCount, 128);
        } else if (isRare(item)) {
            finalCount = Math.min(finalCount, 64);
        } else if (isUltraRare(item)) {
            finalCount = Math.min(finalCount, 8);
        }

        return Math.max(1, finalCount);
    }

    private static boolean isBuildingMaterial(Item item) {
        String id = Registry.ITEM.getId(item).toString();
        return id.contains("stone") || id.contains("brick") || id.contains("plank") ||
                id.contains("log") || id.contains("wood") || id.contains("sand") ||
                id.contains("dirt") || id.contains("clay") || id.contains("concrete") ||
                id.contains("terracotta") || id.contains("glass") || id.contains("wool");
    }

    private static boolean isFood(Item item) {
        return item.isFood() || item == Items.WHEAT || item == Items.POTATO ||
                item == Items.CARROT || item == Items.BEETROOT || item == Items.MELON_SLICE ||
                item == Items.PUMPKIN || item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES ||
                item == Items.CHORUS_FRUIT || item == Items.HONEY_BOTTLE;
    }

    private static boolean isValuable(Item item) {
        return item == Items.DIAMOND || item == Items.EMERALD ||
                item == Items.GOLD_INGOT || item == Items.RAW_GOLD ||
                item == Items.IRON_INGOT || item == Items.RAW_IRON ||
                item == Items.COPPER_INGOT || item == Items.RAW_COPPER ||
                item == Items.NETHERITE_SCRAP ||
                item == Items.ANCIENT_DEBRIS || item == Items.QUARTZ || item == Items.LAPIS_LAZULI ||
                item == Items.REDSTONE || item == Items.COAL || item == Items.OBSIDIAN ||
                item == Items.GLOWSTONE_DUST || item == Items.ENDER_PEARL || item == Items.BLAZE_ROD;
    }

    private static boolean isRare(Item item) {
        return item == Items.NETHERITE_INGOT || item == Items.GHAST_TEAR || item == Items.SHULKER_SHELL ||
                item == Items.HEART_OF_THE_SEA || item == Items.NAUTILUS_SHELL || item == Items.TOTEM_OF_UNDYING ||
                item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.DRAGON_BREATH || item == Items.AMETHYST_SHARD ||
                item == Items.ECHO_SHARD || item == Items.DISC_FRAGMENT_5;
    }

    private static boolean isUltraRare(Item item) {
        return item == Items.ELYTRA || item == Items.DRAGON_EGG || item == Items.NETHER_STAR ||
                item == Items.BEACON || item == Items.TRIDENT;
    }

    public static int getCountForItem(Item item) {
        return availableResources.getOrDefault(item, 0);
    }

    public static boolean isScanning() {
        return isScanning;
    }

    public static boolean isScanned() {
        return scanned;
    }

    public static int getProgress() {
        if (totalBlocks == 0) return 0;

        if (currentPhase == 1 || (!scanningChests && !scanningPlayers)) {
            return (int) ((scannedBlocks / (double) totalBlocks) * 95);
        }

        if (scanningChests || currentPhase == 2) {
            int chestProgress = chestPositions.isEmpty() ? 100 :
                    (int) ((currentChestIndex / (double) chestPositions.size()) * 100);
            return 95 + (chestProgress * 20 / 400);
        }

        int playerProgress = playersToScan.isEmpty() ? 100 :
                (int) ((currentPlayerIndex / (double) playersToScan.size()) * 100);
        return 99 + (playerProgress * 15 / 1000);

    }

    public static boolean isRerollItem(Item item) {
        return rerollItems.contains(item);
    }

    public static void savePersistentState() {
        if (scanned) {
            persistentResources.clear();
            persistentResources.putAll(availableResources);
            persistentScanned = true;
            persistentLastScannedSize = lastScannedSize;
            persistentLastScannedCenterX = lastScannedCenterX;
            persistentLastScannedCenterZ = lastScannedCenterZ;
        }
    }

    public static void restorePersistentState() {
        if (persistentScanned) {
            availableResources.clear();
            availableResources.putAll(persistentResources);
            scanned = true;
            lastScannedSize = persistentLastScannedSize;
            lastScannedCenterX = persistentLastScannedCenterX;
            lastScannedCenterZ = persistentLastScannedCenterZ;
        }
    }

}
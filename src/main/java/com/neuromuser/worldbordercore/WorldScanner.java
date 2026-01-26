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
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldScanner.class);
    private static final Map<Item, Integer> availableResources = new ConcurrentHashMap<>();
    private static final Set<Item> unobtainableItems = new HashSet<>();
    private static final Set<Item> rerollItems = new HashSet<>();

    private static boolean isScanning = false;
    private static boolean scanned = false;
    private static boolean netherScanned = false;
    private static boolean scanningNether = false;
    private static double lastScannedSize = 0;
    private static double lastScannedCenterX = 0;
    private static double lastScannedCenterZ = 0;

    private static int currentX, currentY, currentZ;
    private static int minX, maxX, minY, maxY, minZ, maxZ;
    private static int innerMinX, innerMaxX, innerMinZ, innerMaxZ;
    private static boolean hasInnerBounds = false;
    private static long totalBlocks = 0;
    private static long scannedBlocks = 0;
    private static final int BLOCKS_PER_TICK = 5000; // Increased for better performance
    private static final int CHESTS_PER_TICK = 100;  // Increased

    private static WorldBorder currentBorder;
    private static List<BlockPos> chestPositions = new ArrayList<>();
    private static int currentChestIndex = 0;
    private static boolean scanningChests = false;
    private static boolean scanningPlayers = false;
    private static List<ServerPlayerEntity> playersToScan = new ArrayList<>();
    private static int currentPlayerIndex = 0;

    // Progress tracking for accurate percentages
    private static int totalPhases = 4; // Block scan, chest scan, player scan, finalize
    private static int currentPhase = 0;

    public static void initialize() {
        setupUnobtainableItems();
        setupRerollItems();
        ItemConfig.load();
        LOGGER.info("WorldScanner initialized with " + ItemConfig.getAllData().size() + " configured items");
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

        if (isIncrementalScan) {
            LOGGER.info("Starting INCREMENTAL scan (border expanded from {} to {})", lastScannedSize, size);
            LOGGER.info("Keeping existing {} item counts, will only scan new area", availableResources.size());
        } else {
            LOGGER.info("Starting FULL scan...");
            // Only clear on full scans
            availableResources.clear();
        }

        currentBorder = border;
        scanningNether = false;
        currentPhase = 0;

        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();

        if (size > 59999900) {
            LOGGER.warn("World border too large to scan: " + size);
            return;
        }

        double radius = size / 2.0;
        minX = (int) Math.floor(centerX - radius);
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

        long rangeX = (long) (maxX - minX);
        long rangeY = (long) (maxY - minY);
        long rangeZ = (long) (maxZ - minZ);
        totalBlocks = rangeX * rangeY * rangeZ;

        if (hasInnerBounds) {
            long innerRangeX = (long) (innerMaxX - innerMinX);
            long innerRangeY = rangeY;
            long innerRangeZ = (long) (innerMaxZ - innerMinZ);
            long skippedBlocks = innerRangeX * innerRangeY * innerRangeZ;
            totalBlocks -= skippedBlocks;

            if (isIncrementalScan) {
                LOGGER.info("Skipping previously scanned area ({} blocks), only scanning {} new blocks",
                        skippedBlocks, totalBlocks);
            }
        }

        isScanning = true;
        scanned = false;

        lastScannedSize = size;
        lastScannedCenterX = centerX;
        lastScannedCenterZ = centerZ;

        LOGGER.info("Scan started: {} blocks to scan", totalBlocks);
    }

    public static void scanNether(ServerWorld netherWorld) {
        if (netherScanned) {
            LOGGER.info("Nether already scanned");
            return;
        }

        LOGGER.info("Starting Nether scan...");
        WorldBorder overworldBorder = netherWorld.getServer().getWorld(World.OVERWORLD).getWorldBorder();

        double overworldSize = overworldBorder.getSize();
        double netherSize = overworldSize / 8.0;
        double netherCenterX = overworldBorder.getCenterX() / 8.0;
        double netherCenterZ = overworldBorder.getCenterZ() / 8.0;

        double radius = netherSize / 2.0;
        minX = (int) Math.floor(netherCenterX - radius);
        maxX = (int) Math.ceil(netherCenterX + radius);
        minZ = (int) Math.floor(netherCenterZ - radius);
        maxZ = (int) Math.ceil(netherCenterZ + radius);
        minY = netherWorld.getBottomY();
        maxY = netherWorld.getTopY();

        hasInnerBounds = false;
        currentX = minX;
        currentY = minY;
        currentZ = minZ;
        scannedBlocks = 0;
        currentPhase = 0;

        chestPositions.clear();
        currentChestIndex = 0;
        scanningChests = false;
        scanningPlayers = false;
        playersToScan.clear();
        currentPlayerIndex = 0;

        long rangeX = (long) (maxX - minX);
        long rangeY = (long) (maxY - minY);
        long rangeZ = (long) (maxZ - minZ);
        totalBlocks = rangeX * rangeY * rangeZ;

        isScanning = true;
        scanningNether = true;
        currentBorder = null;

        LOGGER.info("Nether scan started: {} total blocks to scan", totalBlocks);
    }

    public static void tick(ServerWorld world) {
        if (!isScanning) return;

        if (scanningPlayers) {
            currentPhase = 3;
            scanPlayerInventories(world);
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

                    // Only process if chunk is loaded
                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        pos.set(currentX, currentY, currentZ);

                        try {
                            Chunk chunk = world.getChunk(chunkX, chunkZ);
                            BlockState state = chunk.getBlockState(pos);

                            Item item = getItemFromBlock(state);
                            if (item != null && !unobtainableItems.contains(item)) {
                                availableResources.merge(item, 1, Integer::sum);

                                // Also add smelted/processed forms for ores
                                Item processed = getProcessedForm(item);
                                if (processed != null && processed != item) {
                                    availableResources.merge(processed, 1, Integer::sum);
                                }
                            }

                            // Check for containers
                            if (isContainerBlock(state)) {
                                chestPositions.add(pos.toImmutable());
                            }
                        } catch (Exception e) {
                            // Skip problematic blocks
                        }
                    }
                }

                blocksThisTick++;
                scannedBlocks++;
            }

            // Advance position
            currentY++;
            if (currentY >= maxY) {
                currentY = minY;
                currentZ++;
                if (currentZ >= maxZ) {
                    currentZ = minZ;
                    currentX++;
                    if (currentX >= maxX) {
                        LOGGER.info("Block scanning complete. Found {} unique items, {} containers",
                                availableResources.size(), chestPositions.size());
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
        LOGGER.info("Starting chest scanning phase: {} chests to scan", chestPositions.size());
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
                } catch (Exception e) {
                    // Skip problematic chests
                    LOGGER.debug("Failed to scan chest at {}: {}", pos, e.getMessage());
                }
            }

            currentChestIndex++;
            chestsThisTick++;
        }

        if (currentChestIndex >= chestPositions.size()) {
            scanningChests = false;
            LOGGER.info("Chest scanning complete. Total items in containers added to inventory");
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

        LOGGER.info("Starting player inventory scanning: {} players", playersToScan.size());
    }

    private static void scanPlayerInventories(ServerWorld world) {
        if (currentPlayerIndex >= playersToScan.size()) {
            finishScan();
            return;
        }

        ServerPlayerEntity player = playersToScan.get(currentPlayerIndex);

        try {
            // Scan main inventory
            PlayerInventory inventory = player.getInventory();
            scanInventory(inventory);

            // Scan ender chest
            Inventory enderChest = player.getEnderChestInventory();
            if (enderChest != null) {
                scanInventory(enderChest);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to scan player {}: {}", player.getName().getString(), e.getMessage());
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
            } catch (Exception e) {
                // Skip problematic slots
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
            netherScanned = true;
            scanningNether = false;
            LOGGER.info("Nether scan complete!");
        }

        LOGGER.info("World scan complete! Found {} unique items across all sources", availableResources.size());

        // Log top 20 most common items for debugging
        LOGGER.info("Top items found in scan:");
        availableResources.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(20)
                .forEach(entry -> {
                    String itemId = Registries.ITEM.getId(entry.getKey()).toString();
                    LOGGER.info("  - {}: {}", itemId, entry.getValue());
                });
    }

    private static Item getItemFromBlock(BlockState state) {
        // Ore conversions - more comprehensive
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

        // Special block conversions
        if (state.isOf(Blocks.GLOWSTONE)) return Items.GLOWSTONE_DUST;
        if (state.isOf(Blocks.SEA_LANTERN)) return Items.PRISMARINE_CRYSTALS;
        if (state.isOf(Blocks.SPONGE)) return Items.SPONGE;
        if (state.isOf(Blocks.WET_SPONGE)) return Items.WET_SPONGE;
        if (state.isOf(Blocks.MELON)) return Items.MELON_SLICE;
        if (state.isOf(Blocks.PUMPKIN) || state.isOf(Blocks.CARVED_PUMPKIN)) return Items.PUMPKIN;

        // Storage blocks to items
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

        // Crop blocks
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
        if (state.isOf(Blocks.PINK_PETALS)) return Items.PINK_PETALS;
        if (state.isOf(Blocks.TORCHFLOWER) || state.isOf(Blocks.TORCHFLOWER_CROP)) return Items.TORCHFLOWER;
        if (state.isOf(Blocks.PITCHER_CROP) || state.isOf(Blocks.PITCHER_PLANT)) return Items.PITCHER_PLANT;

        // Tree leaves (converted to saplings for tracking renewability)
        if (state.isOf(Blocks.OAK_LEAVES)) return Items.OAK_SAPLING;
        if (state.isOf(Blocks.SPRUCE_LEAVES)) return Items.SPRUCE_SAPLING;
        if (state.isOf(Blocks.BIRCH_LEAVES)) return Items.BIRCH_SAPLING;
        if (state.isOf(Blocks.JUNGLE_LEAVES)) return Items.JUNGLE_SAPLING;
        if (state.isOf(Blocks.ACACIA_LEAVES)) return Items.ACACIA_SAPLING;
        if (state.isOf(Blocks.DARK_OAK_LEAVES)) return Items.DARK_OAK_SAPLING;
        if (state.isOf(Blocks.MANGROVE_LEAVES)) return Items.MANGROVE_PROPAGULE;
        if (state.isOf(Blocks.CHERRY_LEAVES)) return Items.CHERRY_SAPLING;
        if (state.isOf(Blocks.AZALEA_LEAVES)) return Items.AZALEA;
        if (state.isOf(Blocks.FLOWERING_AZALEA_LEAVES)) return Items.FLOWERING_AZALEA;

        // Nylium to fungus
        if (state.isOf(Blocks.CRIMSON_NYLIUM)) return Items.CRIMSON_FUNGUS;
        if (state.isOf(Blocks.WARPED_NYLIUM)) return Items.WARPED_FUNGUS;

        // Default: return block's item form
        return state.getBlock().asItem();
    }

    /**
     * Returns the processed/smelted form of raw materials.
     * This ensures that when raw ores are found, both the raw and processed forms are counted.
     */
    private static Item getProcessedForm(Item rawItem) {
        // Raw ores to ingots
        if (rawItem == Items.RAW_IRON) return Items.IRON_INGOT;
        if (rawItem == Items.RAW_COPPER) return Items.COPPER_INGOT;
        if (rawItem == Items.RAW_GOLD) return Items.GOLD_INGOT;

        // Ancient debris to netherite scrap
        if (rawItem == Items.ANCIENT_DEBRIS) return Items.NETHERITE_SCRAP;

        // Nether gold to gold ingot
        if (rawItem == Items.GOLD_NUGGET) return Items.GOLD_INGOT;

        // Food items (raw to cooked)
        if (rawItem == Items.BEEF) return Items.COOKED_BEEF;
        if (rawItem == Items.PORKCHOP) return Items.COOKED_PORKCHOP;
        if (rawItem == Items.CHICKEN) return Items.COOKED_CHICKEN;
        if (rawItem == Items.MUTTON) return Items.COOKED_MUTTON;
        if (rawItem == Items.RABBIT) return Items.COOKED_RABBIT;
        if (rawItem == Items.COD) return Items.COOKED_COD;
        if (rawItem == Items.SALMON) return Items.COOKED_SALMON;
        if (rawItem == Items.POTATO) return Items.BAKED_POTATO;
        if (rawItem == Items.KELP) return Items.DRIED_KELP;

        // Cactus to green dye
        if (rawItem == Items.CACTUS) return Items.GREEN_DYE;

        // Sand to glass
        if (rawItem == Items.SAND) return Items.GLASS;

        // Clay to brick
        if (rawItem == Items.CLAY_BALL) return Items.BRICK;

        // Logs to charcoal (any log can become charcoal)
        if (rawItem.toString().contains("_log")) return Items.CHARCOAL;

        return null;
    }

    public static Item getRandomAvailableItem(Random random, double borderSize) {
        List<Item> eligibleItems = new ArrayList<>();

        if (!scanned) {
            LOGGER.warn("Attempting to get random item before scan is complete!");
            return Items.STONE;
        }

        for (Map.Entry<Item, ItemConfig.ItemUnlockData> entry : ItemConfig.getAllData().entrySet()) {
            ItemConfig.ItemUnlockData data = entry.getValue();

            // Check if item is unlocked for current border size
            if (borderSize < data.minBorderSize) {
                continue;
            }

            // Check if item requires world presence
            if (data.requiresWorldCheck) {
                Integer count = availableResources.get(data.item);
                if (count != null && count > 0) {
                    eligibleItems.add(data.item);
                    LOGGER.debug("Item {} is eligible (requiresWorldCheck=true, found {} in world)",
                            Registries.ITEM.getId(data.item), count);
                } else {
                    LOGGER.debug("Item {} skipped (requiresWorldCheck=true, but not found in world)",
                            Registries.ITEM.getId(data.item));
                }
            } else {
                // Items that don't require world check are always eligible if unlocked
                eligibleItems.add(data.item);
                LOGGER.debug("Item {} is eligible (requiresWorldCheck=false)",
                        Registries.ITEM.getId(data.item));
            }
        }

        if (eligibleItems.isEmpty()) {
            LOGGER.warn("No eligible items for border size {}! Falling back to stone", borderSize);
            return Items.STONE;
        }

        Item selected = eligibleItems.get(random.nextInt(eligibleItems.size()));
        LOGGER.info("Selected item: {} from {} eligible items (border size: {})",
                Registries.ITEM.getId(selected), eligibleItems.size(), borderSize);

        // Extra validation for requiresWorldCheck items
        ItemConfig.ItemUnlockData selectedData = ItemConfig.getData(selected);
        if (selectedData != null && selectedData.requiresWorldCheck) {
            Integer count = availableResources.get(selected);
            LOGGER.info("  -> This item requires world check. Count in world: {}", count);
        }

        return selected;
    }

    public static int getRequiredCount(Item item, int completionCount) {
        Config config = ConfigManager.get();
        ItemConfig.ItemUnlockData data = ItemConfig.getData(item);

        if (data == null) {
            LOGGER.warn("No config data for item: {}", Registries.ITEM.getId(item));
            return 16;
        }

        String itemId = Registries.ITEM.getId(item).toString();
        LOGGER.info("=== Calculating requirement for: {} ===", itemId);

        // Get actual count in world (blocks + chests + inventories)
        int availableInWorld = availableResources.getOrDefault(item, 0);
        LOGGER.info("  Available in world: {}", availableInWorld);
        LOGGER.info("  Config - baseCount: {}, multiplier: {}, renewable: {}",
                data.baseCount, data.multiplier, data.renewable);

        // Calculate base count
        double baseCount = data.baseCount * data.multiplier;
        LOGGER.info("  Base after item multiplier: {}", baseCount);

        // Apply renewable/non-renewable multiplier from config
        if (data.renewable) {
            baseCount *= config.renewableMultiplier;
            LOGGER.info("  Renewable multiplier ({}) applied: {}", config.renewableMultiplier, baseCount);
        } else {
            LOGGER.info("  Applying non-renewable multiplier: {}", config.nonRenewableMultiplier);
            baseCount *= config.nonRenewableMultiplier;
            LOGGER.info("  After non-renewable multiplier: {}", baseCount);

            // For non-renewable, cap at configured percentage of available
            if (availableInWorld > 0) {
                double maxFromWorld = availableInWorld * config.nonRenewableMultiplier;
                LOGGER.info("  Max from world ({}*{}): {}", availableInWorld, config.nonRenewableMultiplier, maxFromWorld);
                baseCount = Math.min(baseCount, maxFromWorld);
                LOGGER.info("  After capping to world availability: {}", baseCount);
            } else {
                LOGGER.warn("  WARNING: Non-renewable item with 0 available in world!");
            }
        }

        // Apply progression
        double progression = 1.0 + (completionCount * (config.progressionMultiplier - 1.0));
        double calculatedCount = baseCount * progression;
        LOGGER.info("  Progression multiplier (completions: {}): {}", completionCount, progression);
        LOGGER.info("  After progression: {}", calculatedCount);

        // Apply randomness only for renewable items
        if (data.renewable) {
            double variation = config.randomnessVariation;
            double randomFactor = 1.0 + (Math.random() * variation * 2 - variation);
            calculatedCount *= randomFactor;
            LOGGER.info("  After randomness ({} variation): {}", variation, calculatedCount);
        }

        int finalCount = (int) Math.max(1, Math.round(calculatedCount));
        LOGGER.info("  Rounded count: {}", finalCount);

        // Hard caps for non-renewable items
        if (!data.renewable && availableInWorld > 0) {
            int maxAllowed = (int) Math.ceil(availableInWorld * config.nonRenewableMultiplier);
            LOGGER.info("  Non-renewable hard cap: {}", maxAllowed);
            finalCount = Math.min(finalCount, maxAllowed);
            LOGGER.info("  After hard cap: {}", finalCount);
        }

        // General caps based on item type
        int beforeTypeCap = finalCount;
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

        if (finalCount != beforeTypeCap) {
            LOGGER.info("  Item type cap applied: {} -> {}", beforeTypeCap, finalCount);
        }

        LOGGER.info("  FINAL REQUIREMENT: {}", finalCount);
        LOGGER.info("===========================================");

        return Math.max(1, finalCount);
    }

    private static boolean isBuildingMaterial(Item item) {
        String id = Registries.ITEM.getId(item).toString();
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

    /**
     * Subtracts collected items from the available resources count.
     * This is called when the core collects items to keep counts accurate.
     */
    public static void subtractCollectedItem(Item item, int amount) {
        if (amount <= 0) return;

        int current = availableResources.getOrDefault(item, 0);
        int newAmount = Math.max(0, current - amount);

        if (newAmount > 0) {
            availableResources.put(item, newAmount);
        } else {
            availableResources.remove(item);
        }

        LOGGER.debug("Subtracted {} x{} from world count (was: {}, now: {})",
                Registries.ITEM.getId(item), amount, current, newAmount);
    }

    public static boolean isScanning() {
        return isScanning;
    }

    public static boolean isScanned() {
        return scanned;
    }

    public static boolean isScanningNether() {
        return scanningNether;
    }

    public static int getProgress() {
        if (totalBlocks == 0) return 0;

        // Phase 1: Block scanning (0-60%)
        if (currentPhase == 1 || (!scanningChests && !scanningPlayers)) {
            return (int) ((scannedBlocks / (double) totalBlocks) * 60);
        }

        // Phase 2: Chest scanning (60-80%)
        if (scanningChests || currentPhase == 2) {
            int chestProgress = chestPositions.isEmpty() ? 100 :
                    (int) ((currentChestIndex / (double) chestPositions.size()) * 100);
            return 60 + (chestProgress * 20 / 100);
        }

        // Phase 3: Player scanning (80-95%)
        if (scanningPlayers || currentPhase == 3) {
            int playerProgress = playersToScan.isEmpty() ? 100 :
                    (int) ((currentPlayerIndex / (double) playersToScan.size()) * 100);
            return 80 + (playerProgress * 15 / 100);
        }

        // Phase 4: Complete (95-100%)
        return currentPhase >= 4 ? 100 : 95;
    }

    public static boolean isRerollItem(Item item) {
        return rerollItems.contains(item);
    }

    public static boolean isNetherScanned() {
        return netherScanned;
    }

    public static void reset() {
        availableResources.clear();
        scanned = false;
        isScanning = false;
        netherScanned = false;
        scanningNether = false;
        scanningChests = false;
        scanningPlayers = false;
        lastScannedSize = 0;
        currentBorder = null;
        chestPositions.clear();
        currentChestIndex = 0;
        playersToScan.clear();
        currentPlayerIndex = 0;
        currentPhase = 0;
        LOGGER.info("WorldScanner reset");
    }
}
package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.config.Config;
import com.neuromuser.worldbordercore.config.ConfigManager;
import com.neuromuser.worldbordercore.items.RolledItem;
import com.neuromuser.worldbordercore.items.RolledItemRegistry;
import com.neuromuser.worldbordercore.items.WorldRollContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WorldScanner {
    public static final Map<Item, Integer> availableResources = new ConcurrentHashMap<>();
    private static final Set<Item> unobtainableItems = new HashSet<>();
    private static final Set<Item> rerollItems = new HashSet<>();
    private static final Set<RegistryKey<Biome>> scannedBiomes = new HashSet<>();
    private static final Set<RegistryKey<Biome>> persistentScannedBiomes = new HashSet<>();
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
    private static ServerWorld currentWorld;

    private static long overworldTotalBlocks = 0;
    private static long overworldScannedBlocks = 0;
    private static long netherTotalBlocks = 0;
    private static long netherScannedBlocks = 0;

    public static void initialize() {
        setupUnobtainableItems();
        setupRerollItems();
        RolledItemRegistry.registerAll();
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
        currentWorld = world;
        RecipeHelper.initialize(world);

        WorldBorder border = world.getWorldBorder();
        double size = border.getSize();
        boolean isIncrementalScan = (lastScannedSize > 0 && lastScannedSize < size);
        savePersistentState();
        if (!isIncrementalScan) {
            availableResources.clear();
        }
        scannedBiomes.clear();
        currentBorder = border;

        currentPhase = 0;
        scanningNether = false;
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
        overworldScannedBlocks = 0;
        netherScannedBlocks = 0;

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

        overworldTotalBlocks = totalBlocks;

        ServerWorld nether = world.getServer().getWorld(World.NETHER);
        if (nether != null) {
            WorldBorder netherBorder = nether.getWorldBorder();
            double netherRadius = netherBorder.getSize() / 2.0;
            int netherMinX = (int) Math.floor(netherBorder.getCenterX() - netherRadius);
            int netherMaxX = (int) Math.ceil(netherBorder.getCenterX() + netherRadius);
            int netherMinZ = (int) Math.floor(netherBorder.getCenterZ() - netherRadius);
            int netherMaxZ = (int) Math.ceil(netherBorder.getCenterZ() + netherRadius);
            int netherMinY = nether.getBottomY();
            int netherMaxY = nether.getTopY();

            long netherRangeX = netherMaxX - netherMinX;
            long netherRangeY = netherMaxY - netherMinY;
            long netherRangeZ = netherMaxZ - netherMinZ;
            netherTotalBlocks = netherRangeX * netherRangeY * netherRangeZ;

            totalBlocks += netherTotalBlocks;
        } else {
            netherTotalBlocks = 0;
        }

        isScanning = true;
        scanned = false;

        lastScannedSize = size;
        lastScannedCenterX = centerX;
        lastScannedCenterZ = centerZ;
    }

    public static void tick(ServerWorld world) {
        currentWorld = world;
        if (!isScanning) return;

        if (scanningPlayers) {
            currentPhase = 4;
            scanPlayerInventories();
            return;
        }

        if (scanningChests) {
            currentPhase = 3;
            scanChests(world);
            return;
        }

        if (scanningNether) {
            currentPhase = 2;
            scanNetherBlocks();
            return;
        }

        currentPhase = 1;
        int blocksThisTick = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (blocksThisTick < BLOCKS_PER_TICK && isScanning && !scanningChests && !scanningNether) {
            if (shouldScanBlock(currentX, currentZ)) {
                boolean shouldCount = currentBorder != null && currentBorder.contains(currentX, currentZ);

                if (shouldCount) {
                    int chunkX = currentX >> 4;
                    int chunkZ = currentZ >> 4;

                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        pos.set(currentX, currentY, currentZ);

                        try {
                            Chunk chunk = world.getChunk(chunkX, chunkZ);
                            BlockState state = chunk.getBlockState(pos);

                            try {
                                world.getBiome(pos).getKey().ifPresent(scannedBiomes::add);
                            } catch (Exception ignored) {}

                            Item item = RecipeHelper.getItemFromBlock(state);
                            if (item != null && !unobtainableItems.contains(item)) {
                                availableResources.merge(item, 1, Integer::sum);

                                Set<Item> processedForms = RecipeHelper.getProcessedForms(item);
                                for (Item processed : processedForms) {
                                    if (processed != item) {
                                        availableResources.merge(processed, 1, Integer::sum);
                                    }
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
                overworldScannedBlocks++;
            }

            currentY++;
            if (currentY >= maxY) {
                currentY = minY;
                currentZ++;
                if (currentZ >= maxZ) {
                    currentZ = minZ;
                    currentX++;
                    if (currentX >= maxX) {
                        WorldborderCore.LOGGER.info("Overworld scan complete. Scanned {} blocks", overworldScannedBlocks);
                        startNetherScanning();
                        break;
                    }
                }
            }
        }
    }

    private static void startNetherScanning() {
        ServerWorld nether = currentWorld.getServer().getWorld(World.NETHER);
        if (nether == null) {
            WorldborderCore.LOGGER.warn("Nether dimension not available, skipping Nether scan");
            startChestScanning();
            return;
        }

        scanningNether = true;
        currentPhase = 2;

        WorldBorder netherBorder = nether.getWorldBorder();
        double netherRadius = netherBorder.getSize() / 2.0;

        int minX = (int) Math.floor(netherBorder.getCenterX() - netherRadius);
        maxX = (int) Math.ceil(netherBorder.getCenterX() + netherRadius);
        minZ = (int) Math.floor(netherBorder.getCenterZ() - netherRadius);
        maxZ = (int) Math.ceil(netherBorder.getCenterZ() + netherRadius);
        minY = nether.getBottomY();
        maxY = nether.getTopY();

        currentX = minX;
        currentY = minY;
        currentZ = minZ;

        hasInnerBounds = false;

        WorldborderCore.LOGGER.info("Starting Nether scan: minX={}, maxX={}, minY={}, maxY={}, minZ={}, maxZ={}",
                minX, maxX, minY, maxY, minZ, maxZ);
    }

    private static void scanNetherBlocks() {
        ServerWorld nether = currentWorld.getServer().getWorld(World.NETHER);
        if (nether == null) {
            WorldborderCore.LOGGER.warn("Nether became unavailable during scan");
            scanningNether = false;
            startChestScanning();
            return;
        }

        int blocksThisTick = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (blocksThisTick < BLOCKS_PER_TICK && scanningNether) {
            int chunkX = currentX >> 4;
            int chunkZ = currentZ >> 4;

            boolean isChunkLoaded = nether.isChunkLoaded(chunkX, chunkZ);

            if (!isChunkLoaded) {
                try {
                    nether.getChunk(chunkX, chunkZ);
                    isChunkLoaded = true;
                } catch (Exception e) {
                    WorldborderCore.LOGGER.debug("Failed to load Nether chunk at {}, {}", chunkX, chunkZ);
                }
            }

            if (isChunkLoaded) {
                pos.set(currentX, currentY, currentZ);

                try {
                    Chunk chunk = nether.getChunk(chunkX, chunkZ);
                    BlockState state = chunk.getBlockState(pos);

                    try {
                        nether.getBiome(pos).getKey().ifPresent(scannedBiomes::add);
                    } catch (Exception ignored) {}

                    Item item = RecipeHelper.getItemFromBlock(state);
                    if (item != null && !unobtainableItems.contains(item)) {
                        availableResources.merge(item, 1, Integer::sum);

                        Set<Item> processedForms = RecipeHelper.getProcessedForms(item);
                        for (Item processed : processedForms) {
                            if (processed != item) {
                                availableResources.merge(processed, 1, Integer::sum);
                            }
                        }
                    }

                    if (isContainerBlock(state)) {
                        chestPositions.add(pos.toImmutable());
                    }
                } catch (Exception e) {
                    WorldborderCore.LOGGER.debug("Error scanning Nether block at {}: {}", pos, e.getMessage());
                }
            }

            blocksThisTick++;
            scannedBlocks++;
            netherScannedBlocks++;

            currentY++;
            if (currentY >= maxY) {
                currentY = minY;
                currentZ++;
                if (currentZ >= maxZ) {
                    currentZ = minZ;
                    currentX++;
                    if (currentX >= maxX) {
                        scanningNether = false;
                        WorldborderCore.LOGGER.info("Nether scan complete. Scanned {} blocks, found {} Nether biomes",
                                netherScannedBlocks, scannedBiomes.size());
                        startChestScanning();
                        break;
                    }
                }
            }
        }
    }

    private static void startChestScanning() {
        scanningChests = true;
        currentChestIndex = 0;
        currentPhase = 3;
    }

    private static void scanChests(ServerWorld world) {
        int chestsThisTick = 0;

        while (currentChestIndex < chestPositions.size() && chestsThisTick < CHESTS_PER_TICK) {
            BlockPos pos = chestPositions.get(currentChestIndex);

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            ServerWorld targetWorld = world;
            if (world.getRegistryKey() == World.OVERWORLD) {
                ServerWorld nether = world.getServer().getWorld(World.NETHER);
                if (nether != null && nether.isChunkLoaded(chunkX, chunkZ)) {
                    BlockEntity netherBE = nether.getBlockEntity(pos);
                    if (netherBE != null) {
                        targetWorld = nether;
                    }
                }
            }

            if (targetWorld.isChunkLoaded(chunkX, chunkZ)) {
                try {
                    BlockEntity blockEntity = targetWorld.getBlockEntity(pos);
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

    public static Set<RegistryKey<Biome>> getScannedBiomes() {
        return new HashSet<>(scannedBiomes);
    }

    private static void startPlayerScanning(ServerWorld world) {
        scanningPlayers = true;
        currentPlayerIndex = 0;
        currentPhase = 4;

        playersToScan.clear();
        playersToScan.addAll(world.getServer().getPlayerManager().getPlayerList());
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
        scanningNether = false;
        currentBorder = null;
        currentPhase = 5;

        savePersistentState();
    }

    public static RolledItem getRandomAvailableRolledItem( net.minecraft.util.math.random.Random random, double borderSize,
                                                           int completionCount, ServerWorld world) {
        WorldRollContext context = new WorldRollContext(
                world,
                borderSize,
                completionCount,
                new HashMap<>(availableResources),
                getScannedBiomes(),
                ConfigManager.get(),
                random
        );

        List<RolledItem> eligible = RolledItemRegistry.getItemsForContext(context);

        if (eligible.isEmpty()) {
            RolledItem stone = RolledItemRegistry.get(Items.STONE);
            if (stone != null) return stone;
        }

        List<RolledItem> weightedList = new ArrayList<>();
        for (RolledItem item : eligible) {
            int weight = Math.max(1, (int)(100.0 / item.getRarity()));
            for (int i = 0; i < weight; i++) {
                weightedList.add(item);
            }
        }

        return weightedList.get(random.nextInt(weightedList.size()));
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

        if (currentPhase == 1) {
            if (overworldTotalBlocks == 0) return 0;
            return (int) ((overworldScannedBlocks / (double) overworldTotalBlocks) * 40);
        }

        if (currentPhase == 2 || scanningNether) {
            if (netherTotalBlocks == 0) return 40;
            int netherProgress = (int) ((netherScannedBlocks / (double) netherTotalBlocks) * 40);
            return 40 + netherProgress;
        }

        if (currentPhase == 3 || scanningChests) {
            int chestProgress = chestPositions.isEmpty() ? 100 :
                    (int) ((currentChestIndex / (double) chestPositions.size()) * 100);
            return 80 + (chestProgress * 15 / 100);
        }

        if (currentPhase == 4 || scanningPlayers) {
            int playerProgress = playersToScan.isEmpty() ? 100 :
                    (int) ((currentPlayerIndex / (double) playersToScan.size()) * 100);
            return 95 + (playerProgress * 5 / 100);
        }

        return 100;
    }

    public static boolean isRerollItem(Item item) {
        return rerollItems.contains(item);
    }

    public static String getScanDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Scanning: ").append(isScanning).append("\n");
        info.append("Scanned: ").append(scanned).append("\n");
        info.append("Phase: ").append(currentPhase).append("\n");
        info.append("Scanning Nether: ").append(scanningNether).append("\n");
        info.append("Scanning Chests: ").append(scanningChests).append("\n");
        info.append("Scanning Players: ").append(scanningPlayers).append("\n");
        info.append("Overworld blocks: ").append(overworldScannedBlocks).append("/").append(overworldTotalBlocks).append("\n");
        info.append("Nether blocks: ").append(netherScannedBlocks).append("/").append(netherTotalBlocks).append("\n");
        info.append("Total resources found: ").append(availableResources.size()).append("\n");
        info.append("Total biomes found: ").append(scannedBiomes.size()).append("\n");
        return info.toString();
    }

    public static int getCurrentPhase() {
        return currentPhase;
    }

    public static boolean isScanningNether() {
        return scanningNether;
    }

    public static void savePersistentState() {
        if (scanned) {
            persistentResources.clear();
            persistentResources.putAll(availableResources);
            persistentScannedBiomes.clear();
            persistentScannedBiomes.addAll(scannedBiomes);
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
            scannedBiomes.clear();
            scannedBiomes.addAll(persistentScannedBiomes);
            scanned = true;
            lastScannedSize = persistentLastScannedSize;
            lastScannedCenterX = persistentLastScannedCenterX;
            lastScannedCenterZ = persistentLastScannedCenterZ;
        }
    }

    public static void reset() {
        isScanning = false;
        scanned = false;
        scanningNether = false;
        lastScannedSize = 0;
        lastScannedCenterX = 0;
        lastScannedCenterZ = 0;
        currentBorder = null;
        availableResources.clear();
        scannedBiomes.clear();

        currentX = 0;
        currentY = 0;
        currentZ = 0;
        maxX = 0;
        minY = 0;
        maxY = 0;
        minZ = 0;
        maxZ = 0;
        innerMinX = 0;
        innerMaxX = 0;
        innerMinZ = 0;
        innerMaxZ = 0;
        hasInnerBounds = false;
        totalBlocks = 0;
        scannedBlocks = 0;
        overworldTotalBlocks = 0;
        overworldScannedBlocks = 0;
        netherTotalBlocks = 0;
        netherScannedBlocks = 0;

        chestPositions.clear();
        currentChestIndex = 0;
        scanningChests = false;
        scanningPlayers = false;
        playersToScan.clear();
        currentPlayerIndex = 0;

        currentPhase = 0;
        currentWorld = null;

        persistentResources.clear();
        persistentScannedBiomes.clear();
        persistentScanned = false;
        persistentLastScannedSize = 0;
        persistentLastScannedCenterX = 0;
        persistentLastScannedCenterZ = 0;

        RecipeHelper.clearCaches();

        WorldborderCore.LOGGER.info("WorldScanner reset complete");
    }
}
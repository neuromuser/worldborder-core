package com.neuromuser.worldbordercore;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.border.WorldBorder;

import java.util.*;

public class WorldScanner {

    private static final Map<Item, Integer> availableResources = new HashMap<>();
    private static final Set<Item> renewableItems = new HashSet<>();
    private static final Set<Item> rerollItems = new HashSet<>();

    private static boolean isScanning = false;
    private static boolean scanned = false;
    private static double lastScannedSize = 0;
    private static double lastScannedCenterX = 0;
    private static double lastScannedCenterZ = 0;
    private static double progressionMultiplier = 1.0;

    private static int currentX, currentY, currentZ;
    private static int minX, maxX, minY, maxY, minZ, maxZ;
    private static int innerMinX, innerMaxX, innerMinZ, innerMaxZ;
    private static boolean hasInnerBounds = false;
    private static int totalBlocks = 0;
    private static int scannedBlocks = 0;
    private static final int BLOCKS_PER_TICK = 2000;

    private static WorldBorder currentBorder;

    public static void initialize() {
        setupRenewables();
        setupRerollItems();
    }

    private static void setupRenewables() {
        renewableItems.add(Items.WHEAT);
        renewableItems.add(Items.WHEAT_SEEDS);
        renewableItems.add(Items.POTATO);
        renewableItems.add(Items.CARROT);
        renewableItems.add(Items.BEETROOT);
        renewableItems.add(Items.BEETROOT_SEEDS);
        renewableItems.add(Items.PUMPKIN);
        renewableItems.add(Items.MELON);
        renewableItems.add(Items.SUGAR_CANE);
        renewableItems.add(Items.BAMBOO);
        renewableItems.add(Items.CACTUS);
        renewableItems.add(Items.KELP);
        renewableItems.add(Items.SWEET_BERRIES);
        renewableItems.add(Items.GLOW_BERRIES);
        renewableItems.add(Items.COCOA_BEANS);
        renewableItems.add(Items.NETHER_WART);
        renewableItems.add(Items.OAK_LOG);
        renewableItems.add(Items.SPRUCE_LOG);
        renewableItems.add(Items.BIRCH_LOG);
        renewableItems.add(Items.JUNGLE_LOG);
        renewableItems.add(Items.ACACIA_LOG);
        renewableItems.add(Items.DARK_OAK_LOG);
        renewableItems.add(Items.MANGROVE_LOG);
        renewableItems.add(Items.CHERRY_LOG);
        renewableItems.add(Items.ROTTEN_FLESH);
        renewableItems.add(Items.BONE);
        renewableItems.add(Items.STRING);
        renewableItems.add(Items.SPIDER_EYE);
        renewableItems.add(Items.GUNPOWDER);
        renewableItems.add(Items.ENDER_PEARL);
        renewableItems.add(Items.SLIME_BALL);
        renewableItems.add(Items.LEATHER);
        renewableItems.add(Items.FEATHER);
        renewableItems.add(Items.EGG);
        renewableItems.add(Items.PHANTOM_MEMBRANE);
        renewableItems.add(Items.BLAZE_ROD);
        renewableItems.add(Items.MAGMA_CREAM);
        renewableItems.add(Items.GHAST_TEAR);
        renewableItems.add(Items.COBBLESTONE);
        renewableItems.add(Items.STONE);
        renewableItems.add(Items.OBSIDIAN);
        renewableItems.add(Items.BASALT);
    }

    private static void setupRerollItems() {
        rerollItems.add(Items.NETHER_STAR);
        rerollItems.add(Items.TOTEM_OF_UNDYING);
        rerollItems.add(Items.DRAGON_EGG);
        rerollItems.add(Items.ELYTRA);
        rerollItems.add(Items.ENCHANTED_GOLDEN_APPLE);
        rerollItems.add(Items.HEART_OF_THE_SEA);
    }

    public static void startScan(ServerWorld world) {
        WorldBorder border = world.getWorldBorder();
        currentBorder = border;

        double size = border.getSize();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();

        if (size > 59999900) return;

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
            progressionMultiplier += 0.5;
        } else {
            hasInnerBounds = false;
        }

        currentX = minX;
        currentY = minY;
        currentZ = minZ;
        scannedBlocks = 0;

        int rangeX = maxX - minX;
        int rangeY = maxY - minY;
        int rangeZ = maxZ - minZ;
        totalBlocks = rangeX * rangeY * rangeZ;

        if (hasInnerBounds) {
            int innerRangeX = innerMaxX - innerMinX;
            int innerRangeY = rangeY;
            int innerRangeZ = innerMaxZ - innerMinZ;
            totalBlocks -= (innerRangeX * innerRangeY * innerRangeZ);
        }

        isScanning = true;
        scanned = false;

        lastScannedSize = size;
        lastScannedCenterX = centerX;
        lastScannedCenterZ = centerZ;
    }

    public static void tick(ServerWorld world) {
        if (!isScanning) return;

        int blocksThisTick = 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (blocksThisTick < BLOCKS_PER_TICK && isScanning) {
            if (shouldScanBlock(currentX, currentZ)) {
                // Check if this position is actually inside the world border
                if (currentBorder.contains(currentX, currentZ)) {
                    pos.set(currentX, currentY, currentZ);

                    if (world.isChunkLoaded(currentX >> 4, currentZ >> 4)) {
                        BlockState state = world.getBlockState(pos);
                        Item item = getItemFromBlock(state);

                        if (item != null && item != Items.AIR) {
                            availableResources.put(item, availableResources.getOrDefault(item, 0) + 1);
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
                        finishScan();
                        return;
                    }
                }
            }
        }
    }

    private static boolean shouldScanBlock(int x, int z) {
        if (!hasInnerBounds) return true;
        return x < innerMinX || x >= innerMaxX || z < innerMinZ || z >= innerMaxZ;
    }

    private static void finishScan() {
        isScanning = false;
        scanned = true;
        currentBorder = null;
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

        return state.getBlock().asItem();
    }

    public static Item getRandomAvailableItem(Random random) {
        if (availableResources.isEmpty()) {
            return Items.DIAMOND;
        }

        List<Item> items = new ArrayList<>(availableResources.keySet());
        return items.get(random.nextInt(items.size()));
    }

    public static int getRequiredCount(Item item, int completionCount) {
        Integer available = availableResources.get(item);
        if (available == null) return 16;

        boolean isRenewable = renewableItems.contains(item);
        double basePercentage = isRenewable ? 0.1 : 0.05;
        int baseCount = Math.max(1, (int) (available * basePercentage * progressionMultiplier));

        baseCount = Math.min(baseCount, 512);

        int powerOfTwo = 1;
        while (powerOfTwo < baseCount) {
            powerOfTwo *= 2;
        }
        if (powerOfTwo > baseCount * 1.5) {
            powerOfTwo /= 2;
        }

        int powerIncrease = completionCount / 10;
        int finalCount = powerOfTwo * (int) Math.pow(2, powerIncrease);

        return Math.min(finalCount, 4096);
    }

    public static boolean isScanning() {
        return isScanning;
    }

    public static boolean isScanned() {
        return scanned;
    }

    public static int getProgress() {
        if (totalBlocks == 0) return 0;
        return (int) ((scannedBlocks / (double) totalBlocks) * 100);
    }

    public static boolean isRerollItem(Item item) {
        return rerollItems.contains(item);
    }

    public static void reset() {
        availableResources.clear();
        scanned = false;
        isScanning = false;
        lastScannedSize = 0;
        progressionMultiplier = 1.0;
        currentBorder = null;
    }
}
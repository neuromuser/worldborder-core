package com.neuromuser.worldbordercore.entity;

import com.neuromuser.worldbordercore.CoreState;
import com.neuromuser.worldbordercore.WorldScanner;
import com.neuromuser.worldbordercore.config.Config;
import com.neuromuser.worldbordercore.config.ConfigManager;
import com.neuromuser.worldbordercore.items.RolledItem;
import com.neuromuser.worldbordercore.items.WorldRollContext;
import com.neuromuser.worldbordercore.mixin.ArmorStandEntityAccessor;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WorldBorderCoreEntity extends MobEntity {
    private static final int REROLL_TIME = 20 * 24000;
    private static final double COLLECTION_RADIUS = 1.2;

    private static final TrackedData<String> REQUIRED_ITEM =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> REQUIRED_COUNT =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> COMPLETION_COUNT =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> HAS_SCANNED =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private UUID textDisplayUuid;
    private int ticksSinceLastCollection = 0;
    private boolean hasRolledFirstRequirement = false;
    private int ticksUntilNextRequirement = 0;
    private boolean waitingForScanToRoll = false;

    public WorldBorderCoreEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setPersistent();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(REQUIRED_ITEM, "");
        this.dataTracker.startTracking(REQUIRED_COUNT, 0);
        this.dataTracker.startTracking(COMPLETION_COUNT, 0);
        this.dataTracker.startTracking(HAS_SCANNED, false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void tick() {
        super.tick();
        this.setVelocity(Vec3d.ZERO);

        if (this.getWorld().isClient) return;

        if (WorldScanner.isScanned() && !this.dataTracker.get(HAS_SCANNED)) {
            this.dataTracker.set(HAS_SCANNED, true);
            if (!hasRolledFirstRequirement) {
                rollNewRequirement();
                hasRolledFirstRequirement = true;
            }
        }

        if (waitingForScanToRoll && WorldScanner.isScanned() && !WorldScanner.isScanning()) {
            waitingForScanToRoll = false;
            rollNewRequirement();
        }

        if (WorldScanner.isScanning()) {
            if (ticksUntilNextRequirement > 0) {
                ticksUntilNextRequirement = 0;
            }
        } else {
            if (ticksUntilNextRequirement > 0) {
                ticksUntilNextRequirement--;
            }
        }

        if (hasRolledFirstRequirement && !waitingForScanToRoll && getRequiredCount() > 0) {
            if (this.age % 10 == 0) collectItems();
            if (++ticksSinceLastCollection >= REROLL_TIME) rollNewRequirement();
        }

        if (this.age % 80 == 0) playAmbientSound();
        if (this.age > 20 && this.age % 5 == 0) updateDisplay((ServerWorld) this.getWorld());
    }

    private void updateDisplay(ServerWorld world) {
        ArmorStandEntity textDisplay = getTextDisplay(world);
        if (textDisplay == null) {
            textDisplay = createTextDisplay(world);
        }

        if (textDisplay == null) return;

        textDisplay.setPosition(this.getX(), this.getY() + 1.2, this.getZ());

        Text displayText;
        if (WorldScanner.isScanning()) {
            int progress = WorldScanner.getProgress();
            displayText = Text.translatable("worldbordercore.display.generating", progress);
        }
        else if (!WorldScanner.isScanned()) {
            displayText = Text.translatable("worldbordercore.display.waiting");
        }
        else if (getRequiredCount() > 0) {
            Item required = getRequiredItem();
            if (required != null && required != Items.AIR) {
                displayText = Text.translatable("worldbordercore.display.requirement",
                        getRequiredCount(), required.getName());
            } else {
                displayText = Text.translatable("worldbordercore.display.initializing");
            }
        } else {
            displayText = Text.translatable("worldbordercore.display.initializing");
        }

        if (!displayText.equals(textDisplay.getCustomName())) {
            textDisplay.setCustomName(displayText);
            textDisplay.calculateDimensions();
        }
    }

    private ArmorStandEntity createTextDisplay(ServerWorld world) {
        ArmorStandEntity display = EntityType.ARMOR_STAND.create(world);
        if (display == null) return null;

        display.setPosition(this.getX(), this.getY() + 1.2, this.getZ());
        ((ArmorStandEntityAccessor) display).invokeSetMarker(true);
        display.setInvisible(true);
        display.setNoGravity(true);
        display.setCustomNameVisible(true);
        display.setCustomName(Text.literal(""));

        world.spawnEntity(display);
        this.textDisplayUuid = display.getUuid();
        return display;
    }

    private ArmorStandEntity getTextDisplay(ServerWorld world) {
        if (this.textDisplayUuid == null) return null;
        Entity entity = world.getEntity(this.textDisplayUuid);
        return (entity instanceof ArmorStandEntity) ? (ArmorStandEntity) entity : null;
    }

    private void collectItems() {
        if (!hasRolledFirstRequirement) return;
        if (getRequiredCount() <= 0) return;

        Item requiredItem = getRequiredItem();
        if (requiredItem == null || requiredItem == Items.AIR) return;

        Box box = this.getBoundingBox().expand(COLLECTION_RADIUS);
        List<ItemEntity> items = this.getWorld().getEntitiesByClass(
                ItemEntity.class, box,
                e -> !e.isRemoved()
        );

        for (ItemEntity item : items) {
            ItemStack stack = item.getStack();

            if (stack.isOf(requiredItem)) {
                int taken = Math.min(stack.getCount(), getRequiredCount());

                stack.decrement(taken);
                this.dataTracker.set(REQUIRED_COUNT, getRequiredCount() - taken);
                this.ticksSinceLastCollection = 0;

                this.getWorld().playSound(null, this.getBlockPos(),
                        SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.2f, 1.0f + this.random.nextFloat() * 0.2f);

                if (stack.isEmpty()) item.discard();

                ((ServerWorld) this.getWorld()).spawnParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        item.getX(), item.getY() + 0.5, item.getZ(),
                        8, 0.2, 0.2, 0.2, 0.05
                );

                if (getRequiredCount() <= 0) {
                    onRequirementFulfilled();
                    break;
                }
            } else if (WorldScanner.isRerollItem(stack.getItem()) && stack.getCount() >= 1) {
                if (!WorldScanner.isScanned()) {return;}
                stack.decrement(1);
                if (stack.isEmpty()) item.discard();

                ((ServerWorld) this.getWorld()).spawnParticles(
                        ParticleTypes.END_ROD,
                        item.getX(), item.getY() + 0.5, item.getZ(),
                        20, 0.3, 0.3, 0.3, 0.1
                );

                this.getWorld().playSound(null, this.getBlockPos(),
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1f, 1.2f);

                rollNewRequirement();
                break;
            }
        }
    }

    private void onRequirementFulfilled() {
        ServerWorld world = (ServerWorld) this.getWorld();
        Config config = ConfigManager.get();

        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0f, 0.9f + this.random.nextFloat() * 0.2f);
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 2.0f, 1.0f);

        world.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.5, 0.5, 0.5, 0.2);
        world.spawnParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.0, this.getZ(), 50, 0.5, 0.5, 0.5, 0.5);

        int completionCount = this.dataTracker.get(COMPLETION_COUNT);

        double diamondChance =
                config.diamondRewardBaseChance +
                        (completionCount * config.diamondRewardChanceIncreasePerLevel);

        double mendingChance =
                config.mendingRewardBaseChance +
                        (completionCount * config.mendingRewardChanceIncreasePerLevel);

        float rng = this.random.nextFloat();

        if (rng < diamondChance) {
            int minDiamonds = Math.min(config.diamondRewardMinAmount, config.diamondRewardMaxAmount);
            int maxDiamonds = Math.max(config.diamondRewardMinAmount, config.diamondRewardMaxAmount);
            int diamonds = minDiamonds + this.random.nextInt(maxDiamonds - minDiamonds + 1);

            ItemStack stack = new ItemStack(Items.DIAMOND, diamonds);
            ItemEntity entity = new ItemEntity(world, this.getX(), this.getY() + 1.0, this.getZ(), stack);
            entity.setVelocity(this.random.nextDouble() * 0.5 - 0.25, 0.5, this.random.nextDouble() * 0.5 - 0.25);
            world.spawnEntity(entity);

            world.playSound(null, this.getBlockPos(),
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5f, 1.5f);
        }
        else if (rng < diamondChance + mendingChance) {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(
                    book,
                    new EnchantmentLevelEntry(Enchantments.MENDING, 1)
            );

            ItemEntity entity = new ItemEntity(world, this.getX(), this.getY() + 1.0, this.getZ(), book);
            entity.setVelocity(this.random.nextDouble() * 0.5 - 0.25, 0.5, this.random.nextDouble() * 0.5 - 0.25);
            world.spawnEntity(entity);

            world.playSound(null, this.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1.0f, 1.2f);
        }


        WorldBorder border = world.getWorldBorder();
        border.setSize(border.getSize() + config.borderIncreaseAmount);

        int completions = this.dataTracker.get(COMPLETION_COUNT) + 1;
        this.dataTracker.set(COMPLETION_COUNT, completions);

        this.dataTracker.set(REQUIRED_ITEM, "");
        this.dataTracker.set(REQUIRED_COUNT, 0);

        WorldScanner.startScan(world);
        waitingForScanToRoll = true;
    }

    private void rollNewRequirement() {
        if (WorldScanner.isScanning()) return;
        if (!WorldScanner.isScanned()) return;

        WorldBorder border = this.getWorld().getWorldBorder();
        double borderSize = border.getSize();
        int completions = this.dataTracker.get(COMPLETION_COUNT);

        ServerWorld serverWorld = (ServerWorld) this.getWorld();

        WorldRollContext context = new WorldRollContext(
                serverWorld,
                borderSize,
                completions,
                new HashMap<>(WorldScanner.availableResources),
                WorldScanner.getScannedBiomes(),
                ConfigManager.get(),
                this.random
        );

        RolledItem rolledItem = WorldScanner.getRandomAvailableRolledItem(
                this.random, borderSize, completions, serverWorld
        );

        int count = rolledItem.calculateRequiredCount(context);
        Item item = rolledItem.getMinecraftItem();

        String itemId = Registry.ITEM.getId(item).toString();
        this.dataTracker.set(REQUIRED_ITEM, itemId);
        this.dataTracker.set(REQUIRED_COUNT, count);
        this.ticksSinceLastCollection = 0;

        this.getWorld().playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0f, 0.8f);
        ((ServerWorld) this.getWorld()).spawnParticles(
                ParticleTypes.ENCHANT,
                this.getX(), this.getY() + 1.0, this.getZ(),
                15, 0.3, 0.3, 0.3, 0.1
        );
    }

    public Item getRequiredItem() {
        String itemId = this.dataTracker.get(REQUIRED_ITEM);
        if (itemId == null || itemId.isEmpty()) {
            return Items.AIR;
        }

        try {
            return Registry.ITEM.get(new Identifier(itemId));
        } catch (Exception e) {
            return Items.AIR;
        }
    }

    public int getRequiredCount() {
        return this.dataTracker.get(REQUIRED_COUNT);
    }

    public void playAmbientSound() {
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS, 0.6f, 0.8f);
    }

    @Override
    public boolean damage(DamageSource src, float amt) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.getWorld().isClient) {
            ServerWorld world = (ServerWorld) this.getWorld();

            ArmorStandEntity textDisplay = getTextDisplay(world);
            if (textDisplay != null) textDisplay.discard();

            CoreState state = world.getPersistentStateManager()
                    .getOrCreate(CoreState::fromNbt, CoreState::new, "worldborder_core");
            if (this.getUuid().equals(state.getCoreUuid())) {
                state.clearCoreUuid();
            }
        }
        super.remove(reason);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("RequiredItem", this.dataTracker.get(REQUIRED_ITEM));
        nbt.putInt("RequiredCount", this.dataTracker.get(REQUIRED_COUNT));
        nbt.putInt("CompletionCount", this.dataTracker.get(COMPLETION_COUNT));
        nbt.putBoolean("HasRolledFirst", this.hasRolledFirstRequirement);
        nbt.putInt("TicksUntilNext", this.ticksUntilNextRequirement);
        nbt.putBoolean("HasScanned", this.dataTracker.get(HAS_SCANNED));
        nbt.putBoolean("WaitingForScan", this.waitingForScanToRoll);

        if (this.textDisplayUuid != null) {
            nbt.putUuid("TextDisplayUuid", this.textDisplayUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("RequiredItem")) {
            this.dataTracker.set(REQUIRED_ITEM, nbt.getString("RequiredItem"));
        }
        if (nbt.contains("RequiredCount")) {
            this.dataTracker.set(REQUIRED_COUNT, nbt.getInt("RequiredCount"));
        }
        if (nbt.contains("CompletionCount")) {
            this.dataTracker.set(COMPLETION_COUNT, nbt.getInt("CompletionCount"));
        }
        if (nbt.contains("HasRolledFirst")) {
            this.hasRolledFirstRequirement = nbt.getBoolean("HasRolledFirst");
        }
        if (nbt.contains("TicksUntilNext")) {
            this.ticksUntilNextRequirement = nbt.getInt("TicksUntilNext");
        }
        if (nbt.contains("HasScanned")) {
            this.dataTracker.set(HAS_SCANNED, nbt.getBoolean("HasScanned"));
        }
        if (nbt.contains("WaitingForScan")) {
            this.waitingForScanToRoll = nbt.getBoolean("WaitingForScan");
        }

        if (nbt.contains("TextDisplayUuid")) {
            this.textDisplayUuid = nbt.getUuid("TextDisplayUuid");
        }
    }
}
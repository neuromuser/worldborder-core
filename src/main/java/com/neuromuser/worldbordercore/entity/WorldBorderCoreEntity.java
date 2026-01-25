package com.neuromuser.worldbordercore.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.List;

public class WorldBorderCoreEntity extends MobEntity {

    private static final int REROLL_TIME = 20 * 24000;
    private static final int COLLECTION_RADIUS = 15;
    private static final int COLLECTION_HEIGHT = 5;

    
    public static final TrackedData<String> REQUIRED_ITEM_ID =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> REQUIRED_COUNT =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private int ticksSinceLastCollection = 0;
    private int soundTimer = 0;
    private static final int SOUND_INTERVAL = 80;

    public WorldBorderCoreEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.noClip = false;
        this.setInvulnerable(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        
        this.dataTracker.startTracking(REQUIRED_ITEM_ID, "minecraft:diamond");
        this.dataTracker.startTracking(REQUIRED_COUNT, 20);

        
        if (!this.getWorld().isClient &&
                this.dataTracker.get(REQUIRED_ITEM_ID).equals("minecraft:diamond") &&
                this.dataTracker.get(REQUIRED_COUNT) == 20) {

            
            if (this.age < 5) {
                rollNewRequirement();
            }
        }
    }

    @Override
    protected void initGoals() {
        
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void tick() {
        super.tick();

        
        this.setVelocity(0, 0, 0);
        this.velocityModified = true;

        if (!this.getWorld().isClient) {
            
            updatePositionToWorldBorderCenter();
            collectNearbyItems();
            ticksSinceLastCollection++;
            if (ticksSinceLastCollection >= REROLL_TIME) {
                rerollRequirement();
            }
            soundTimer++;
            if (soundTimer >= SOUND_INTERVAL) {
                playAmbientSound();
                soundTimer = 0;
            }
        }
    }

    private void updatePositionToWorldBorderCenter() {
        WorldBorder border = this.getWorld().getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        
        double targetY = this.getWorld().getTopY(
                net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (int)centerX,
                (int)centerZ
        ) + 1.0;

        
        this.setPosition(centerX, targetY, centerZ);
    }
    private void collectNearbyItems() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        Box collectionBox = new Box(
                x - COLLECTION_RADIUS, y - COLLECTION_HEIGHT, z - COLLECTION_RADIUS,
                x + COLLECTION_RADIUS, y + COLLECTION_HEIGHT, z + COLLECTION_RADIUS
        );

        Item currentRequiredItem = getRequiredItem();
        int currentRequiredCount = getRequiredCount();

        if (currentRequiredCount <= 0) {
            return;
        }

        List<ItemEntity> nearbyItems = this.getWorld().getEntitiesByClass(
                ItemEntity.class,
                collectionBox,
                item -> {
                    ItemStack stack = item.getStack();
                    return !stack.isEmpty() &&
                            stack.getItem() == currentRequiredItem &&
                            !item.isRemoved();
                }
        );

        boolean collectedAny = false;

        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack stack = itemEntity.getStack();
            int count = stack.getCount();
            
            int toConsume = Math.min(count, currentRequiredCount);
            if (toConsume <= 0) {
                continue;
            }
            
            int newRequiredCount = currentRequiredCount - toConsume;
            stack.decrement(toConsume);
            
            this.dataTracker.set(REQUIRED_COUNT, newRequiredCount);
            currentRequiredCount = newRequiredCount;
            
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        5, 0.2, 0.2, 0.2, 0.1
                );
            }

            if (stack.isEmpty()) {
                itemEntity.discard();
            }

            collectedAny = true;
            ticksSinceLastCollection = 0;

            
            if (currentRequiredCount <= 0) {
                onRequirementFulfilled();
                break;
            }
        }

        if (collectedAny) {
            
            this.getWorld().playSound(null, this.getBlockPos(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);

            this.getWorld().playSound(null, this.getBlockPos(),
                    SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(),
                    SoundCategory.BLOCKS, 0.5f, 1.2f);
        }
    }

    private void onRequirementFulfilled() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            
            serverWorld.spawnParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0
            );

            for (int i = 0; i < 50; i++) {
                serverWorld.spawnParticles(
                        ParticleTypes.FIREWORK,
                        this.getX(), this.getY(), this.getZ(),
                        1,
                        serverWorld.random.nextGaussian() * 0.5,
                        serverWorld.random.nextGaussian() * 0.5,
                        serverWorld.random.nextGaussian() * 0.5,
                        0.5
                );
            }

            
            this.getWorld().playSound(null, this.getBlockPos(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE,
                    SoundCategory.BLOCKS, 2.0f, 1.0f);
            
            WorldBorder border = this.getWorld().getWorldBorder();
            double newSize = border.getSize() + 10.0;
            border.setSize(newSize);
            
            Text message = Text.literal("§6World Border Core satisfied! Border expanded by 10 blocks!");
            serverWorld.getPlayers().forEach(player -> player.sendMessage(message, false));
            
            rollNewRequirement();
            
            announceNewRequirement(serverWorld);
        }
    }

    private void rollNewRequirement() {
        
        List<Item> allItems = Registries.ITEM.stream()
                .filter(item -> item != Items.AIR && !item.getDefaultStack().isEmpty())
                .toList();

        Item newItem;
        if (!allItems.isEmpty()) {
            newItem = allItems.get(this.random.nextInt(allItems.size()));
        } else {
            newItem = Items.DIAMOND;
        }

        int newCount = this.random.nextInt(64) + 1;

        this.dataTracker.set(REQUIRED_ITEM_ID, Registries.ITEM.getId(newItem).toString());
        this.dataTracker.set(REQUIRED_COUNT, newCount);

        this.ticksSinceLastCollection = 0;
    }

    private void rerollRequirement() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Text message = Text.literal("§cWorld Border Core's requirement timed out! Rolling new requirement...");
            serverWorld.getPlayers().forEach(player -> player.sendMessage(message, false));

            rollNewRequirement();
            announceNewRequirement(serverWorld);
        }
    }

    private void announceNewRequirement(ServerWorld world) {
        Item currentItem = getRequiredItem();
        int currentCount = getRequiredCount();
        String itemName = currentItem.getName().getString();
        Text message = Text.literal("§eWorld Border Core now requires: §f" + currentCount + "x " + itemName);
        world.getPlayers().forEach(player -> player.sendMessage(message, false));
    }

    public void playAmbientSound() {
        this.getWorld().playSound(null, this.getBlockPos(),
                SoundEvents.BLOCK_BEACON_AMBIENT,
                SoundCategory.BLOCKS, 0.5f, 0.7f);
    }

    
    public Item getRequiredItem() {
        String itemId = this.dataTracker.get(REQUIRED_ITEM_ID);
        return Registries.ITEM.getOrEmpty(new Identifier(itemId))
                .orElse(Items.DIAMOND);
    }

    public int getRequiredCount() {
        return this.dataTracker.get(REQUIRED_COUNT);
    }

    public int getTicksSinceLastCollection() {
        return ticksSinceLastCollection;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("RequiredItem", this.dataTracker.get(REQUIRED_ITEM_ID));
        nbt.putInt("RequiredCount", this.dataTracker.get(REQUIRED_COUNT));
        nbt.putInt("TicksSinceLastCollection", ticksSinceLastCollection);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("RequiredItem")) {
            this.dataTracker.set(REQUIRED_ITEM_ID, nbt.getString("RequiredItem"));
        }
        if (nbt.contains("RequiredCount")) {
            this.dataTracker.set(REQUIRED_COUNT, nbt.getInt("RequiredCount"));
        }
        this.ticksSinceLastCollection = nbt.getInt("TicksSinceLastCollection");
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected boolean canStartRiding(net.minecraft.entity.Entity entity) {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
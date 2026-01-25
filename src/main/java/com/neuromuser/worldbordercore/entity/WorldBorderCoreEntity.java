package com.neuromuser.worldbordercore.entity;

import com.neuromuser.worldbordercore.CoreState;
import com.neuromuser.worldbordercore.mixin.ArmorStandEntityAccessor;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
import java.util.UUID;

public class WorldBorderCoreEntity extends MobEntity {
    private static final int REROLL_TIME = 20 * 24000;
    private static final double COLLECTION_RADIUS = 2.5;

    private static final TrackedData<String> REQUIRED_ITEM =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> REQUIRED_COUNT =
            DataTracker.registerData(WorldBorderCoreEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private UUID displayEntityUuid;
    private int ticksSinceLastCollection = 0;

    public WorldBorderCoreEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setPersistent();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(REQUIRED_ITEM, "minecraft:diamond");
        this.dataTracker.startTracking(REQUIRED_COUNT, 20);
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

        if (this.age % 10 == 0) collectItems();
        if (++ticksSinceLastCollection >= REROLL_TIME) rollNewRequirement();
        if (this.age % 80 == 0) playAmbientSound();
        if (this.age > 20 && this.age % 5 == 0) updateDisplay((ServerWorld) this.getWorld());
    }

    private void updateDisplay(ServerWorld world) {
        if (this.displayEntityUuid != null && world.getEntity(this.displayEntityUuid) == null) {
            return;
        }

        ArmorStandEntity display = getDisplayEntity(world);

        if (display == null) {
            display = EntityType.ARMOR_STAND.create(world);
            if (display == null) return;

            ((ArmorStandEntityAccessor) display).invokeSetMarker(true);
            display.setInvisible(true);
            display.setNoGravity(true);
            display.setCustomNameVisible(true);
            display.setCustomName(Text.literal("WorldBorderCoreDisplay"));

            world.spawnEntity(display);
            this.displayEntityUuid = display.getUuid();
        }

        display.setPosition(this.getX(), this.getY() - 0.8, this.getZ());
        display.setCustomName(Text.literal("§e" + getRequiredCount() + "x §f" +
                getRequiredItem().getName().getString()));
        display.equipStack(EquipmentSlot.HEAD, new ItemStack(getRequiredItem()));
    }

    private ArmorStandEntity getDisplayEntity(ServerWorld world) {
        if (this.displayEntityUuid == null) return null;
        Entity entity = world.getEntity(this.displayEntityUuid);
        return (entity instanceof ArmorStandEntity) ? (ArmorStandEntity) entity : null;
    }

    private void collectItems() {
        if (getRequiredCount() <= 0) return;

        Box box = this.getBoundingBox().expand(COLLECTION_RADIUS);
        List<ItemEntity> items = this.getWorld().getEntitiesByClass(
                ItemEntity.class, box,
                e -> e.getStack().isOf(getRequiredItem()) && !e.isRemoved()
        );

        for (ItemEntity item : items) {
            ItemStack stack = item.getStack();
            int taken = Math.min(stack.getCount(), getRequiredCount());

            stack.decrement(taken);
            this.dataTracker.set(REQUIRED_COUNT, getRequiredCount() - taken);
            this.ticksSinceLastCollection = 0;

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
        }
    }

    private void onRequirementFulfilled() {
        ServerWorld world = (ServerWorld) this.getWorld();
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1f, 1f);

        WorldBorder border = world.getWorldBorder();
        border.setSize(border.getSize() + 10.0);

        rollNewRequirement();
    }

    private void rollNewRequirement() {
        List<Item> pool = Registries.ITEM.stream()
                .filter(i -> i != Items.AIR && i.getName().getString().length() > 1)
                .toList();

        Item item = pool.get(this.random.nextInt(pool.size()));
        int count = this.random.nextBetween(5, 32);

        this.dataTracker.set(REQUIRED_ITEM, Registries.ITEM.getId(item).toString());
        this.dataTracker.set(REQUIRED_COUNT, count);
        this.ticksSinceLastCollection = 0;
    }

    public Item getRequiredItem() {
        return Registries.ITEM.get(new Identifier(this.dataTracker.get(REQUIRED_ITEM)));
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
            ArmorStandEntity display = getDisplayEntity(world);
            if (display != null) display.discard();

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
        // Save the item ID and the count
        nbt.putString("RequiredItem", this.dataTracker.get(REQUIRED_ITEM));
        nbt.putInt("RequiredCount", this.dataTracker.get(REQUIRED_COUNT));

        // Also save the display UUID so it doesn't duplicate armor stands
        if (this.displayEntityUuid != null) {
            nbt.putUuid("DisplayEntityUuid", this.displayEntityUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        // Load the item ID and the count back into the DataTracker
        if (nbt.contains("RequiredItem")) {
            this.dataTracker.set(REQUIRED_ITEM, nbt.getString("RequiredItem"));
        }
        if (nbt.contains("RequiredCount")) {
            this.dataTracker.set(REQUIRED_COUNT, nbt.getInt("RequiredCount"));
        }

        // Restore the link to the armor stand
        if (nbt.contains("DisplayEntityUuid")) {
            this.displayEntityUuid = nbt.getUuid("DisplayEntityUuid");
        }
    }}
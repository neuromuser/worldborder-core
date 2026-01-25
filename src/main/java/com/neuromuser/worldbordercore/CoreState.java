package com.neuromuser.worldbordercore;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import java.util.UUID;

public class CoreState extends PersistentState {
    private UUID coreUuid = null;

    public CoreState() {
        super();
    }

    public static CoreState fromNbt(NbtCompound nbt) {
        CoreState state = new CoreState();
        if (nbt.contains("CoreUUID")) {
            state.coreUuid = nbt.getUuid("CoreUUID");
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        if (coreUuid != null) {
            nbt.putUuid("CoreUUID", coreUuid);
        }
        return nbt;
    }

    public UUID getCoreUuid() {
        return coreUuid;
    }

    public void setCoreUuid(UUID uuid) {
        this.coreUuid = uuid;
        markDirty();
    }

    public void clearCoreUuid() {
        this.coreUuid = null;
        markDirty();
    }
}
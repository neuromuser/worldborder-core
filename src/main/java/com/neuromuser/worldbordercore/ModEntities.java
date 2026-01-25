package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<WorldBorderCoreEntity> WORLD_BORDER_CORE =
            EntityType.Builder.create(WorldBorderCoreEntity::new, SpawnGroup.MISC)
                    .setDimensions(1.0f, 1.0f)
                    .maxTrackingRange(10)
                    .trackingTickInterval(Integer.MAX_VALUE)
                    .build(new Identifier("worldborder-core", "worldborder_core").toString());
}
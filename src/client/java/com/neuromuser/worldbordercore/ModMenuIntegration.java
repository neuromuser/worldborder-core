package com.neuromuser.worldbordercore;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        Config config = ConfigManager.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("World Border Core Config"));

        builder.setSavingRunnable(() -> {
            ConfigManager.save(FabricLoader.getInstance().getConfigDir().resolve("worldborder-core.json"));
        });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        var category = builder.getOrCreateCategory(Text.literal("Settings"));

        category.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Renewable Multiplier"),
                        config.renewableMultiplier)
                .setDefaultValue(2.5)
                .setMin(1.0)
                .setMax(20.0)
                .setTooltip(Text.literal("Farmable items: wood, crops, mob drops (2.5 = 2.5x what exists)"))
                .setSaveConsumer(val -> config.renewableMultiplier = val)
                .build());

        category.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Non-Renewable Multiplier"),
                        config.nonRenewableMultiplier)
                .setDefaultValue(0.3)
                .setMin(0.1)
                .setMax(1.0)
                .setTooltip(Text.literal("Ores & limited resources (0.3 = 30% of what exists, capped at 100%)"))
                .setSaveConsumer(val -> config.nonRenewableMultiplier = val)
                .build());

        category.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Progression Multiplier"),
                        config.progressionMultiplier)
                .setDefaultValue(1.15)
                .setMin(1.0)
                .setMax(3.0)
                .setTooltip(Text.literal("How much harder each completion (1.15 = 15% harder each time)"))
                .setSaveConsumer(val -> config.progressionMultiplier = val)
                .build());

        category.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Randomness Variation"),
                        config.randomnessVariation)
                .setDefaultValue(0.25)
                .setMin(0.0)
                .setMax(0.5)
                .setTooltip(Text.literal("Random variation in counts (0.25 = ±25% randomness)"))
                .setSaveConsumer(val -> config.randomnessVariation = val)
                .build());

        return builder.build();
    }
}
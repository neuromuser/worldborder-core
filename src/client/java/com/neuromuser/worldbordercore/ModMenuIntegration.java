package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.config.Config;
import com.neuromuser.worldbordercore.config.ConfigManager;
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

        builder.setSavingRunnable(() -> ConfigManager.save(FabricLoader.getInstance().getConfigDir().resolve("worldborder-core.json")));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        var categoryGeneral = builder.getOrCreateCategory(Text.literal("General"));
        var categoryRewards = builder.getOrCreateCategory(Text.literal("Rewards"));

        categoryGeneral.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Renewable Multiplier"),
                        config.renewableMultiplier)
                .setDefaultValue(1.5)
                .setMin(1.0)
                .setMax(20.0)
                .setTooltip(Text.literal("Farmable items: wood, crops, mob drops (1.5 = 1.5x what exists)"))
                .setSaveConsumer(val -> config.renewableMultiplier = val)
                .build());

        categoryGeneral.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Non-Renewable Multiplier"),
                        config.nonRenewableMultiplier)
                .setDefaultValue(0.3)
                .setMin(0.1)
                .setMax(1.0)
                .setTooltip(Text.literal("Ores & limited resources (0.3 = 30% of what exists, capped at 100%)"))
                .setSaveConsumer(val -> config.nonRenewableMultiplier = val)
                .build());

        categoryGeneral.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Progression Multiplier"),
                        config.progressionMultiplier)
                .setDefaultValue(1.1)
                .setMin(1.0)
                .setMax(3.0)
                .setTooltip(Text.literal("How much harder each completion (1.1 = 10% harder each time)"))
                .setSaveConsumer(val -> config.progressionMultiplier = val)
                .build());

        categoryGeneral.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Randomness Variation"),
                        config.randomnessVariation)
                .setDefaultValue(0.10)
                .setMin(0.0)
                .setMax(0.5)
                .setTooltip(Text.literal("Random variation in counts (0.1 = Â±10% randomness)"))
                .setSaveConsumer(val -> config.randomnessVariation = val)
                .build());

        categoryGeneral.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Border Increase Amount"),
                        config.borderIncreaseAmount)
                .setDefaultValue(10.0)
                .setMin(1.0)
                .setMax(100.0)
                .setTooltip(Text.literal("How much the border increases per completion (blocks)"))
                .setSaveConsumer(val -> config.borderIncreaseAmount = val)
                .build());

        categoryRewards.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Diamond Reward Base Chance"),
                        config.diamondRewardBaseChance)
                .setDefaultValue(0.1)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Text.literal("Base chance for diamond reward (0.1 = 10%)"))
                .setSaveConsumer(val -> config.diamondRewardBaseChance = val)
                .build());

        categoryRewards.addEntry(entryBuilder.startIntField(
                        Text.literal("Diamond Reward Min Amount"),
                        config.diamondRewardMinAmount)
                .setDefaultValue(1)
                .setMin(1)
                .setMax(64)
                .setTooltip(Text.literal("Minimum diamonds dropped when reward triggers"))
                .setSaveConsumer(val -> config.diamondRewardMinAmount = val)
                .build());

        categoryRewards.addEntry(entryBuilder.startIntField(
                        Text.literal("Diamond Reward Max Amount"),
                        config.diamondRewardMaxAmount)
                .setDefaultValue(4)
                .setMin(1)
                .setMax(64)
                .setTooltip(Text.literal("Maximum diamonds dropped when reward triggers"))
                .setSaveConsumer(val -> config.diamondRewardMaxAmount = val)
                .build());

        categoryRewards.addEntry(entryBuilder.startDoubleField(
                        Text.literal("Diamond Chance Increase Per Level"),
                        config.diamondRewardChanceIncreasePerLevel)
                .setDefaultValue(0.01)
                .setMin(0.0)
                .setMax(0.1)
                .setTooltip(Text.literal("How much the diamond chance increases per completion (0.01 = +1%)"))
                .setSaveConsumer(val -> config.diamondRewardChanceIncreasePerLevel = val)
                .build());

        return builder.build();
    }
}
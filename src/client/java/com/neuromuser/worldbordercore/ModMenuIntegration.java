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
                .setTitle(Text.literal("Worldborder Core Config"));

        builder.setSavingRunnable(() -> {
            ConfigManager.save(FabricLoader.getInstance().getConfigDir().resolve("worldborder-core.json"));
        });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        var category = builder.getOrCreateCategory(Text.literal("Settings"));

        category.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enabled"), config.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.enabled = val)
                .build());

        category.addEntry(entryBuilder.startIntField(Text.literal("Radius"), config.radius)
                .setDefaultValue(16)
                .setMin(0)
                .setMax(100)
                .setSaveConsumer(val -> config.radius = val)
                .build());

        category.addEntry(entryBuilder.startDoubleField(Text.literal("Multiplier"), config.multiplier)
                .setDefaultValue(1.5)
                .setMin(0.0)
                .setMax(10.0)
                .setSaveConsumer(val -> config.multiplier = val)
                .build());

        category.addEntry(entryBuilder.startStrField(Text.literal("Message"), config.message)
                .setDefaultValue("Hello World")
                .setSaveConsumer(val -> config.message = val)
                .build());

        return builder.build();
    }
}
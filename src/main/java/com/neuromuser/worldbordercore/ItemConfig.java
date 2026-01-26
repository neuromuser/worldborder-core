package com.neuromuser.worldbordercore;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ItemConfig {
    private static final Map<Item, ItemUnlockData> itemData = new HashMap<>();

    public record ItemUnlockData(Item item, int minBorderSize, boolean requiresWorldCheck, boolean renewable,
                                 int baseCount, double multiplier) {
    }

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("worldborder-core");
        Path configPath = configDir.resolve("items.json");

        try {
            Files.createDirectories(configDir);
        } catch (IOException ignored) {}

        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            itemData.clear(); 

            for (String key : json.keySet()) {
                if (key.startsWith("_")) continue; 

                try {
                    JsonObject itemJson = json.getAsJsonObject(key);
                    Item item = Registry.ITEM.get(new Identifier(key));

                    int minBorderSize = itemJson.get("minBorderSize").getAsInt();
                    boolean requiresWorldCheck = itemJson.get("requiresWorldCheck").getAsBoolean();
                    boolean renewable = itemJson.get("renewable").getAsBoolean();
                    int baseCount = itemJson.get("baseCount").getAsInt();
                    double multiplier = itemJson.get("multiplier").getAsDouble();

                    ItemUnlockData data = new ItemUnlockData(
                            item, minBorderSize, requiresWorldCheck,
                            renewable, baseCount, multiplier
                    );

                    itemData.put(item, data);
                } catch (Exception ignored) {}
            }

            if (itemData.isEmpty()) {
                loadFallbackConfig();
            }

        } catch (Exception e) {
            loadFallbackConfig();
        }
    }

    private static void createDefaultConfig(Path configPath) {
        try {
            InputStream inputStream = ItemConfig.class.getResourceAsStream(
                    "/assets/worldborder-core/defaults/items.json"
            );

            if (inputStream != null) {
                Files.copy(inputStream, configPath);
            } else {
                createMinimalConfig(configPath);
            }
        } catch (IOException e) {
            createMinimalConfig(configPath);
        }
    }

    private static void createMinimalConfig(Path configPath) {
        try {
            String minimalConfig = """
                {
                  "_comment": "World Border Core - Minimal Item Configuration",
                  "minecraft:stone": {
                    "minBorderSize": 20,
                    "requiresWorldCheck": false,
                    "renewable": true,
                    "baseCount": 64,
                    "multiplier": 0.8
                  },
                  "minecraft:oak_log": {
                    "minBorderSize": 25,
                    "requiresWorldCheck": false,
                    "renewable": true,
                    "baseCount": 48,
                    "multiplier": 0.8
                  },
                  "minecraft:diamond": {
                    "minBorderSize": 60,
                    "requiresWorldCheck": false,
                    "renewable": false,
                    "baseCount": 8,
                    "multiplier": 0.2
                  }
                }
                """;

            Files.writeString(configPath, minimalConfig);
        } catch (IOException ignored) {
        }
    }

    private static void loadFallbackConfig() {
        itemData.clear();

        addFallback("minecraft:stone", 20, true, 64, 0.8);
        addFallback("minecraft:oak_log", 25, true, 48, 0.8);
        addFallback("minecraft:diamond", 60, false, 8, 0.2);
        addFallback("minecraft:iron_ingot", 40, false, 16, 0.3);
        addFallback("minecraft:coal", 30, false, 24, 0.3);
    }

    private static void addFallback(String itemId, int minBorder,
                                    boolean renewable, int baseCount, double multiplier) {
        try {
            Item item = Registry.ITEM.get(new Identifier(itemId));
            itemData.put(item, new ItemUnlockData(
                    item, minBorder, false, renewable, baseCount, multiplier
            ));
        } catch (Exception ignored) {}
    }

    public static ItemUnlockData getData(Item item) {
        return itemData.get(item);
    }

    public static Map<Item, ItemUnlockData> getAllData() {
        return new HashMap<>(itemData);
    }
}
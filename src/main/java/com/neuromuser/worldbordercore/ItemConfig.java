package com.neuromuser.worldbordercore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ItemConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemConfig.class);
    private static Map<Item, ItemUnlockData> itemData = new HashMap<>();

    public static class ItemUnlockData {
        public final Item item;
        public final int minBorderSize;
        public final boolean requiresWorldCheck;
        public final boolean renewable;
        public final int baseCount;
        public final double multiplier;

        public ItemUnlockData(Item item, int minBorderSize, boolean requiresWorldCheck,
                              boolean renewable, int baseCount, double multiplier) {
            this.item = item;
            this.minBorderSize = minBorderSize;
            this.requiresWorldCheck = requiresWorldCheck;
            this.renewable = renewable;
            this.baseCount = baseCount;
            this.multiplier = multiplier;
        }
    }

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("worldborder-core");
        Path configPath = configDir.resolve("items.json");

        LOGGER.info("Loading item config from: {}", configPath);

        // Create config directory if it doesn't exist
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }

        // Create default config if it doesn't exist
        if (!Files.exists(configPath)) {
            LOGGER.info("items.json not found, creating default configuration...");
            createDefaultConfig(configPath);
        }

        // Load the config
        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            itemData.clear(); // Clear existing data

            for (String key : json.keySet()) {
                if (key.startsWith("_")) continue; // Skip comments

                try {
                    JsonObject itemJson = json.getAsJsonObject(key);
                    Item item = Registries.ITEM.get(new Identifier(key));

                    if (item == null) {
                        LOGGER.warn("Unknown item in config: " + key);
                        continue;
                    }

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
                    LOGGER.debug("Loaded config for item: {}", key);
                } catch (Exception e) {
                    LOGGER.error("Error parsing item config for: " + key, e);
                }
            }

            LOGGER.info("Successfully loaded {} items from configuration", itemData.size());

            // If no items were loaded, use fallback
            if (itemData.isEmpty()) {
                LOGGER.warn("No items loaded from config, using fallback");
                loadFallbackConfig();
            }

        } catch (IOException e) {
            LOGGER.error("Failed to load items.json", e);
            loadFallbackConfig();
        } catch (Exception e) {
            LOGGER.error("Unexpected error loading config", e);
            loadFallbackConfig();
        }
    }

    private static void createDefaultConfig(Path configPath) {
        try {
            // Get resource from classpath
            InputStream inputStream = ItemConfig.class.getResourceAsStream(
                    "/assets/worldborder-core/defaults/items.json"
            );

            if (inputStream != null) {
                Files.copy(inputStream, configPath);
                LOGGER.info("Successfully created default items.json from resources.");
            } else {
                LOGGER.error("Could not find default config in resources");
                // Create a minimal config file
                createMinimalConfig(configPath);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to copy default items.json", e);
            // Create a minimal config file as last resort
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
            LOGGER.info("Created minimal items.json");
        } catch (IOException e) {
            LOGGER.error("Failed to create minimal config", e);
        }
    }

    private static void loadFallbackConfig() {
        LOGGER.warn("Using fallback configuration with basic items only");
        itemData.clear();

        // Add some basic fallback items to prevent crashes
        addFallback("minecraft:stone", 20, false, true, 64, 0.8);
        addFallback("minecraft:oak_log", 25, false, true, 48, 0.8);
        addFallback("minecraft:diamond", 60, false, false, 8, 0.2);
        addFallback("minecraft:iron_ingot", 40, false, false, 16, 0.3);
        addFallback("minecraft:coal", 30, false, false, 24, 0.3);

        LOGGER.info("Loaded {} fallback items", itemData.size());
    }

    private static void addFallback(String itemId, int minBorder, boolean worldCheck,
                                    boolean renewable, int baseCount, double multiplier) {
        try {
            Item item = Registries.ITEM.get(new Identifier(itemId));
            if (item != null) {
                itemData.put(item, new ItemUnlockData(
                        item, minBorder, worldCheck, renewable, baseCount, multiplier
                ));
                LOGGER.debug("Added fallback item: {}", itemId);
            } else {
                LOGGER.warn("Could not find item for fallback: {}", itemId);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to add fallback item: " + itemId, e);
        }
    }

    public static ItemUnlockData getData(Item item) {
        return itemData.get(item);
    }

    public static Map<Item, ItemUnlockData> getAllData() {
        return new HashMap<>(itemData);
    }

    public static boolean hasData(Item item) {
        return itemData.containsKey(item);
    }

    public static void reload() {
        itemData.clear();
        load();
    }
}
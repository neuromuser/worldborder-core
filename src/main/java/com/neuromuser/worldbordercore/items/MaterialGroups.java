package com.neuromuser.worldbordercore.items;

import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MaterialGroups {

    public static final TagKey<Item> PLANKS = ItemTags.PLANKS;
    public static final TagKey<Item> LOGS = ItemTags.LOGS;
    public static final TagKey<Item> WOOL = ItemTags.WOOL;
    public static final TagKey<Item> STONE_CRAFTING_MATERIALS = ItemTags.STONE_CRAFTING_MATERIALS;
    public static final TagKey<Item> SAPLINGS = ItemTags.SAPLINGS;
    public static final TagKey<Item> FLOWERS = ItemTags.FLOWERS;
    public static final TagKey<Item> FISHES = ItemTags.FISHES;

    public static final TagKey<Item> INGOTS_IRON = createCommonTag("ingots/iron");
    public static final TagKey<Item> INGOTS_GOLD = createCommonTag("ingots/gold");
    public static final TagKey<Item> INGOTS_COPPER = createCommonTag("ingots/copper");
    public static final TagKey<Item> GEMS_DIAMOND = createCommonTag("gems/diamond");
    public static final TagKey<Item> GEMS_EMERALD = createCommonTag("gems/emerald");
    public static final TagKey<Item> DUSTS_REDSTONE = createCommonTag("dusts/redstone");
    public static final TagKey<Item> GEMS_COAL = createCommonTag("gems/coal");

    private static TagKey<Item> createTag(String name) {
        return TagKey.of(Registry.ITEM.getKey(), new Identifier("worldborder-core", name));
    }

    private static TagKey<Item> createCommonTag(String name) {
        return TagKey.of(Registry.ITEM.getKey(), new Identifier("c", name));
    }

    public static TagKey<Item> tag(String namespacedId) {
        return TagKey.of(Registry.ITEM.getKey(), new Identifier(namespacedId));
    }

    public static TagKey<Item> tag(String namespace, String path) {
        return TagKey.of(Registry.ITEM.getKey(), new Identifier(namespace, path));
    }
}
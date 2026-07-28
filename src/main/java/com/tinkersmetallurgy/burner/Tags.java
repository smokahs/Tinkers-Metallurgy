package com.tinkersmetallurgy.burner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.tinkersmetallurgy.TinkersMetallurgy;

// the tags the burner reads. all four ship with the mod and are meant to be edited by packs.
public final class Tags {

    private Tags() {}

    // only consulted while burner.ignoresFuelTagWhitelist is off. the blacklist always applies.
    public static final TagKey<Item> FUEL_WHITELIST = itemTag("basic_burner_fuel_whitelist");

    public static final TagKey<Item> FUEL_BLACKLIST = itemTag("basic_burner_fuel_blacklist");

    public static final TagKey<Item> BURNER_STARTERS = itemTag("burner_starters");

    // blocks that count as low heat for basin recipes without being burners. empty by default: this is
    // where a pack puts campfires back if it wants them lighting recipes but not boilers.
    public static final TagKey<Block> LOWHEAT_RECIPE_HEATERS = blockTag("lowheat_recipe_heaters");

    private static TagKey<Item> itemTag(String path) {
        return ItemTags.create(new ResourceLocation(TinkersMetallurgy.MOD_ID, path));
    }

    private static TagKey<Block> blockTag(String path) {
        return BlockTags.create(new ResourceLocation(TinkersMetallurgy.MOD_ID, path));
    }
}

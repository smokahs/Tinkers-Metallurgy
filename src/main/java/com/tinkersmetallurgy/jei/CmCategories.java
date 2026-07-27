package com.tinkersmetallurgy.jei;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingBasinRecipe;
import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingTableRecipe;

import com.tinkersmetallurgy.TinkersMetallurgy;

import mezz.jei.api.recipe.RecipeType;

// no jei call hands back a tab by id alone, so the only way to name one is to rebuild an equal
// instance from its id and recipe class. kept out of Plugin because naming these classes loads
// them, and every use sits behind a create metallurgy presence check.
final class CmCategories {

    private CmCategories() {}

    static List<RecipeType<?>> casting() {
        return List.of(
                new RecipeType<>(id("casting_in_table"), CastingTableRecipe.class),
                new RecipeType<>(id("casting_in_basin"), CastingBasinRecipe.class));
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(TinkersMetallurgy.CM, name);
    }
}

package com.tinkersmetallurgy.mixin;

import net.minecraft.world.item.crafting.Ingredient;

import com.simibubi.create.foundation.fluid.FluidIngredient;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// opens the mold slot and the fluid, both protected with no setter, for recipe.Molds and
// recipe.FluidRewrite. needs its own accessor because a casting recipe is not a ProcessingRecipe: it
// implements Recipe straight off and keeps its own copy of these fields.
@Mixin(CastingRecipe.class)
public interface CastingRecipeAccessor {

    @Accessor(value = "ingredient", remap = false)
    void tinkersmetallurgy$setIngredient(Ingredient ingredient);

    @Accessor(value = "fluidIngredient", remap = false)
    void tinkersmetallurgy$setFluidIngredient(FluidIngredient fluidIngredient);
}

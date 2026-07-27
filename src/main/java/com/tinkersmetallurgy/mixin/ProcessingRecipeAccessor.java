package com.tinkersmetallurgy.mixin;

import net.minecraft.core.NonNullList;

import net.minecraftforge.fluids.FluidStack;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// create keeps a processing recipe's fluids protected with no way in, so this opens the two fields
// for repointing the create metallurgy recipes at the tinkers fluids.
@Mixin(ProcessingRecipe.class)
public interface ProcessingRecipeAccessor {

    @Accessor(value = "fluidIngredients", remap = false)
    NonNullList<FluidIngredient> tinkersmetallurgy$fluidIngredients();

    @Accessor(value = "fluidIngredients", remap = false)
    void tinkersmetallurgy$setFluidIngredients(NonNullList<FluidIngredient> ingredients);

    @Accessor(value = "fluidResults", remap = false)
    NonNullList<FluidStack> tinkersmetallurgy$fluidResults();

    @Accessor(value = "fluidResults", remap = false)
    void tinkersmetallurgy$setFluidResults(NonNullList<FluidStack> results);
}

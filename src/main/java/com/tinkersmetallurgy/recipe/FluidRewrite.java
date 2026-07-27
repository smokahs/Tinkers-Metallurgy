package com.tinkersmetallurgy.recipe;

import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.fluids.FluidStack;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingRecipe;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;
import com.tinkersmetallurgy.mixin.CastingRecipeAccessor;
import com.tinkersmetallurgy.mixin.ProcessingRecipeAccessor;

// with the tinkers fluids winning, a create metallurgy recipe still using its own molten iron would
// be stranded, so the fluid is swapped and everything else left alone. only the eighteen shared
// metals are touched; obdurium, lithium, void steel and necromium keep their own fluid.
public final class FluidRewrite {

    private FluidRewrite() {}

    public static void run(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType) {
        if (!Cfg.INSTANCE.substituteCmFluids.get()) {
            return;
        }

        int rewritten = 0;
        for (Map<ResourceLocation, Recipe<?>> map : byType.values()) {
            for (Recipe<?> recipe : map.values()) {
                if (!recipe.getId().getNamespace().equals(TinkersMetallurgy.CM)) {
                    continue;
                }
                // casting keeps its own fluid field, so without a pass of its own it would hold on to
                // the folded away fluid and stop matching anything.
                boolean changed = recipe instanceof CastingRecipe casting
                        ? rewrite(casting)
                        : recipe instanceof ProcessingRecipe<?> processing && rewrite(processing);
                if (changed) {
                    rewritten++;
                }
            }
        }

        if (rewritten > 0) {
            TinkersMetallurgy.LOGGER.info("Repointed {} Create: Metallurgy recipes at Tinkers' fluids", rewritten);
        }
    }

    private static boolean rewrite(CastingRecipe recipe) {
        FluidIngredient swapped = swap(recipe.getFluidIngredient());
        if (swapped == recipe.getFluidIngredient()) {
            return false;
        }
        ((CastingRecipeAccessor) recipe).tinkersmetallurgy$setFluidIngredient(swapped);
        return true;
    }

    private static boolean rewrite(ProcessingRecipe<?> recipe) {
        ProcessingRecipeAccessor accessor = (ProcessingRecipeAccessor) recipe;
        boolean changed = false;

        NonNullList<FluidIngredient> inputs = accessor.tinkersmetallurgy$fluidIngredients();
        if (inputs != null && !inputs.isEmpty()) {
            NonNullList<FluidIngredient> replaced = NonNullList.create();
            boolean any = false;
            for (FluidIngredient ingredient : inputs) {
                FluidIngredient swapped = swap(ingredient);
                any |= swapped != ingredient;
                replaced.add(swapped);
            }
            if (any) {
                accessor.tinkersmetallurgy$setFluidIngredients(replaced);
                changed = true;
            }
        }

        NonNullList<FluidStack> results = accessor.tinkersmetallurgy$fluidResults();
        if (results != null && !results.isEmpty()) {
            NonNullList<FluidStack> replaced = NonNullList.create();
            boolean any = false;
            for (FluidStack result : results) {
                FluidStack swapped = Fluids.canonical(result);
                any |= swapped != result;
                replaced.add(swapped);
            }
            if (any) {
                accessor.tinkersmetallurgy$setFluidResults(replaced);
                changed = true;
            }
        }

        return changed;
    }

    // ingredients accepting several fluids collapse onto the first, as create's own does.
    private static FluidIngredient swap(FluidIngredient ingredient) {
        for (FluidStack stack : ingredient.getMatchingFluidStacks()) {
            Fluid canon = Fluids.canonical(stack.getFluid());
            if (canon != stack.getFluid()) {
                return FluidIngredient.fromFluid(canon, ingredient.getRequiredAmount());
            }
        }
        return ingredient;
    }
}

package com.tinkersmetallurgy.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import net.minecraftforge.fluids.FluidStack;

import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;

import fr.lucreeper74.createmetallurgy.content.blocks.foundry_mixer.AlloyingRecipe;

// turns tinkers alloy recipes into create metallurgy foundry mixer recipes. the catalyst is the one
// idea that does not survive: the mixer cannot require a fluid without using it up, so it becomes an
// input that is handed straight back out.
public final class Alloying {

    private Alloying() {}

    // the mixer inherits create's basin limit of four fluid inputs.
    private static final int MAX_FLUID_INPUTS = 4;

    // tinkers alloying has no duration field, so converted recipes use a flat one.
    private static final int DURATION = 100;

    @Nullable
    public static Recipe<?> convert(AlloyRecipe source) {
        List<AlloyRecipe.AlloyIngredient> inputs = source.getInputs();
        if (inputs.isEmpty()) {
            return null;
        }

        List<FluidIngredient> converted = new ArrayList<>();
        List<FluidStack> catalystRefunds = new ArrayList<>();
        for (AlloyRecipe.AlloyIngredient input : inputs) {
            FluidIngredient ingredient = translate(input.fluid());
            if (ingredient == null) {
                return null;
            }
            converted.add(ingredient);
            if (input.catalyst()) {
                List<FluidStack> matching = ingredient.getMatchingFluidStacks();
                if (!matching.isEmpty()) {
                    FluidStack refund = matching.get(0).copy();
                    refund.setAmount(ingredient.getRequiredAmount());
                    catalystRefunds.add(refund);
                }
            }
        }

        if (converted.size() > MAX_FLUID_INPUTS) {
            return null;
        }

        FluidStack result = Fluids.canonical(source.getOutput());
        if (result.isEmpty()) {
            return null;
        }

        List<FluidStack> outputs = new ArrayList<>();
        outputs.add(result);
        outputs.addAll(catalystRefunds);

        ResourceLocation id = Ids.derive(source.getId(), "alloying");
        ProcessingRecipeBuilder<AlloyingRecipe> builder =
                new ProcessingRecipeBuilder<>(AlloyingRecipe::new, id);
        builder.duration(DURATION)
                .requiresHeat(Heat.condition(source.getTemperature()))
                .withFluidIngredients(converted.toArray(new FluidIngredient[0]))
                .withFluidOutputs(outputs.toArray(new FluidStack[0]));

        return builder.build();
    }

    @Nullable
    private static FluidIngredient translate(slimeknights.mantle.recipe.ingredient.FluidIngredient source) {
        List<FluidStack> fluids = source.getFluids();
        if (fluids.isEmpty()) {
            return null;
        }
        FluidStack first = Fluids.canonical(fluids.get(0));
        if (first.isEmpty()) {
            return null;
        }
        return FluidIngredient.fromFluid(first.getFluid(), first.getAmount());
    }
}

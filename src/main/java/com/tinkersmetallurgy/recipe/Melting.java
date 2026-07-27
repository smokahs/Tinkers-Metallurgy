package com.tinkersmetallurgy.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import net.minecraftforge.fluids.FluidStack;

import slimeknights.tconstruct.library.recipe.melting.DamageableMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.melting.Damaged;

// tinkers melting is a family: plain, ore with its rate multiplier, damageable, and material melting
// standing in for every tool part. the first three answer getOutputWithByproducts() with their
// multipliers applied and the last expands per material, so the whole family converts the same way.
public final class Melting {

    private Melting() {}

    // create's basin takes two output fluids. tinkers allows more byproducts than that.
    private static final int MAX_FLUID_OUTPUTS = 2;

    public static Recipe<?> convert(MeltingRecipe source) {
        Ingredient input = source.getInput();
        if (input.isEmpty()) {
            return null;
        }

        List<FluidStack> outputs = flatten(source);
        if (outputs.isEmpty()) {
            return null;
        }

        ResourceLocation id = Ids.derive(source.getId(), "melting");
        int duration = Math.max(1, source.getTime(EmptyMeltingContainer.INSTANCE));
        HeatCondition heat = Heat.condition(source.getTemperature(EmptyMeltingContainer.INSTANCE));
        FluidStack[] results = outputs.toArray(new FluidStack[0]);

        if (source instanceof DamageableMeltingRecipe) {
            ProcessingRecipeBuilder<Damaged> builder =
                    new ProcessingRecipeBuilder<>(Damaged::new, id);
            builder.require(input)
                    .duration(duration)
                    .requiresHeat(heat)
                    .withFluidOutputs(results);
            Damaged recipe = builder.build();
            recipe.setSource(source);
            return recipe;
        }

        ProcessingRecipeBuilder<fr.lucreeper74.createmetallurgy.content.blocks.foundry_lid.MeltingRecipe> builder =
                new ProcessingRecipeBuilder<>(
                        fr.lucreeper74.createmetallurgy.content.blocks.foundry_lid.MeltingRecipe::new, id);

        builder.require(input)
                .duration(duration)
                .requiresHeat(heat)
                .withFluidOutputs(results);

        return builder.build();
    }

    // squashes output and byproducts into one list. the main output comes first and is always kept;
    // only trailing byproducts are trimmed, which in practice hits a few seared glass recipes.
    private static List<FluidStack> flatten(MeltingRecipe source) {
        List<FluidStack> out = new ArrayList<>();
        for (List<FluidStack> group : source.getOutputWithByproducts()) {
            if (out.size() >= MAX_FLUID_OUTPUTS) {
                TinkersMetallurgy.LOGGER.debug(
                        "Trimmed byproducts from {}; the foundry basin holds {} output fluids",
                        source.getId(), MAX_FLUID_OUTPUTS);
                break;
            }
            if (!group.isEmpty()) {
                FluidStack first = group.get(0);
                if (!first.isEmpty()) {
                    out.add(Fluids.canonical(first));
                }
            }
        }
        return out;
    }

    private static final class EmptyMeltingContainer implements slimeknights.tconstruct.library.recipe.melting.IMeltingContainer {

        static final EmptyMeltingContainer INSTANCE = new EmptyMeltingContainer();

        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public IOreRate getOreRate() {
            return slimeknights.tconstruct.common.config.Config.COMMON.foundryOreRate;
        }
    }
}

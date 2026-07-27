package com.tinkersmetallurgy.melting;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;

import com.tinkersmetallurgy.recipe.Basin;
import com.tinkersmetallurgy.recipe.Fluids;

// a create processing recipe states its results up front and never sees its input, so a plain
// conversion would make melting a nearly broken tool free metal. this keeps the tinkers recipe and
// asks it for the real yield when create reads the results, finding the stack via Basin.
public final class Damaged
        extends fr.lucreeper74.createmetallurgy.content.blocks.foundry_lid.MeltingRecipe {

    @Nullable
    private MeltingRecipe source;

    public Damaged(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(params);
    }

    public void setSource(MeltingRecipe source) {
        this.source = source;
    }

    @Override
    public NonNullList<FluidStack> getFluidResults() {
        NonNullList<FluidStack> base = super.getFluidResults();
        MeltingRecipe recipe = this.source;
        if (recipe == null || base.isEmpty()) {
            return base;
        }

        BasinBlockEntity basin = Basin.current();
        if (basin == null) {
            return base;
        }

        ItemStack input = findInput(basin, recipe);
        if (input.isEmpty()) {
            return base;
        }

        FluidStack scaled = Fluids.canonical(recipe.getOutput(new Container(input)));
        if (scaled.isEmpty()) {
            return base;
        }

        // only the main output scales. byproducts keep the amounts they were converted with.
        NonNullList<FluidStack> out = NonNullList.create();
        out.add(scaled);
        for (int i = 1; i < base.size(); i++) {
            out.add(base.get(i));
        }
        return out;
    }

    private static ItemStack findInput(BasinBlockEntity basin, MeltingRecipe recipe) {
        var inventory = basin.getInputInventory();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && recipe.getInput().test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}

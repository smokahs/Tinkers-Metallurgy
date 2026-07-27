package com.tinkersmetallurgy.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import net.minecraftforge.fluids.FluidStack;

import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import com.simibubi.create.foundation.fluid.FluidIngredient;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingOutput;

import com.tinkersmetallurgy.casting.Recipes;

// tinkers has a couple dozen casting serialisers, most working out their result from context rather
// than stating it. leaning on the display view they all provide for jei hands over exactly what a
// create metallurgy recipe needs, covering every casting type without a mixin.
public final class Casting {

    private Casting() {}

    public enum Kind {
        TABLE,
        BASIN
    }

    public static List<Recipe<?>> convert(Recipe<?> source, Kind kind, RegistryAccess registries) {
        List<Recipe<?>> out = new ArrayList<>();
        for (IDisplayableCastingRecipe display : displays(source, registries)) {
            build(source.getId(), display, kind, out);
        }
        return out;
    }

    private static List<IDisplayableCastingRecipe> displays(Recipe<?> source, RegistryAccess registries) {
        if (source instanceof IMultiRecipe<?> multi) {
            List<IDisplayableCastingRecipe> out = new ArrayList<>();
            for (Object expanded : multi.getRecipes(registries)) {
                if (expanded instanceof IDisplayableCastingRecipe display) {
                    out.add(display);
                }
            }
            return out;
        }
        if (source instanceof IDisplayableCastingRecipe display) {
            return List.of(display);
        }
        return List.of();
    }

    private static void build(ResourceLocation sourceId, IDisplayableCastingRecipe display,
                              Kind kind, List<Recipe<?>> out) {
        ItemStack result = display.getOutput();
        if (result.isEmpty()) {
            return;
        }

        List<FluidStack> fluids = display.getFluids();
        if (fluids.isEmpty()) {
            return;
        }

        Ingredient mold = mold(display);
        int duration = Math.max(1, display.getCoolingTime());
        boolean consumed = display.hasCast() && display.isConsumed();

        // a recipe may accept several fluids. create ingredients hold one, so each gets its own.
        for (FluidStack fluid : fluids) {
            FluidStack canon = Fluids.canonical(fluid);
            if (canon.isEmpty()) {
                continue;
            }
            ResourceLocation id = Ids.derive(sourceId,
                    kind == Kind.TABLE ? "casting_table" : "casting_basin", out.size());
            FluidIngredient ingredient = FluidIngredient.fromFluid(canon.getFluid(), canon.getAmount());
            CastingOutput output = CastingOutput.fromStack(result.copy());
            out.add(kind == Kind.TABLE
                    ? new Recipes.Table(id, mold, ingredient, output, duration, consumed)
                    : new Recipes.Basin(id, mold, ingredient, output, duration, consumed));
        }
    }

    // a recipe with no cast keeps an empty ingredient, which create metallurgy matches against an
    // empty mold slot: the same thing tinkers means by no cast.
    private static Ingredient mold(IDisplayableCastingRecipe display) {
        if (!display.hasCast()) {
            return Ingredient.EMPTY;
        }
        List<ItemStack> casts = display.getCastItems();
        if (casts.isEmpty()) {
            return Ingredient.EMPTY;
        }
        return Ingredient.of(casts.toArray(new ItemStack[0]));
    }
}

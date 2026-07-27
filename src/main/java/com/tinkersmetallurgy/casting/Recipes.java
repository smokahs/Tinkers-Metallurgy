package com.tinkersmetallurgy.casting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import com.simibubi.create.foundation.fluid.FluidIngredient;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingBasinRecipe;
import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingOutput;
import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingTableRecipe;

// create metallurgy only builds casting recipes from json, so there is no public constructor.
// subclassing reaches the protected fields: a mold, a fluid, an output and a duration.
public final class Recipes {

    private Recipes() {}

    public static final class Table extends CastingTableRecipe {

        public Table(ResourceLocation id, Ingredient mold, FluidIngredient fluid,
                     CastingOutput output, int duration, boolean moldConsumed) {
            super(id);
            this.ingredient = mold;
            this.fluidIngredient = fluid;
            this.result = output;
            this.processingDuration = duration;
            this.moldConsumed = moldConsumed;
        }
    }

    public static final class Basin extends CastingBasinRecipe {

        public Basin(ResourceLocation id, Ingredient mold, FluidIngredient fluid,
                     CastingOutput output, int duration, boolean moldConsumed) {
            super(id);
            this.ingredient = mold;
            this.fluidIngredient = fluid;
            this.result = output;
            this.processingDuration = duration;
            this.moldConsumed = moldConsumed;
        }
    }
}

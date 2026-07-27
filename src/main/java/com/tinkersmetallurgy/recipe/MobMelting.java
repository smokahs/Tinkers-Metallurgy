package com.tinkersmetallurgy.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Recipe;

import net.minecraftforge.fluids.FluidStack;

import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;

import fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.recipes.EntityMeltingRecipeBuilder;

// both mods model mob boiling almost the same, except the create metallurgy matcher holds one entity
// type while tinkers often matches a tag, so a tagged recipe fans out into one crucible recipe each.
public final class MobMelting {

    private MobMelting() {}

    // tinkers entity melting carries no temperature, so the crucible heat window comes from how hot
    // the output fluid is: molten gold should need a hotter crucible than blood.
    public static List<Recipe<?>> convert(EntityMeltingRecipe source) {
        List<Recipe<?>> out = new ArrayList<>();
        FluidStack result = Fluids.canonical(source.getOutput());
        if (result.isEmpty()) {
            return out;
        }

        Collection<EntityType<?>> types = source.getInputs();
        if (types.isEmpty()) {
            return out;
        }

        int damage = source.getDamage();
        int minHeat = Heat.minHeatForFluid(result.getFluid().getFluidType().getTemperature(result));

        int index = 0;
        for (EntityType<?> type : types) {
            ResourceLocation id = Ids.derive(source.getId(), "entity_melting", index++);
            EntityMeltingRecipeBuilder builder = new EntityMeltingRecipeBuilder(
                    fr.lucreeper74.createmetallurgy.content.blocks.industrial_crucible.foundry.recipes.EntityMeltingRecipe::new,
                    id);
            builder.requireEntity(type, damage);
            builder.requiresMinHeat(minHeat)
                    .requiresMaxHeat(Heat.maxHeat());
            builder.withFluidOutputs(result.copy());
            out.add(builder.build());
        }
        return out;
    }
}

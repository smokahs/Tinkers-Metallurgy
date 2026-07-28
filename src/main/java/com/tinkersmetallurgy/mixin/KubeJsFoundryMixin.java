package com.tinkersmetallurgy.mixin;

import fr.lucreeper74.createmetallurgy.compat.kubejs.recipes.ProcessingRecipeSchema;

import com.tinkersmetallurgy.burner.Lowheat;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import org.spongepowered.asm.mixin.Mixin;

// the same call on create: metallurgy's own kubejs schema, which is the one the foundry lid and mixer
// recipes are written through. create low-heated had no reason to touch this; a mod that puts the
// tinkers' smeltery on those two machines does.
@Mixin(value = ProcessingRecipeSchema.ProcessingRecipeJS.class, remap = false)
public abstract class KubeJsFoundryMixin extends RecipeJS {

    public RecipeJS lowheated() {
        return setValue(ProcessingRecipeSchema.HEAT_REQUIREMENT, Lowheat.CONDITION.serialize());
    }
}

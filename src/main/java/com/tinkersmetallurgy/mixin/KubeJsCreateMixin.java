package com.tinkersmetallurgy.mixin;

import com.tinkersmetallurgy.burner.Lowheat;

import dev.latvian.mods.kubejs.create.ProcessingRecipeSchema;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import org.spongepowered.asm.mixin.Mixin;

// kubejs's create integration writes heat with heated() and superheated(). the new rung gets the
// matching call, so a script can say .lowheated() instead of setting the raw json key. rhino reflects
// over public methods, so merging one in is all it takes. ported from create low-heated (MIT).
@Mixin(value = ProcessingRecipeSchema.ProcessingRecipeJS.class, remap = false)
public abstract class KubeJsCreateMixin extends RecipeJS {

    public RecipeJS lowheated() {
        return setValue(ProcessingRecipeSchema.HEAT_REQUIREMENT, Lowheat.CONDITION.serialize());
    }
}

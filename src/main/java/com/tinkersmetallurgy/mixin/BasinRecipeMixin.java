package com.tinkersmetallurgy.mixin;

import net.minecraft.world.item.crafting.Recipe;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import com.tinkersmetallurgy.recipe.Basin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// hands the basin being processed to Basin for the length of the call. records context only; a
// converted melting recipe needs it to scale its output against the item actually being melted.
@Mixin(BasinRecipe.class)
public abstract class BasinRecipeMixin {

    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("HEAD"), remap = false)
    private static void tinkersmetallurgy$captureBasin(BasinBlockEntity basin, Recipe<?> recipe,
                                                       boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        Basin.push(basin);
    }

    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("RETURN"), remap = false)
    private static void tinkersmetallurgy$releaseBasin(BasinBlockEntity basin, Recipe<?> recipe,
                                                       boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        Basin.pop();
    }
}

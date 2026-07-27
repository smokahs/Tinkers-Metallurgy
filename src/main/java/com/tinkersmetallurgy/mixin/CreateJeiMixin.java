package com.tinkersmetallurgy.mixin;

import java.util.function.Consumer;

import net.minecraft.world.item.crafting.Recipe;

import com.simibubi.create.compat.jei.CreateJEI;

import com.tinkersmetallurgy.jei.RecipeFilter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// every create metallurgy tab is built with addTypedRecipes, which ends up here, so swapping the
// collector once covers all of them. see RecipeFilter. create's own tabs use the same method but
// hold nothing of ours, since conversion only ever writes create metallurgy recipe types.
@Mixin(CreateJEI.class)
public class CreateJeiMixin {

    // consumeTypedRecipes belongs to create, so there is no obfuscation mapping to look it up in.
    @ModifyVariable(method = "consumeTypedRecipes(Ljava/util/function/Consumer;Lnet/minecraft/world/item/crafting/RecipeType;)V",
            at = @At("HEAD"), argsOnly = true, remap = false)
    private static Consumer<Recipe<?>> tinkersmetallurgy$skipConverted(Consumer<Recipe<?>> consumer) {
        return RecipeFilter.wrap(consumer);
    }
}

package com.tinkersmetallurgy.mixin;

import slimeknights.tconstruct.plugin.jei.JEIPlugin;

import com.tinkersmetallurgy.jei.CategoryRegistration;

import mezz.jei.api.registration.IRecipeCategoryRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// swaps what tinkers registers its tabs against for one that re-skins the smeltery tabs. replacing
// the argument catches every tab at once and leaves the non smeltery ones alone by not matching.
@Mixin(JEIPlugin.class)
public class TicCategoryMixin {

    @ModifyVariable(method = "registerCategories(Lmezz/jei/api/registration/IRecipeCategoryRegistration;)V",
            at = @At("HEAD"), argsOnly = true, remap = false)
    private IRecipeCategoryRegistration tinkersmetallurgy$skinCategories(IRecipeCategoryRegistration registration) {
        return CategoryRegistration.wrap(registration);
    }
}

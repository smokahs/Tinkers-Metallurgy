package com.tinkersmetallurgy.mixin;

import slimeknights.tconstruct.plugin.jei.JEIPlugin;

import com.tinkersmetallurgy.jei.CatalystFilter;

import mezz.jei.api.registration.IRecipeCatalystRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// swaps what tinkers registers its catalysts against for one that filters out the hidden smeltery
// blocks. replacing the argument rather than each call covers its private casting helper too.
@Mixin(JEIPlugin.class)
public class TicCatalystMixin {

    @ModifyVariable(method = "registerRecipeCatalysts(Lmezz/jei/api/registration/IRecipeCatalystRegistration;)V",
            at = @At("HEAD"), argsOnly = true, remap = false)
    private IRecipeCatalystRegistration tinkersmetallurgy$filterCatalysts(IRecipeCatalystRegistration registration) {
        return CatalystFilter.wrap(registration);
    }
}

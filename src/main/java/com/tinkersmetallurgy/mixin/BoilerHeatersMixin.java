package com.tinkersmetallurgy.mixin;

import com.simibubi.create.content.fluids.tank.BoilerHeaters;

import com.tinkersmetallurgy.burner.Boiler;
import com.tinkersmetallurgy.config.Cfg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// create registers the blaze burner and the passive heater tag here. the whole call is replaced rather
// than added to, because taking the passive tag back out again afterwards is not something the registry
// offers. ported from create low-heated (MIT).
@Mixin(value = BoilerHeaters.class, remap = false)
public class BoilerHeatersMixin {

    @Inject(method = "registerDefaults", at = @At("HEAD"), cancellable = true)
    private static void tinkersmetallurgy$registerDefaults(CallbackInfo ci) {
        if (!Cfg.INSTANCE.basicBurnerBoiler.get()) {
            return;
        }
        Boiler.registerDefaults();
        ci.cancel();
    }
}

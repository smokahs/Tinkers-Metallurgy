package com.tinkersmetallurgy.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;

import com.tinkersmetallurgy.burner.Dispense;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// create low-heated wrapped the item's own dispense behaviour partway through this method. taking the
// question at the top instead asks nothing of the local variables, which is the part of that approach
// a mappings change breaks.
@Mixin(DispenserBlock.class)
public class DispenserMixin {

    @Inject(method = "dispenseFrom", at = @At("HEAD"), cancellable = true)
    private void tinkersmetallurgy$lightBurner(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (Dispense.lightBurner(level, pos)) {
            ci.cancel();
        }
    }
}

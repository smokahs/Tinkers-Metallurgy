package com.tinkersmetallurgy.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;

import com.tinkersmetallurgy.burner.BurnerEntity;
import com.tinkersmetallurgy.config.Cfg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// a fan running flat out against a burner empowers it, at a fuel bill to match. the fan has to push the
// news, since nothing about a burner would otherwise notice a neighbour changing speed. ported from
// create low-heated (MIT).
@Mixin(value = EncasedFanBlockEntity.class, remap = false)
public class EncasedFanMixin {

    @Inject(method = "onSpeedChanged", at = @At("HEAD"))
    private void tinkersmetallurgy$onSpeedChanged(float previousSpeed, CallbackInfo ci) {
        tinkersmetallurgy$updateBurner(false);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void tinkersmetallurgy$onRemove(CallbackInfo ci) {
        tinkersmetallurgy$updateBurner(true);
    }

    @Unique
    private void tinkersmetallurgy$updateBurner(boolean removed) {
        EncasedFanBlockEntity fan = (EncasedFanBlockEntity) (Object) this;
        Level level = fan.getLevel();
        if (level == null) {
            return;
        }

        // despite the name this is the side the fan faces, not the side air arrives from.
        Direction facing = fan.getAirflowOriginSide();
        if (Cfg.INSTANCE.fanHorizontalOnly.get() && !facing.getAxis().isHorizontal()) {
            return;
        }

        BlockPos target = fan.getBlockPos().relative(facing);
        if (!(level.getBlockEntity(target) instanceof BurnerEntity burner)) {
            return;
        }

        burner.setEmpowered(!removed && Mth.abs(fan.getSpeed()) >= Cfg.INSTANCE.fanSpeedRequired.get());
    }
}

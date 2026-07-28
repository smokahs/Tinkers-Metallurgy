package com.tinkersmetallurgy.mixin;

import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.BlockHelper;

import com.tinkersmetallurgy.burner.Burner;
import com.tinkersmetallurgy.burner.Lowheat;
import com.tinkersmetallurgy.burner.Tags;
import com.tinkersmetallurgy.config.Cfg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// what a basin sees underneath it. every recipe heat check in create, and every one in create:
// metallurgy since its foundry basin extends this class, comes through here. ported from create
// low-heated (MIT).
@Mixin(value = BasinBlockEntity.class, remap = false)
public class BasinHeatMixin {

    @Inject(method = "getHeatLevelOf", at = @At("HEAD"), cancellable = true)
    private static void tinkersmetallurgy$getHeatLevelOf(BlockState state,
                                                         CallbackInfoReturnable<HeatLevel> cir) {
        if (state.hasProperty(Burner.HEAT_LEVEL)) {
            cir.setReturnValue(state.getValue(Burner.HEAT_LEVEL));
            return;
        }

        // a pack's own low heat sources, which count for recipes without ever heating a boiler.
        if (state.is(Tags.LOWHEAT_RECIPE_HEATERS) && BlockHelper.isNotUnheated(state)) {
            cir.setReturnValue(Lowheat.LEVEL);
            return;
        }

        // with the burner kept out of the boiler, create's own answer stands, passive heaters included.
        if (!Cfg.INSTANCE.basicBurnerBoiler.get()) {
            return;
        }

        if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
            cir.setReturnValue(state.getValue(BlazeBurnerBlock.HEAT_LEVEL));
            return;
        }

        boolean passive = Cfg.INSTANCE.passiveBoilerHeaters.get()
                && AllTags.AllBlockTags.PASSIVE_BOILER_HEATERS.matches(state)
                && BlockHelper.isNotUnheated(state);
        cir.setReturnValue(passive ? HeatLevel.SMOULDERING : HeatLevel.NONE);
    }
}

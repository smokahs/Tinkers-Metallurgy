package com.tinkersmetallurgy.burner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import com.tinkersmetallurgy.config.Cfg;

// what heats a boiler, once the passive heaters stop doing it for free.
//
// create sorts the answer into three buckets: above zero adds to the boiler's active heat and scales
// with its size, exactly zero flags it passive and pins it to one engine's worth, and below zero is
// nothing at all. campfires and lava used to sit in the middle bucket; here only a burner with fuel in
// it does, so the same steam costs charcoal now. this is also the number create: metallurgy's crucible
// sums under its footprint, which is where the -1 per bare block in the crucible config comes from.
public final class Boiler {

    private Boiler() {}

    public static void registerDefaults() {
        BoilerHeater.REGISTRY.register(AllBlocks.BLAZE_BURNER.get(), Boiler::blazeHeat);
        BoilerHeater.REGISTRY.register(Setup.BURNER.get(), Boiler::burnerHeat);

        if (Cfg.INSTANCE.passiveBoilerHeaters.get()) {
            BoilerHeater.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(
                    AllTags.AllBlockTags.PASSIVE_BOILER_HEATERS.tag, BoilerHeater.PASSIVE));
        }
    }

    // same numbers create registers for it. restated rather than reused because create hands its own
    // out in the call being replaced.
    private static float blazeHeat(Level level, BlockPos pos, BlockState state) {
        return rate(state.getValue(BlazeBurnerBlock.HEAT_LEVEL));
    }

    // LOW is checked before isAtLeast: the rung was appended to the end of the enum, so it outranks
    // SEETHING by ordinal and would otherwise answer for every comparison.
    private static float burnerHeat(Level level, BlockPos pos, BlockState state) {
        HeatLevel value = state.getValue(Burner.HEAT_LEVEL);
        if (value == Lowheat.LEVEL) {
            return BoilerHeater.PASSIVE_HEAT;
        }
        return rate(value);
    }

    private static float rate(HeatLevel value) {
        if (value == HeatLevel.NONE) {
            return BoilerHeater.NO_HEAT;
        }
        if (value == HeatLevel.SEETHING) {
            return 2;
        }
        return value.isAtLeast(HeatLevel.FADING) ? 1 : BoilerHeater.NO_HEAT;
    }
}

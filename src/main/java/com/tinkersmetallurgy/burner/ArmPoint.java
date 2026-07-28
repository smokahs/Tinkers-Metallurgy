package com.tinkersmetallurgy.burner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;

import com.tinkersmetallurgy.TinkersMetallurgy;

// lets a mechanical arm drop fuel into a burner. arm point types sit in a plain registry rather than a
// forge one, so create registers its own from a static initialiser and calls an empty init() to trip
// it; doing the same is what keeps this to exactly one registration whenever the trigger fires.
public final class ArmPoint {

    static {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE,
                new ResourceLocation(TinkersMetallurgy.MOD_ID, "basic_burner"), new Type());
    }

    private ArmPoint() {}

    public static void init() {}

    public static class Type extends ArmInteractionPointType {

        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return Setup.BURNER.has(state);
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new DepositOnlyArmInteractionPoint(this, level, pos, state);
        }
    }
}

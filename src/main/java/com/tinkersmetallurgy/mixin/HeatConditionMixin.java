package com.tinkersmetallurgy.mixin;

import java.util.ArrayList;
import java.util.Arrays;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// the recipe side of the same rung: a fourth heat requirement below HEATED. serialize() and
// deserialize() read values(), so rewriting $VALUES is enough for "lowheated" to round trip through
// recipe json on its own. what does not follow is which burners satisfy which condition, so the two
// blaze burner tests are answered by hand. ported from create low-heated (MIT).
@Mixin(value = HeatCondition.class, remap = false)
public abstract class HeatConditionMixin {

    @Shadow
    @Final
    @Mutable
    private static HeatCondition[] $VALUES;

    // the tone create low-heated picked for the tier, kept so recipe viewers agree across packs.
    @Unique
    private static final HeatCondition LOWHEATED = tinkersmetallurgy$addVariant("LOWHEATED", 0xED9C33);

    @Invoker("<init>")
    public static HeatCondition tinkersmetallurgy$invokeInit(String internalName, int internalId, int color) {
        throw new AssertionError();
    }

    @Unique
    private static HeatCondition tinkersmetallurgy$addVariant(String internalName, int color) {
        ArrayList<HeatCondition> variants = new ArrayList<>(Arrays.asList(HeatConditionMixin.$VALUES));
        HeatCondition heat = tinkersmetallurgy$invokeInit(internalName,
                variants.get(variants.size() - 1).ordinal() + 1, color);
        variants.add(heat);
        HeatConditionMixin.$VALUES = variants.toArray(new HeatCondition[0]);
        return heat;
    }

    // create's own test is a switch that would fall through to false for the new constant, so all
    // four branches are restated. LOW is the added level and sorts last in the enum, which is why
    // this compares equality per level rather than leaning on isAtLeast.
    @Inject(method = "testBlazeBurner", at = @At("HEAD"), cancellable = true)
    private void tinkersmetallurgy$testBlazeBurner(HeatLevel level, CallbackInfoReturnable<Boolean> cir) {
        HeatCondition self = tinkersmetallurgy$self();

        if (self == HeatCondition.SUPERHEATED) {
            cir.setReturnValue(level == HeatLevel.SEETHING);
            return;
        }

        if (self == HeatCondition.HEATED) {
            cir.setReturnValue(level == HeatLevel.FADING || level == HeatLevel.KINDLED
                    || level == HeatLevel.SEETHING);
            return;
        }

        if (self == LOWHEATED) {
            cir.setReturnValue(level == HeatLevel.valueOf("LOW") || level == HeatLevel.FADING
                    || level == HeatLevel.KINDLED || level == HeatLevel.SEETHING);
        }
    }

    @Inject(method = "visualizeAsBlazeBurner", at = @At("HEAD"), cancellable = true)
    private void tinkersmetallurgy$visualizeAsBlazeBurner(CallbackInfoReturnable<HeatLevel> cir) {
        if (tinkersmetallurgy$self() == LOWHEATED) {
            cir.setReturnValue(HeatLevel.valueOf("LOW"));
        }
    }

    @Unique
    private HeatCondition tinkersmetallurgy$self() {
        return (HeatCondition) (Object) this;
    }
}

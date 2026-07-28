package com.tinkersmetallurgy.mixin;

import java.util.ArrayList;
import java.util.Arrays;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

// create's heat ladder starts at a blaze burner. the basic burner sits below it, so the enum gains a
// rung: rewriting $VALUES during the enum's own clinit is the only way to add one, since every switch
// map and values() call downstream reads the array afterwards. ported from create low-heated (MIT).
@Mixin(value = HeatLevel.class, remap = false)
public abstract class HeatLevelMixin {

    @Shadow
    @Final
    @Mutable
    private static HeatLevel[] $VALUES;

    @Unique
    private static final HeatLevel LOW = tinkersmetallurgy$addVariant("LOW");

    @Invoker("<init>")
    public static HeatLevel tinkersmetallurgy$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static HeatLevel tinkersmetallurgy$addVariant(String internalName) {
        ArrayList<HeatLevel> variants = new ArrayList<>(Arrays.asList(HeatLevelMixin.$VALUES));
        HeatLevel heat = tinkersmetallurgy$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1);
        variants.add(heat);
        HeatLevelMixin.$VALUES = variants.toArray(new HeatLevel[0]);
        return heat;
    }
}

package com.tinkersmetallurgy.recipe;

import com.simibubi.create.content.processing.recipe.HeatCondition;

import com.tinkersmetallurgy.burner.Lowheat;
import com.tinkersmetallurgy.config.Cfg;

// tinkers states melting temperatures as plain numbers, create knows four heat levels once the basic
// burner adds its own, and the crucible wants a numeric window on top. this translates between them.
public final class Heat {

    private Heat() {}

    public static final int MIN_HEAT = -50;
    public static final int MAX_HEAT = 50;

    // tinkers writes a molten metal's fluid temperature as its melting point plus forge's 300 kelvin
    // baseline, so a fluid temperature comes back down to melting points before being compared.
    public static final int FLUID_BASELINE = 300;

    public static HeatCondition condition(int temperature) {
        if (temperature >= Cfg.INSTANCE.superheatedThreshold.get()) {
            return HeatCondition.SUPERHEATED;
        }
        if (temperature >= Cfg.INSTANCE.heatedThreshold.get()) {
            return HeatCondition.HEATED;
        }
        if (temperature >= Cfg.INSTANCE.lowheatedThreshold.get()) {
            return Lowheat.CONDITION;
        }
        return HeatCondition.NONE;
    }

    // -50 to 50 is the field's range, not one a crucible reaches: a 3x3 on blaze burners reads 9, on
    // blaze cake 18. spreading tinkers temperatures across the field put molten gold at 33 and
    // nothing ever boiled, so the same step ladder create uses decides this too.
    //
    // written out rather than switched on: the enum gained a constant that create's own switches were
    // compiled without, and an exhaustive switch over it is exactly what breaks on one.
    public static int minHeat(int temperature) {
        HeatCondition condition = condition(temperature);
        if (condition == HeatCondition.SUPERHEATED) {
            return Cfg.INSTANCE.crucibleSuperheatedHeat.get();
        }
        if (condition == HeatCondition.HEATED) {
            return Cfg.INSTANCE.crucibleHeatedHeat.get();
        }
        if (condition == Lowheat.CONDITION) {
            return Cfg.INSTANCE.crucibleLowheatedHeat.get();
        }
        return 0;
    }

    public static int minHeatForFluid(int fluidTemperature) {
        return minHeat(fluidTemperature - FLUID_BASELINE);
    }

    public static int maxHeat() {
        return MAX_HEAT;
    }
}

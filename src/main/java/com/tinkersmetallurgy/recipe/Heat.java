package com.tinkersmetallurgy.recipe;

import com.simibubi.create.content.processing.recipe.HeatCondition;

import com.tinkersmetallurgy.config.Cfg;

// tinkers states melting temperatures as plain numbers, create knows three heat levels, and the
// crucible wants a numeric window on top. this translates between them.
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
        return HeatCondition.NONE;
    }

    // -50 to 50 is the field's range, not one a crucible reaches: a 3x3 on blaze burners reads 9, on
    // blaze cake 18. spreading tinkers temperatures across the field put molten gold at 33 and
    // nothing ever boiled, so the same three step ladder create uses decides this too.
    public static int minHeat(int temperature) {
        return switch (condition(temperature)) {
            case NONE -> 0;
            case HEATED -> Cfg.INSTANCE.crucibleHeatedHeat.get();
            case SUPERHEATED -> Cfg.INSTANCE.crucibleSuperheatedHeat.get();
        };
    }

    public static int minHeatForFluid(int fluidTemperature) {
        return minHeat(fluidTemperature - FLUID_BASELINE);
    }

    public static int maxHeat() {
        return MAX_HEAT;
    }
}

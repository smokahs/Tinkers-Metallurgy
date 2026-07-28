package com.tinkersmetallurgy.burner;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.HeatCondition;

// the two constants HeatLevelMixin and HeatConditionMixin add. they cannot be named directly, and the
// mixins cannot hand them over either: touching this class from inside them would load it while the
// enum it reads is still being initialised. so everything outside the mixins looks them up here, once.
public final class Lowheat {

    private Lowheat() {}

    public static final HeatLevel LEVEL = HeatLevel.valueOf("LOW");

    public static final HeatCondition CONDITION = HeatCondition.valueOf("LOWHEATED");
}

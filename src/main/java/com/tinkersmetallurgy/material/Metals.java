package com.tinkersmetallurgy.material;

import java.util.List;

import com.tinkersmetallurgy.TinkersMetallurgy;

// create metallurgy has no tool stats of its own, so these numbers are hand written from each metal's
// melting point and its place in that mod's progression, and the traits from what it is alloyed
// from. lithium is left out: a soft alkali intermediate makes a silly pickaxe.
public final class Metals {

    private Metals() {}

    // temperature is a melting point on tinkers' scale (iron 800, manyullyn 1200), not the real one
    // create metallurgy states. colour is the second brightest stop of the metal's ramp, since it is
    // multiplied over tinkers' greyscale sprite and so darkens a stop on the way.
    public record Metal(String name, String tier, int materialTier, int temperature, int durability,
                        float miningSpeed, float attack, int colour, List<Trait> traits) {

        public String fluid() {
            return "molten_" + name;
        }
    }

    public record Trait(String name, int level) {}

    private static Trait trait(String name, int level) {
        return new Trait(name, level);
    }

    public static final List<Metal> ALL = List.of(
            new Metal("obdurium", "manyullyn", 4, 1400, 1450, 9.0f, 4.5f, 0xF2D688,
                    List.of(trait("tconstruct:heavy", 1), trait("tconstruct:reinforced", 1))),
            new Metal("void_steel", "manyullyn", 4, 1450, 1320, 8.5f, 5.0f, 0x36BFA8,
                    List.of(trait("tconstruct:enderdodging", 1), trait("tconstruct:netherite", 1))),
            new Metal("necromium", "cobalt", 3, 1150, 1100, 8.0f, 4.0f, 0x7A9690,
                    List.of(trait("tconstruct:smite", 1), trait("tconstruct:netherite", 1))));

    // the sister addon replaces the vanilla ladder with an eleven step one, so aim at that when it
    // is installed and fall back to the closest vanilla tier otherwise.
    public static String tierId(String tier) {
        if (TinkersMetallurgy.nhLoaded()) {
            return TinkersMetallurgy.TIC3NH + ":" + tier;
        }
        return switch (tier) {
            case "manyullyn", "cobalt" -> "minecraft:netherite";
            default -> "minecraft:diamond";
        };
    }
}

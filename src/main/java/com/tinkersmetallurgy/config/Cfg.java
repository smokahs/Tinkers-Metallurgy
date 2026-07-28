package com.tinkersmetallurgy.config;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

import org.apache.commons.lang3.tuple.Pair;

public final class Cfg {

    public static final Cfg INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<Cfg, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Cfg::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final BooleanValue melting;
    public final BooleanValue alloying;
    public final BooleanValue entityMelting;
    public final BooleanValue casting;
    public final BooleanValue molding;

    public final IntValue lowheatedThreshold;
    public final IntValue heatedThreshold;
    public final IntValue superheatedThreshold;
    public final IntValue crucibleLowheatedHeat;
    public final IntValue crucibleHeatedHeat;
    public final IntValue crucibleSuperheatedHeat;

    public final BooleanValue hotBurners;
    public final BooleanValue basicBurnerBoiler;
    public final BooleanValue passiveBoilerHeaters;
    public final BooleanValue ignoresFuelTagWhitelist;
    public final BooleanValue ignoresBurnerStarters;
    public final IntValue baseMultiplier;
    public final IntValue fanMultiplier;
    public final IntValue fanSpeedRequired;
    public final BooleanValue fanHorizontalOnly;
    public final BooleanValue dispenserBurner;

    public final BooleanValue replaceGraphiteMolds;

    public final BooleanValue substituteCmFluids;
    public final BooleanValue hideDuplicateCmFluids;
    public final BooleanValue hideTicSmelteryBlocks;

    public final BooleanValue generateCmMaterials;

    public final ForgeConfigSpec.EnumValue<Categories> jeiCategories;
    public final BooleanValue jeiCreateHeat;

    // both mods can show the same smeltery recipe, so one has to give way.
    public enum Categories {
        CREATE_METALLURGY,
        TINKERS,
        BOTH
    }

    private Cfg(ForgeConfigSpec.Builder b) {
        b.comment("Which parts of the Tinkers' smeltery get converted onto Create: Metallurgy machines.")
                .push("convert");
        melting = b
                .comment("Convert Tinkers' melting recipes onto the Create: Metallurgy foundry lid.")
                .define("melting", true);
        alloying = b
                .comment("Convert Tinkers' alloy recipes onto the Create: Metallurgy foundry mixer.")
                .define("alloying", true);
        entityMelting = b
                .comment("Convert Tinkers' entity melting recipes onto the industrial crucible.")
                .define("entityMelting", true);
        casting = b
                .comment("Let the Create: Metallurgy casting table and basin run Tinkers' casting recipes.")
                .define("casting", true);
        molding = b
                .comment("Let the Create: Metallurgy casting table run Tinkers' molding recipes (cast creation).")
                .define("molding", true);
        b.pop();

        b.comment("Tinkers' melting temperatures are absolute; Create heat is a four step ladder.",
                "These thresholds decide where a converted recipe lands.").push("heat");
        lowheatedThreshold = b
                .comment("At or above this Tinkers' temperature a recipe requires a low-heated basin",
                        "(basic burner). Below it the recipe needs no heat at all.")
                .defineInRange("lowheatedThreshold", 1, 0, 100000);
        heatedThreshold = b
                .comment("At or above this Tinkers' temperature a recipe requires a heated basin (blaze burner).",
                        "The default splits the metals where Tinkers' own temperatures already do: tin (225),",
                        "lead (330), zinc (420) and aluminum (425) melt on a basic burner, copper (500) up to",
                        "gold (700) need a blaze, and iron (800) up needs blaze cake. Set this to 1 to put",
                        "everything back on a blaze burner the way it was before the basic burner existed.")
                .defineInRange("heatedThreshold", 500, 0, 100000);
        superheatedThreshold = b
                .comment("At or above this Tinkers' temperature a recipe requires a superheated basin (blaze cake).")
                .defineInRange("superheatedThreshold", 800, 0, 100000);
        crucibleLowheatedHeat = b
                .comment("Minimum crucible heat for a converted mob boiling recipe below the heated",
                        "threshold. A burning basic burner reads 0, the same as the passive heaters it",
                        "replaces, so 0 is what a crucible standing on them can reach: the requirement is",
                        "really that no square of the footprint is bare, since bare blocks each read -1.")
                .defineInRange("crucibleLowheatedHeat", 0, 0, 50);
        crucibleHeatedHeat = b
                .comment("Minimum crucible heat for a converted mob boiling recipe below the superheated",
                        "threshold. The crucible sums the heat of every block under its footprint: a lit",
                        "blaze burner gives 1, a superheated one 2, anything else -1. So a 3x3 crucible on",
                        "blaze burners reads 9, and on blaze cake 18. Create: Metallurgy's own recipes ask",
                        "for 6 (piglins) and 9 (iron golems), which these mirror.")
                .defineInRange("crucibleHeatedHeat", 6, 0, 50);
        crucibleSuperheatedHeat = b
                .comment("Minimum crucible heat for a converted mob boiling recipe at or above the",
                        "superheated threshold.")
                .defineInRange("crucibleSuperheatedHeat", 9, 0, 50);
        b.pop();

        b.comment("The basic burner: a fed burner standing in for the heat a blaze burner gives away, and",
                "the passive heaters it takes off the table. Ported from Create Low-Heated (MIT).").push("burner");
        hotBurners = b
                .comment("When true a burning basic burner reads as a kindled blaze burner and an empowered",
                        "one as a seething blaze burner, collapsing the low-heated tier back into the two",
                        "Create already had. An unlit burner is unaffected either way.")
                .define("hotBurners", false);
        basicBurnerBoiler = b
                .comment("Let the basic burner heat steam engines. Turning this off leaves it a recipe heater",
                        "only, and puts every passive boiler heater back as a consequence.")
                .define("basicBurnerBoiler", true);
        passiveBoilerHeaters = b
                .comment("Put the passive heaters back: campfires, lava and the rest of Create's",
                        "#create:passive_boiler_heaters tag heat boilers and basins again, for free.")
                .define("passiveBoilerHeaters", false);
        ignoresFuelTagWhitelist = b
                .comment("Accept anything with a burn time as burner fuel, rather than only the items in",
                        "#tinkersmetallurgy:basic_burner_fuel_whitelist. The blacklist applies either way.")
                .define("ignoresFuelTagWhitelist", true);
        ignoresBurnerStarters = b
                .comment("Light a burner the moment fuel goes in, skipping the flint and steel step.")
                .define("ignoresBurnerStarters", false);
        baseMultiplier = b
                .comment("How much faster an unempowered burner eats its fuel. Meant for use alongside",
                        "hotBurners; leave it at 1 otherwise.")
                .defineInRange("baseMultiplier", 1, 1, Integer.MAX_VALUE);
        fanMultiplier = b
                .comment("How much faster a fan-empowered burner eats its fuel.")
                .defineInRange("fanMultiplier", 32, 1, Integer.MAX_VALUE);
        fanSpeedRequired = b
                .comment("Fan speed needed to empower a burner.")
                .defineInRange("fanSpeedRequired", 256, 1, Integer.MAX_VALUE);
        fanHorizontalOnly = b
                .comment("Only let fans blowing sideways empower a burner.")
                .define("fanHorizontalOnly", true);
        dispenserBurner = b
                .comment("Let a dispenser light a burner with flint and steel.")
                .define("dispenserBurner", true);
        b.pop();

        b.comment("Create: Metallurgy casts with graphite molds, Tinkers' with casts. Only one is needed.")
                .push("molds");
        replaceGraphiteMolds = b
                .comment("Swap the graphite mold out of every Create: Metallurgy casting recipe for the",
                        "matching Tinkers' cast, drop the recipes Tinkers' already covers, then take the",
                        "molds out of the game: the recipes that made them are deleted and the items are",
                        "hidden from creative and the recipe viewer. Nothing is lost with them: Create:",
                        "Metallurgy casts plates, rods and gears for metals Tinkers' has no recipe for,",
                        "and is the only source for obdurium, void steel, necromium and lithium.")
                .define("replaceGraphiteMolds", true);
        b.pop();

        b.comment("Tinkers' fluids are canonical. Create: Metallurgy's duplicates get folded into them.")
                .push("fluids");
        substituteCmFluids = b
                .comment("Rewrite Create: Metallurgy recipes that reference a duplicated molten fluid",
                        "so they use the Tinkers' fluid instead. Keeps graphite molds working.")
                .define("substituteCmFluids", true);
        hideDuplicateCmFluids = b
                .comment("Hide the now unused Create: Metallurgy molten fluid buckets from creative and JEI.")
                .define("hideDuplicateCmFluids", true);
        hideTicSmelteryBlocks = b
                .comment("Hide Tinkers' smeltery blocks from creative and the recipe viewer,",
                        "since the Create: Metallurgy foundry replaces them entirely.")
                .define("hideTicSmelteryBlocks", true);
        b.pop();

        b.comment("Turning Create: Metallurgy exclusive metals into Tinkers' tool materials.")
                .push("materials");
        generateCmMaterials = b
                .comment("Generate tool material definitions for Create: Metallurgy exclusive metals",
                        "(obdurium, void steel, necromium). Lithium is excluded on purpose.")
                .define("generateCmMaterials", true);
        b.pop();

        b.comment("How the smeltery recipes present themselves in the recipe viewer.").push("jei");
        jeiCategories = b
                .comment("Which mod's categories show the smeltery recipes.",
                        "  CREATE_METALLURGY: recipes appear on the Create: Metallurgy machines,",
                        "                     and Tinkers' smeltery categories are hidden.",
                        "  TINKERS:           Tinkers' compact categories are kept and pointed at the",
                        "                     Create: Metallurgy machines; converted recipes are hidden.",
                        "  BOTH:              leave both alone, showing every recipe twice.",
                        "Tinkers' molding category is always kept, since nothing else shows cast creation.",
                        "Tinkers' melting category is dropped under TINKERS: it lists the same recipes as",
                        "its foundry category, which is the one the conversion matches.")
                .defineEnum("categories", Categories.TINKERS);
        jeiCreateHeat = b
                .comment("Draw the Create heat requirement on Tinkers' foundry category in place of its",
                        "melting point and fuel tank. Converted melting recipes run on the foundry lid,",
                        "which reads a blaze burner, not the fuels Tinkers' lists. Only applies under",
                        "TINKERS, and only while melting is being converted.")
                .define("createHeat", true);
        b.pop();
    }
}

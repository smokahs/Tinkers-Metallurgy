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

    public final IntValue heatedThreshold;
    public final IntValue superheatedThreshold;
    public final IntValue crucibleHeatedHeat;
    public final IntValue crucibleSuperheatedHeat;

    public final BooleanValue replaceGraphiteMolds;

    public final BooleanValue substituteCmFluids;
    public final BooleanValue hideDuplicateCmFluids;
    public final BooleanValue hideTicSmelteryBlocks;

    public final BooleanValue generateCmMaterials;

    public final ForgeConfigSpec.EnumValue<Categories> jeiCategories;

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

        b.comment("Tinkers' melting temperatures are absolute; Create heat is a three step ladder.",
                "These thresholds decide where a converted recipe lands.").push("heat");
        heatedThreshold = b
                .comment("At or above this Tinkers' temperature a recipe requires a heated basin (blaze burner).")
                .defineInRange("heatedThreshold", 1, 0, 100000);
        superheatedThreshold = b
                .comment("At or above this Tinkers' temperature a recipe requires a superheated basin (blaze cake).")
                .defineInRange("superheatedThreshold", 800, 0, 100000);
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
                        "Tinkers' molding category is always kept, since nothing else shows cast creation.")
                .defineEnum("categories", Categories.TINKERS);
        b.pop();
    }
}

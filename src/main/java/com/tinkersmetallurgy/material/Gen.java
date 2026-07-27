package com.tinkersmetallurgy.material;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.registries.ForgeRegistries;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// writes tinkers material files for the create metallurgy exclusive metals, in the shape tinkers
// loads from a datapack. generated rather than shipped so the harvest tier can follow whether the
// sister addon replaced the tier ladder.
public final class Gen {

    private Gen() {}

    private static final String NS = TinkersMetallurgy.MOD_ID;

    private static final int SORT_BASE = 400;

    public record Generated(Map<ResourceLocation, byte[]> data, Map<ResourceLocation, byte[]> assets) {}

    public static Generated generate() {
        Map<ResourceLocation, byte[]> data = new HashMap<>();
        Map<ResourceLocation, byte[]> assets = new HashMap<>();

        if (Cfg.INSTANCE.generateCmMaterials.get()) {
            int sort = SORT_BASE;
            List<Metals.Metal> generated = new ArrayList<>();
            for (Metals.Metal metal : Metals.ALL) {
                String name = metal.name();

                // the metal exists if its molten fluid does. create metallurgy has no ingot for void
                // steel or necromium, so an ingot tag check would drop both.
                if (!fluidExists(metal.fluid())) {
                    TinkersMetallurgy.LOGGER.debug("Skipping material {}; no {} fluid", name, metal.fluid());
                    continue;
                }
                generated.add(metal);

                put(data, "tinkering/materials/definition/" + name + ".json", definition(metal, sort++));
                put(data, "tinkering/materials/stats/" + name + ".json", stats(metal));
                put(data, "tinkering/materials/traits/" + name + ".json", traits(metal));
                put(assets, "tinkering/materials/" + name + ".json", renderInfo(metal));

                // without these the material is inert: listed at the tool station, but nothing can
                // cast, melt or repair it.
                put(data, "recipes/materials/" + name + "_casting.json", materialFluid(metal));
                put(data, "recipes/materials/" + name + "_melting.json", materialMelting(metal));
                for (Form form : FORMS) {
                    if (itemExists(name + form.itemSuffix())) {
                        put(data, "recipes/materials/" + name + form.itemSuffix() + ".json",
                                material(metal, form));
                    }
                }
            }
            if (!generated.isEmpty()) {
                put(assets, "lang/en_us.json", lang(generated));
                put(assets, "mantle/colors.json", colors(generated));
            }
            TinkersMetallurgy.LOGGER.info("Generated {} Create: Metallurgy tool materials", generated.size());
        }

        return new Generated(data, assets);
    }

    private static void put(Map<ResourceLocation, byte[]> out, String path, JsonObject json) {
        out.put(new ResourceLocation(NS, path), json.toString().getBytes(StandardCharsets.UTF_8));
    }

    private record Form(String itemSuffix, String tagFolder, int needed, int value) {}

    private static final Form[] FORMS = {
            new Form("_ingot", "ingots", 1, 1),
            new Form("_nugget", "nuggets", 9, 1),
            new Form("_block", "storage_blocks", 1, 9)};

    private static JsonObject definition(Metals.Metal metal, int sort) {
        JsonObject o = new JsonObject();
        o.addProperty("craftable", false);
        o.addProperty("hidden", false);
        o.addProperty("sortOrder", sort);
        o.addProperty("tier", metal.materialTier());
        return o;
    }

    private static JsonObject materialFluid(Metals.Metal metal) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "tconstruct:material_fluid");
        o.add("fluid", fluid(metal));
        o.addProperty("output", NS + ":" + metal.name());
        o.addProperty("temperature", metal.temperature());
        return o;
    }

    private static JsonObject materialMelting(Metals.Metal metal) {
        JsonObject o = new JsonObject();
        o.addProperty("type", "tconstruct:material_melting");
        o.addProperty("input", NS + ":" + metal.name());
        o.addProperty("temperature", metal.temperature());
        o.add("result", fluid(metal));
        return o;
    }

    private static JsonObject material(Metals.Metal metal, Form form) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", "forge:" + form.tagFolder() + "/" + metal.name());

        JsonObject o = new JsonObject();
        o.addProperty("type", "tconstruct:material");
        o.add("ingredient", ingredient);
        o.addProperty("material", NS + ":" + metal.name());
        o.addProperty("needed", form.needed());
        o.addProperty("value", form.value());
        return o;
    }

    private static JsonObject fluid(Metals.Metal metal) {
        JsonObject o = new JsonObject();
        o.addProperty("fluid", TinkersMetallurgy.CM + ":" + metal.fluid());
        o.addProperty("amount", 90);
        return o;
    }

    private static boolean fluidExists(String path) {
        return ForgeRegistries.FLUIDS.containsKey(new ResourceLocation(TinkersMetallurgy.CM, path));
    }

    private static boolean itemExists(String path) {
        return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(TinkersMetallurgy.CM, path));
    }

    private static JsonObject lang(List<Metals.Metal> metals) {
        JsonObject o = new JsonObject();
        for (Metals.Metal metal : metals) {
            o.addProperty("material." + NS + "." + metal.name(), displayName(metal.name()));
        }
        return o;
    }

    private static JsonObject colors(List<Metals.Metal> metals) {
        JsonObject materials = new JsonObject();
        for (Metals.Metal metal : metals) {
            materials.addProperty(metal.name(), String.format("#%06X", metal.colour() & 0xFFFFFF));
        }
        JsonObject o = new JsonObject();
        o.add("material." + NS, materials);
        return o;
    }

    private static String displayName(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (String word : name.split("_")) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word, 1, word.length());
        }
        return out.toString();
    }

    private static JsonObject stats(Metals.Metal metal) {
        JsonObject head = new JsonObject();
        head.addProperty("durability", metal.durability());
        head.addProperty("melee_attack", metal.attack());
        head.addProperty("mining_speed", metal.miningSpeed());
        head.addProperty("mining_tier", Metals.tierId(metal.tier()));

        JsonObject handle = new JsonObject();
        handle.addProperty("durability", round2(Math.max(0.1f, Math.min(2.0f, metal.durability() / 800.0f))));
        handle.addProperty("melee_damage", 0.0f);
        handle.addProperty("melee_speed", 0.0f);
        handle.addProperty("mining_speed", 0.0f);

        JsonObject stats = new JsonObject();
        stats.add("tconstruct:head", head);
        stats.add("tconstruct:handle", handle);
        stats.add("tconstruct:binding", new JsonObject());

        JsonObject o = new JsonObject();
        o.add("stats", stats);
        return o;
    }

    private static JsonObject traits(Metals.Metal metal) {
        JsonArray defaults = new JsonArray();
        for (Metals.Trait trait : metal.traits()) {
            JsonObject t = new JsonObject();
            t.addProperty("name", trait.name());
            t.addProperty("level", trait.level());
            defaults.add(t);
        }
        JsonObject o = new JsonObject();
        o.add("default", defaults);
        return o;
    }

    private static JsonObject renderInfo(Metals.Metal metal) {
        JsonArray fallbacks = new JsonArray();
        fallbacks.add("metal");

        JsonObject o = new JsonObject();
        o.addProperty("color", String.format("FF%06X", metal.colour() & 0xFFFFFF));
        o.add("fallbacks", fallbacks);
        return o;
    }

    private static float round2(float v) {
        return Math.round(v * 100f) / 100f;
    }
}

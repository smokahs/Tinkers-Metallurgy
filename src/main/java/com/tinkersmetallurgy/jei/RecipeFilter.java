package com.tinkersmetallurgy.jei;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;
import com.tinkersmetallurgy.config.Cfg.Categories;

// every create addon tab gathers its recipes through one create method, so filtering that consumer
// covers all the tabs before anything registers. done at the source rather than through hideRecipes
// because jei is not always the viewer behind the plugin, and emi's tab lookup answers nothing.
public final class RecipeFilter {

    private static final AtomicInteger FILTERED = new AtomicInteger();

    private RecipeFilter() {}

    public static boolean hidesConverted() {
        return Cfg.INSTANCE.jeiCategories.get() == Categories.TINKERS;
    }

    // kept even under TINKERS: both carry a crucible heat, and mob boiling a damage per hit, that the
    // tinkers display has no slot for. it would look right and say the wrong thing.
    public static final Set<String> KEPT_BY_CM = Set.of("alloying", "entity_melting");

    public static Consumer<Recipe<?>> wrap(Consumer<Recipe<?>> consumer) {
        if (!hidesConverted()) {
            return consumer;
        }
        return recipe -> {
            if (TinkersMetallurgy.MOD_ID.equals(recipe.getId().getNamespace()) && !keptByCm(recipe.getType())) {
                FILTERED.incrementAndGet();
            } else {
                consumer.accept(recipe);
            }
        };
    }

    // matched by registry name instead of class, so this stays loadable without create metallurgy.
    private static boolean keptByCm(RecipeType<?> type) {
        ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        return id != null && TinkersMetallurgy.CM.equals(id.getNamespace()) && KEPT_BY_CM.contains(id.getPath());
    }

    public static int filtered() {
        return FILTERED.get();
    }
}

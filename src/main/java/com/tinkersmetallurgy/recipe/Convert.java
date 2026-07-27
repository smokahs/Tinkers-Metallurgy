package com.tinkersmetallurgy.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import fr.lucreeper74.createmetallurgy.registries.CMRecipeTypes;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;

// runs once per datapack reload, converting each tinkers smeltery recipe onto its create metallurgy
// match. done at reload rather than shipped as a datapack so material driven recipes can expand
// against the filled material registry, and so other addons' smeltery recipes are picked up too.
public final class Convert {

    private Convert() {}

    public static void inject(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
                              Map<ResourceLocation, Recipe<?>> byName) {
        if (!TinkersMetallurgy.cmLoaded()) {
            return;
        }
        Fluids.invalidate();

        Sink sink = new Sink(byType, byName);
        long start = System.currentTimeMillis();

        // fold the create metallurgy fluids away first, so converted and existing recipes agree on
        // which molten iron is the real one.
        FluidRewrite.run(byType);

        if (Cfg.INSTANCE.melting.get()) {
            meltings(byType, sink);
        }
        int melted = sink.mark();
        if (Cfg.INSTANCE.alloying.get()) {
            alloys(byType, sink);
        }
        int alloyed = sink.mark();
        if (Cfg.INSTANCE.entityMelting.get()) {
            entityMeltings(byType, sink);
        }
        int boiled = sink.mark();
        if (Cfg.INSTANCE.casting.get()) {
            castings(byType, sink);
        }
        int cast = sink.mark();

        // last, so the converted casting recipes are already in place to be compared against.
        Molds.run(byType, byName, registryAccess());

        TinkersMetallurgy.LOGGER.info(
                "Converted {} Tinkers' smeltery recipes onto Create: Metallurgy in {} ms"
                        + " ({} melting, {} alloying, {} mob boiling, {} casting, {} skipped)",
                sink.added, System.currentTimeMillis() - start, melted, alloyed, boiled, cast, sink.skipped);
    }

    private static void meltings(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType, Sink sink) {
        RecipeType<?> type = TinkerRecipeTypes.MELTING.get();
        RegistryAccess registries = registryAccess();
        for (Recipe<?> recipe : snapshot(byType, type)) {
            if (recipe instanceof IMultiRecipe<?> multi) {
                if (registries == null) {
                    sink.skipped++;
                    continue;
                }
                for (Object expanded : multi.getRecipes(registries)) {
                    if (expanded instanceof MeltingRecipe melting) {
                        sink.accept(Melting.convert(melting), CMRecipeTypes.MELTING.getType());
                    }
                }
            } else if (recipe instanceof MeltingRecipe melting) {
                sink.accept(Melting.convert(melting), CMRecipeTypes.MELTING.getType());
            } else {
                sink.skipped++;
            }
        }
    }

    private static void alloys(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType, Sink sink) {
        RecipeType<?> type = TinkerRecipeTypes.ALLOYING.get();
        for (Recipe<?> recipe : snapshot(byType, type)) {
            if (recipe instanceof AlloyRecipe alloy) {
                sink.accept(Alloying.convert(alloy), CMRecipeTypes.ALLOYING.getType());
            } else {
                sink.skipped++;
            }
        }
    }

    private static void entityMeltings(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType, Sink sink) {
        RecipeType<?> type = TinkerRecipeTypes.ENTITY_MELTING.get();
        for (Recipe<?> recipe : snapshot(byType, type)) {
            if (recipe instanceof EntityMeltingRecipe melting) {
                for (Recipe<?> converted : MobMelting.convert(melting)) {
                    sink.accept(converted, CMRecipeTypes.ENTITY_MELTING.getType());
                }
            } else {
                sink.skipped++;
            }
        }
    }

    private static void castings(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType, Sink sink) {
        RegistryAccess registries = registryAccess();
        if (registries == null) {
            return;
        }
        casting(byType, sink, TinkerRecipeTypes.CASTING_TABLE.get(), Casting.Kind.TABLE,
                CMRecipeTypes.CASTING_IN_TABLE.getType());
        casting(byType, sink, TinkerRecipeTypes.CASTING_BASIN.get(), Casting.Kind.BASIN,
                CMRecipeTypes.CASTING_IN_BASIN.getType());
    }

    private static void casting(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType, Sink sink,
                                RecipeType<?> source, Casting.Kind kind, RecipeType<?> target) {
        RegistryAccess registries = registryAccess();
        for (Recipe<?> recipe : snapshot(byType, source)) {
            List<Recipe<?>> converted = Casting.convert(recipe, kind, registries);
            if (converted.isEmpty()) {
                TinkersMetallurgy.LOGGER.info("Could not convert casting recipe {} ({})",
                        recipe.getId(), recipe.getClass().getSimpleName());
                sink.skipped++;
                continue;
            }
            for (Recipe<?> result : converted) {
                sink.accept(result, target);
            }
        }
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        return Reload.registryAccess();
    }

    private static List<Recipe<?>> snapshot(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
                                            RecipeType<?> type) {
        Map<ResourceLocation, Recipe<?>> map = byType.get(type);
        return map == null ? List.of() : new ArrayList<>(map.values());
    }

    private static final class Sink {

        private final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType;
        private final Map<ResourceLocation, Recipe<?>> byName;

        private int added;
        private int skipped;
        private int counted;

        Sink(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
             Map<ResourceLocation, Recipe<?>> byName) {
            this.byType = byType;
            this.byName = byName;
        }

        int mark() {
            int since = added - counted;
            counted = added;
            return since;
        }

        void accept(Recipe<?> recipe, RecipeType<?> type) {
            if (recipe == null) {
                skipped++;
                return;
            }
            Map<ResourceLocation, Recipe<?>> map = byType.computeIfAbsent(type, t -> new HashMap<>());
            if (map.putIfAbsent(recipe.getId(), recipe) == null) {
                byName.put(recipe.getId(), recipe);
                added++;
            } else {
                skipped++;
            }
        }
    }
}

package com.tinkersmetallurgy.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingRecipe;
import fr.lucreeper74.createmetallurgy.registries.CMRecipeTypes;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;
import com.tinkersmetallurgy.mixin.CastingRecipeAccessor;

// both mods solve casting the same way and disagree only on the mold, so the tinkers cast wins. the
// molds cannot simply be deleted: create metallurgy alone casts plates, rods and gears, and anything
// at all for obdurium, void steel, necromium and lithium. so the order is swap the mold for the
// cast, drop the recipes that now read the same as a converted tinkers one, then delete the recipes
// that made the molds. Hide then takes the items out of creative and the recipe viewer.
public final class Molds {

    private Molds() {}

    private static final Map<String, String> CASTS = Map.of(
            "graphite_ingot_mold", "ingot_cast",
            "graphite_nugget_mold", "nugget_cast",
            "graphite_plate_mold", "plate_cast",
            "graphite_rod_mold", "rod_cast",
            "graphite_gear_mold", "gear_cast");

    private static final String BLANK = "graphite_blank_mold";

    public static void run(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
                           Map<ResourceLocation, Recipe<?>> byName,
                           @Nullable RegistryAccess registries) {
        if (!Cfg.INSTANCE.replaceGraphiteMolds.get() || registries == null) {
            return;
        }

        Map<Item, Item> swaps = swaps();
        List<Map<ResourceLocation, Recipe<?>>> maps = new ArrayList<>();
        for (CMRecipeTypes type : List.of(CMRecipeTypes.CASTING_IN_TABLE, CMRecipeTypes.CASTING_IN_BASIN)) {
            Map<ResourceLocation, Recipe<?>> map = byType.get(type.<RecipeType<?>>getType());
            if (map != null) {
                maps.add(map);
            }
        }

        // what the tinkers side already casts, read after conversion so the converted recipes count.
        Set<String> covered = new HashSet<>();
        for (Map<ResourceLocation, Recipe<?>> map : maps) {
            for (Recipe<?> recipe : map.values()) {
                if (recipe instanceof CastingRecipe casting
                        && TinkersMetallurgy.MOD_ID.equals(recipe.getId().getNamespace())) {
                    covered.add(signature(casting, registries));
                }
            }
        }

        int swapped = 0;
        int dropped = 0;
        for (Map<ResourceLocation, Recipe<?>> map : maps) {
            var iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Recipe<?>> entry = iterator.next();
                if (!(entry.getValue() instanceof CastingRecipe casting)
                        || !TinkersMetallurgy.CM.equals(entry.getKey().getNamespace())) {
                    continue;
                }
                if (swap(casting, swaps)) {
                    swapped++;
                }
                if (covered.contains(signature(casting, registries))) {
                    iterator.remove();
                    byName.remove(entry.getKey());
                    dropped++;
                }
            }
        }

        Set<Item> molds = molds();
        int stranded = stranded(maps, molds);
        int removed = removeSources(byType, byName, registries, molds);

        TinkersMetallurgy.LOGGER.info(
                "Repointed {} Create: Metallurgy casting recipes at Tinkers' casts ({} dropped as duplicates);"
                        + " removed the graphite molds and the {} recipes that made them",
                swapped, dropped, removed);
        if (stranded > 0) {
            TinkersMetallurgy.LOGGER.warn(
                    "{} casting recipes still ask for a graphite mold and can no longer be run", stranded);
        }
    }

    private static int stranded(List<Map<ResourceLocation, Recipe<?>>> maps, Set<Item> molds) {
        int stranded = 0;
        for (Map<ResourceLocation, Recipe<?>> map : maps) {
            for (Map.Entry<ResourceLocation, Recipe<?>> entry : map.entrySet()) {
                if (!(entry.getValue() instanceof CastingRecipe casting)) {
                    continue;
                }
                for (ItemStack stack : casting.getIngredient().getItems()) {
                    if (molds.contains(stack.getItem())) {
                        TinkersMetallurgy.LOGGER.warn("Casting recipe {} still wants {}", entry.getKey(),
                                ForgeRegistries.ITEMS.getKey(stack.getItem()));
                        stranded++;
                        break;
                    }
                }
            }
        }
        return stranded;
    }

    private static int removeSources(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
                                     Map<ResourceLocation, Recipe<?>> byName,
                                     RegistryAccess registries, Set<Item> molds) {
        int removed = 0;
        for (Map<ResourceLocation, Recipe<?>> map : byType.values()) {
            var iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<ResourceLocation, Recipe<?>> entry = iterator.next();
                Item result = result(entry.getValue(), registries);
                if (result != null && molds.contains(result)) {
                    iterator.remove();
                    byName.remove(entry.getKey());
                    removed++;
                }
            }
        }
        return removed;
    }

    @Nullable
    private static Item result(Recipe<?> recipe, RegistryAccess registries) {
        try {
            ItemStack stack = recipe.getResultItem(registries);
            return stack.isEmpty() ? null : stack.getItem();
        } catch (Throwable notAvailable) {
            return null;
        }
    }

    private static boolean swap(CastingRecipe recipe, Map<Item, Item> swaps) {
        ItemStack[] molds = recipe.getIngredient().getItems();
        if (molds.length != 1) {
            return false;
        }
        Item cast = swaps.get(molds[0].getItem());
        if (cast == null) {
            return false;
        }
        ((CastingRecipeAccessor) recipe).tinkersmetallurgy$setIngredient(Ingredient.of(cast));
        return true;
    }

    // what a casting recipe amounts to: cast, fluid, output. two recipes agreeing on all three are
    // the same recipe however they were written.
    private static String signature(CastingRecipe recipe, RegistryAccess registries) {
        ItemStack[] casts = recipe.getIngredient().getItems();
        String cast = casts.length == 0 ? "" : String.valueOf(ForgeRegistries.ITEMS.getKey(casts[0].getItem()));

        List<FluidStack> fluids = recipe.getFluidIngredient().getMatchingFluidStacks();
        String fluid = fluids.isEmpty() ? ""
                : ForgeRegistries.FLUIDS.getKey(fluids.get(0).getFluid()) + "x" + fluids.get(0).getAmount();

        ItemStack output = recipe.getResultItem(registries);
        return cast + "|" + fluid + "|" + ForgeRegistries.ITEMS.getKey(output.getItem()) + "x" + output.getCount();
    }

    private static Map<Item, Item> swaps() {
        Map<Item, Item> swaps = new HashMap<>();
        CASTS.forEach((mold, cast) -> {
            Item from = item(TinkersMetallurgy.CM, mold);
            Item to = item(TinkersMetallurgy.TIC, cast);
            if (from != null && to != null) {
                swaps.put(from, to);
            }
        });
        return swaps;
    }

    public static Set<Item> molds() {
        Set<Item> molds = new HashSet<>();
        for (String mold : CASTS.keySet()) {
            Item item = item(TinkersMetallurgy.CM, mold);
            if (item != null) {
                molds.add(item);
            }
        }
        Item blank = item(TinkersMetallurgy.CM, BLANK);
        if (blank != null) {
            molds.add(blank);
        }
        return molds;
    }

    @Nullable
    private static Item item(String namespace, String path) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, path));
        return item == null || item == Items.AIR ? null : item;
    }
}

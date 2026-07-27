package com.tinkersmetallurgy.jei;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import com.tinkersmetallurgy.dedupe.Hide;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IIngredientManager;

// catalysts register separately from the creative menu, so a hidden smeltery block would still
// advertise itself under a tab the player can no longer build for. wrapping the registration
// tinkers is handed catches every path, including its private casting catalyst helper.
public final class CatalystFilter implements IRecipeCatalystRegistration {

    private final IRecipeCatalystRegistration delegate;

    private CatalystFilter(IRecipeCatalystRegistration delegate) {
        this.delegate = delegate;
    }

    public static IRecipeCatalystRegistration wrap(IRecipeCatalystRegistration delegate) {
        return Hide.hidesSmelteryBlocks() ? new CatalystFilter(delegate) : delegate;
    }

    private static boolean blocked(Object ingredient) {
        return ingredient instanceof ItemStack stack && Hide.isHiddenSmelteryBlock(stack.getItem());
    }

    @Override
    public void addRecipeCatalysts(RecipeType<?> recipeType, ItemLike... items) {
        ItemLike[] kept = java.util.Arrays.stream(items)
                .filter(item -> !Hide.isHiddenSmelteryBlock(item.asItem()))
                .toArray(ItemLike[]::new);
        if (kept.length > 0) {
            delegate.addRecipeCatalysts(recipeType, kept);
        }
    }

    @Override
    public <T> void addRecipeCatalysts(RecipeType<?> recipeType, IIngredientType<T> ingredientType,
                                       List<T> ingredients) {
        List<T> kept = ingredients.stream().filter(ingredient -> !blocked(ingredient)).toList();
        if (!kept.isEmpty()) {
            delegate.addRecipeCatalysts(recipeType, ingredientType, kept);
        }
    }

    @Override
    public <T> void addRecipeCatalyst(IIngredientType<T> ingredientType, T ingredient,
                                      RecipeType<?>... recipeTypes) {
        if (!blocked(ingredient)) {
            delegate.addRecipeCatalyst(ingredientType, ingredient, recipeTypes);
        }
    }

    @Override
    public IIngredientManager getIngredientManager() {
        return delegate.getIngredientManager();
    }

    @Override
    public IJeiHelpers getJeiHelpers() {
        return delegate.getJeiHelpers();
    }
}

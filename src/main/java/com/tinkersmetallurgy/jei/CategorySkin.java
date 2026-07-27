package com.tinkersmetallurgy.jei;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

// a tinkers tab with only its icon swapped, keeping the compact cycling layout. titles stay as they
// are: tinkers names them after the process, not the block, so they read right against these
// machines. FoundrySkin builds on this to redraw one of them.
class CategorySkin<T> implements IRecipeCategory<T> {

    private final IRecipeCategory<T> delegate;
    private final IDrawable icon;

    CategorySkin(IRecipeCategory<T> delegate, IDrawable icon) {
        this.delegate = delegate;
        this.icon = icon;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return delegate.getRecipeType();
    }

    @Override
    public Component getTitle() {
        return delegate.getTitle();
    }

    @Override
    public IDrawable getBackground() {
        return delegate.getBackground();
    }

    @Override
    public int getWidth() {
        return delegate.getWidth();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        delegate.setRecipe(builder, recipe, focuses);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        delegate.createRecipeExtras(builder, recipe, focuses);
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        delegate.draw(recipe, slots, graphics, mouseX, mouseY);
    }

    @Override
    public void onDisplayedIngredientsUpdate(T recipe, List<IRecipeSlotDrawable> slots, IFocusGroup focuses) {
        delegate.onDisplayedIngredientsUpdate(recipe, slots, focuses);
    }

    @Override
    public List<Component> getTooltipStrings(T recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        return delegate.getTooltipStrings(recipe, slots, mouseX, mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView slots,
                           double mouseX, double mouseY) {
        delegate.getTooltip(tooltip, recipe, slots, mouseX, mouseY);
    }

    @Override
    public boolean handleInput(T recipe, double mouseX, double mouseY, InputConstants.Key input) {
        return delegate.handleInput(recipe, mouseX, mouseY, input);
    }

    @Override
    public boolean isHandled(T recipe) {
        return delegate.isHandled(recipe);
    }

    @Override
    public ResourceLocation getRegistryName(T recipe) {
        return delegate.getRegistryName(recipe);
    }
}

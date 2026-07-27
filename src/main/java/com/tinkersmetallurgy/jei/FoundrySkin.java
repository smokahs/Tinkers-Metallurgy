package com.tinkersmetallurgy.jei;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.plugin.jei.AlloyRecipeCategory;
import slimeknights.tconstruct.plugin.jei.melting.AbstractMeltingCategory;

import com.simibubi.create.content.processing.recipe.HeatCondition;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;
import com.tinkersmetallurgy.recipe.Heat;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

final class FoundrySkin extends CategorySkin<MeltingRecipe> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TinkersMetallurgy.TIC, "textures/gui/jei/melting.png");

    private static final int SHIFT = 20;
    private static final int WIDTH = 132 - SHIFT;
    private static final int HEIGHT = 40;

    private static final String HEAT_PREFIX = "create.";

    private final IGuiHelper gui;
    private final IDrawable background;
    private final IDrawableStatic plus;
    private final Map<Integer, IDrawableAnimated> arrows = new HashMap<>();

    FoundrySkin(IRecipeCategory<MeltingRecipe> delegate, IDrawable icon, IGuiHelper gui) {
        super(delegate, icon);
        this.gui = gui;
        this.background = gui.createDrawable(TEXTURE, SHIFT, 0, WIDTH, HEIGHT);
        this.plus = gui.createDrawable(TEXTURE, 132, 34, 6, 6);
    }

    static boolean enabled() {
        return RecipeFilter.hidesConverted()
                && Cfg.INSTANCE.melting.get()
                && Cfg.INSTANCE.jeiCreateHeat.get();
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MeltingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 24 - SHIFT, 18).addIngredients(recipe.getInput());
        AlloyRecipeCategory.drawVariableFluids(builder, RecipeIngredientRole.OUTPUT, 96 - SHIFT, 4, 32, 32,
                recipe.getOutputWithByproducts(), FluidValues.METAL_BLOCK, Function.identity(),
                fluids -> AbstractMeltingCategory.MeltingFluidCallback.INSTANCE);
    }

    @Override
    public void draw(MeltingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow(recipe.getTime() * 5).draw(graphics, 56 - SHIFT, 18);
        if (recipe.getOreType() != null) {
            plus.draw(graphics, 87 - SHIFT, 31);
        }
        HeatCondition heat = Heat.condition(recipe.getTemperature());
        if (heat == HeatCondition.NONE) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        Component label = Component.translatable(HEAT_PREFIX + heat.getTranslationKey());
        graphics.drawString(font, label, 56 - SHIFT - font.width(label) / 2, 3, heat.getColor(), false);
    }

    @Override
    public List<Component> getTooltipStrings(MeltingRecipe recipe, IRecipeSlotsView slots,
                                             double mouseX, double mouseY) {
        return super.getTooltipStrings(recipe, slots, mouseX + SHIFT, mouseY);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, MeltingRecipe recipe,
                           IRecipeSlotsView slots, double mouseX, double mouseY) {
        super.getTooltip(tooltip, recipe, slots, mouseX + SHIFT, mouseY);
    }

    @Override
    public boolean handleInput(MeltingRecipe recipe, double mouseX, double mouseY,
                               InputConstants.Key input) {
        return super.handleInput(recipe, mouseX + SHIFT, mouseY, input);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MeltingRecipe recipe, IFocusGroup focuses) {
    }

    private IDrawableAnimated arrow(int ticks) {
        return arrows.computeIfAbsent(ticks, t ->
                gui.drawableBuilder(TEXTURE, 150, 41, 24, 17).buildAnimated(t, StartDirection.LEFT, false));
    }
}

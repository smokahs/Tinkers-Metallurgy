package com.tinkersmetallurgy.jei;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.registries.ForgeRegistries;

import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.dedupe.Hide;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;

// each tinkers smeltery tab pictures a block we hide, so wrapping the registration swaps in the
// create metallurgy machine that really runs it. usually only the icon changes (CategorySkin); the
// foundry tab is redrawn as well (FoundrySkin).
public final class CategoryRegistration implements IRecipeCategoryRegistration {

    // the one tab whose contents are redrawn, not just re-iconed.
    private static final String FOUNDRY = "foundry";

    // tinkers tab path to the create metallurgy block standing in for it.
    private static final Map<String, String> STAND_INS = Map.of(
            "casting_table", "casting_table",
            "casting_basin", "casting_basin",
            "molding", "casting_table",
            "melting", "foundry_lid",
            "foundry", "foundry_lid",
            "alloy", "foundry_mixer",
            "entity_melting", "industrial_crucible");

    private final IRecipeCategoryRegistration delegate;

    private CategoryRegistration(IRecipeCategoryRegistration delegate) {
        this.delegate = delegate;
    }

    public static IRecipeCategoryRegistration wrap(IRecipeCategoryRegistration delegate) {
        // if the tinkers blocks stay visible their own icons are fine, and without the foundry redraw
        // there is nothing else left to change.
        return Hide.hidesSmelteryBlocks() || FoundrySkin.enabled()
                ? new CategoryRegistration(delegate)
                : delegate;
    }

    @Override
    public void addRecipeCategories(IRecipeCategory<?>... categories) {
        delegate.addRecipeCategories(Arrays.stream(categories)
                .map(this::skin)
                .toArray(IRecipeCategory<?>[]::new));
    }

    @Override
    public IJeiHelpers getJeiHelpers() {
        return delegate.getJeiHelpers();
    }

    private IRecipeCategory<?> skin(IRecipeCategory<?> category) {
        ResourceLocation uid = category.getRecipeType().getUid();
        if (!TinkersMetallurgy.TIC.equals(uid.getNamespace())) {
            return category;
        }
        IGuiHelper gui = delegate.getJeiHelpers().getGuiHelper();
        IDrawable standIn = standIn(uid, gui);
        if (FOUNDRY.equals(uid.getPath()) && FoundrySkin.enabled()) {
            return foundry(category, standIn == null ? category.getIcon() : standIn, gui);
        }
        return standIn == null ? category : reIcon(category, standIn);
    }

    private static <T> IRecipeCategory<T> reIcon(IRecipeCategory<T> category, IDrawable icon) {
        return new CategorySkin<>(category, icon);
    }

    // the foundry tab is the only one whose contents we redraw and tinkers types it against melting
    // recipes, so the uid it was matched on is what makes the cast safe.
    @SuppressWarnings("unchecked")
    private static IRecipeCategory<?> foundry(IRecipeCategory<?> category, IDrawable icon, IGuiHelper gui) {
        return new FoundrySkin((IRecipeCategory<MeltingRecipe>) category, icon, gui);
    }

    @Nullable
    private IDrawable standIn(ResourceLocation uid, IGuiHelper gui) {
        if (!Hide.hidesSmelteryBlocks()) {
            return null;
        }
        Block block = block(STAND_INS.get(uid.getPath()));
        return block == null ? null : gui.createDrawableItemStack(new ItemStack(block));
    }

    @Nullable
    private static Block block(@Nullable String name) {
        if (name == null) {
            return null;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TinkersMetallurgy.CM, name));
        return block == null || block == Blocks.AIR ? null : block;
    }
}

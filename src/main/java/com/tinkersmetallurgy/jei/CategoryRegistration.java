package com.tinkersmetallurgy.jei;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.registries.ForgeRegistries;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.dedupe.Hide;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;

// each tinkers smeltery tab pictures a block we hide, so wrapping the registration swaps in the
// create metallurgy machine that really runs it. only the icon changes; see CategorySkin.
public final class CategoryRegistration implements IRecipeCategoryRegistration {

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
        // if the tinkers blocks stay visible their own icons are fine, so do nothing.
        return Hide.hidesSmelteryBlocks() ? new CategoryRegistration(delegate) : delegate;
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

    private <T> IRecipeCategory<T> skin(IRecipeCategory<T> category) {
        ResourceLocation uid = category.getRecipeType().getUid();
        if (!TinkersMetallurgy.TIC.equals(uid.getNamespace())) {
            return category;
        }
        Block standIn = block(STAND_INS.get(uid.getPath()));
        if (standIn == null) {
            return category;
        }
        IGuiHelper gui = delegate.getJeiHelpers().getGuiHelper();
        return new CategorySkin<>(category, gui.createDrawableItemStack(new ItemStack(standIn)));
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

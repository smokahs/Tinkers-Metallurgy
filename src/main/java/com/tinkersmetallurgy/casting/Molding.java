package com.tinkersmetallurgy.casting;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.recipe.molding.IMoldingContainer;

// a create metallurgy casting table read as a tinkers molding container: mold slot is the material,
// the player's hand is the pattern.
public final class Molding implements IMoldingContainer {

    private final ItemStack material;
    private final ItemStack pattern;

    public Molding(ItemStack material, ItemStack pattern) {
        this.material = material;
        this.pattern = pattern;
    }

    @Override
    public ItemStack getMaterial() {
        return material;
    }

    @Override
    public ItemStack getPattern() {
        return pattern;
    }
}

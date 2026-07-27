package com.tinkersmetallurgy.melting;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer;

// wraps one item stack so tinkers melting recipes can work out their output.
public final class Container implements IMeltingContainer {

    private final ItemStack stack;

    public Container(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public IOreRate getOreRate() {
        return Config.COMMON.foundryOreRate;
    }
}

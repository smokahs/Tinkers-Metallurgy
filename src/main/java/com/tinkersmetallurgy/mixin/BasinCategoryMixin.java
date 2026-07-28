package com.tinkersmetallurgy.mixin;

import net.minecraft.world.item.ItemStack;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import com.tinkersmetallurgy.burner.Lowheat;
import com.tinkersmetallurgy.burner.Setup;

import com.tterrag.registrate.util.entry.BlockEntry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// a low-heated recipe already draws its heat bar and skips the blaze cake on its own, since both follow
// from testBlazeBurner. what it cannot get right is the burner shown beside them, which create takes
// straight from AllBlocks.BLAZE_BURNER. that is the only BlockEntry.asStack() call in the method, so
// the swap needs nothing else pinned down.
@Mixin(value = BasinCategory.class, remap = false)
public class BasinCategoryMixin {

    @Redirect(method = "setRecipe",
            at = @At(value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/BlockEntry;asStack()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack tinkersmetallurgy$heatSourceIcon(BlockEntry<?> blazeBurner, IRecipeLayoutBuilder builder,
                                                       BasinRecipe recipe, IFocusGroup focuses) {
        if (recipe.getRequiredHeat() == Lowheat.CONDITION) {
            return Setup.BURNER.asStack();
        }
        return blazeBurner.asStack();
    }
}

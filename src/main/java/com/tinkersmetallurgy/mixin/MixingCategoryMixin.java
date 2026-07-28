package com.tinkersmetallurgy.mixin;

import net.minecraft.client.gui.GuiGraphics;

import com.simibubi.create.compat.jei.category.MixingCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import com.tinkersmetallurgy.burner.Lowheat;
import com.tinkersmetallurgy.jei.LowBurner;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// the animated scene above the recipe. create hands the heat condition to AnimatedBlazeBurner, which
// draws a blaze whatever level it is given, so the low-heated tier would advertise the burner it exists
// to avoid. injecting on the heat lookup lands after the super call has drawn the bar and shadow, which
// are already right; what is left is the burner and the mixer. ported from create low-heated (MIT).
@Mixin(value = MixingCategory.class, remap = false)
public abstract class MixingCategoryMixin {

    @Inject(method = "draw",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"),
            cancellable = true)
    private void tinkersmetallurgy$drawLowHeated(BasinRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                                                 double mouseX, double mouseY, CallbackInfo ci) {
        if (recipe.getRequiredHeat() != Lowheat.CONDITION) {
            return;
        }

        int x = ((MixingCategory) (Object) this).getBackground().getWidth() / 2 + 3;
        LowBurner.drawBurner(graphics, x, 55);
        LowBurner.drawMixer(graphics, x, 34);
        ci.cancel();
    }
}

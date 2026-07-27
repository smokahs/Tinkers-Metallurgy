package com.tinkersmetallurgy.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.CastingBlockEntity;
import fr.lucreeper74.createmetallurgy.content.blocks.casting.CastingBlockRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// a tinkers cast is a flat sprite, so it lands on the same plane as the part emerging from it and
// the surface tears into stripes. lifting the mold a hair apart settles the depth test, and keeps
// the cast on top of the table rather than half swallowed by it.
@Mixin(CastingBlockRenderer.class)
public abstract class CastingRendererMixin {

    // block units, too small to see at any normal distance.
    private static final float MOLD_LIFT = 0.01F;

    // renderSafe and renderItem belong to create and create metallurgy, so there is no obfuscation
    // mapping to look them up in.
    @Shadow(remap = false)
    protected abstract void renderItem(CastingBlockEntity blockEntity, PoseStack pose,
                                       MultiBufferSource buffer, int light, int overlay, ItemStack stack);

    @Redirect(method = "renderSafe(Lfr/lucreeper74/createmetallurgy/content/blocks/casting/CastingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lfr/lucreeper74/createmetallurgy/content/blocks/casting/CastingBlockRenderer;renderItem(Lfr/lucreeper74/createmetallurgy/content/blocks/casting/CastingBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 2,
                    remap = false),
            remap = false)
    private void tinkersmetallurgy$liftMold(CastingBlockRenderer self, CastingBlockEntity blockEntity,
                                            PoseStack pose, MultiBufferSource buffer, int light,
                                            int overlay, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        pose.pushPose();
        pose.translate(0, MOLD_LIFT, 0);
        renderItem(blockEntity, pose, buffer, light, overlay, stack);
        pose.popPose();
    }
}

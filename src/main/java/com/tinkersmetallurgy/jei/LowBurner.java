package com.tinkersmetallurgy.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import com.tinkersmetallurgy.burner.Setup;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;

// create's AnimatedBlazeBurner drawn with a basic burner in place of the blaze, for the low-heated tier.
// same framing and scale, so a recipe that moves between tiers does not jump around in the viewer.
// ported from create low-heated (MIT).
public final class LowBurner {

    private LowBurner() {}

    // one shared instance: an animated mixer only reads the render clock, it holds no per recipe state.
    private static final AnimatedMixer MIXER = new AnimatedMixer();

    public static void drawMixer(GuiGraphics graphics, int xOffset, int yOffset) {
        MIXER.draw(graphics, xOffset, yOffset);
    }

    public static void drawBurner(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        AnimatedKinetics.defaultBlockElement(Setup.BURNER.getDefaultState())
                .atLocal(0, 1.65, 0)
                .scale(scale)
                .render(graphics);

        AnimatedKinetics.defaultBlockElement(Setup.BURNER.getDefaultState())
                .atLocal(1, 1.8, 1)
                .rotate(0, 180, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.scale(scale, -scale, scale);
        matrixStack.translate(0, -1.8, 0);

        SpriteShiftEntry spriteShift = AllSpriteShifts.BURNER_FLAME;
        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();
        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

        float time = AnimationTickHolder.getRenderTime(Minecraft.getInstance().level);

        // create scales the scroll by the heat level's ordinal. LOW was appended to the end of the enum
        // and would come out the fastest flame of the lot, so the rung below a lit blaze is used here.
        float speed = 1 / 32f + 1 / 64f * HeatLevel.SMOULDERING.ordinal();

        double vScroll = speed * time;
        vScroll = (vScroll - Math.floor(vScroll)) * spriteHeight / 2;

        double uScroll = speed * time / 2;
        uScroll = (uScroll - Math.floor(uScroll)) * spriteWidth / 2;

        CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, Blocks.AIR.defaultBlockState())
                .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll)
                .light(LightTexture.FULL_BRIGHT)
                .renderInto(matrixStack, graphics.bufferSource().getBuffer(RenderType.cutoutMipped()));
        matrixStack.popPose();
    }
}

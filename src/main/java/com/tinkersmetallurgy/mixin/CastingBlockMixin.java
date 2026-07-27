package com.tinkersmetallurgy.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.CastingBlock;
import fr.lucreeper74.createmetallurgy.content.blocks.casting.CastingBlockEntity;
import fr.lucreeper74.createmetallurgy.content.blocks.casting.table.CastingTableBlock;

import com.tinkersmetallurgy.casting.Molding;
import com.tinkersmetallurgy.config.Cfg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// molding is the one smeltery step create metallurgy has no version of, and it is what makes casts,
// so without it the whole casting chain is out of reach. only takes over when the mold slot already
// holds something a molding recipe matches; otherwise the block's normal behaviour runs untouched.
@Mixin(CastingBlock.class)
public abstract class CastingBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void tinkersmetallurgy$mold(BlockState state, Level level, BlockPos pos, Player player,
                                        InteractionHand hand, BlockHitResult ray,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (!Cfg.INSTANCE.molding.get()) {
            return;
        }
        if (level.isClientSide) {
            return;
        }

        ItemStack pattern = player.getItemInHand(hand);
        if (pattern.isEmpty()) {
            return;
        }

        if (!(level.getBlockEntity(pos) instanceof CastingBlockEntity be)) {
            return;
        }
        // a table holding fluid is mid cast. leave it alone.
        if (!be.getFluidTank().getFluidInTank(0).isEmpty()) {
            return;
        }

        ItemStack material = be.moldInv.getStackInSlot(0);
        if (material.isEmpty()) {
            // nothing to press against, so let the block insert the mold normally.
            return;
        }

        RecipeType<MoldingRecipe> type = ((Object) this) instanceof CastingTableBlock
                ? TinkerRecipeTypes.MOLDING_TABLE.get()
                : TinkerRecipeTypes.MOLDING_BASIN.get();

        Molding container = new Molding(material, pattern);
        Optional<MoldingRecipe> match = level.getRecipeManager().getRecipeFor(type, container, level);
        if (match.isEmpty()) {
            return;
        }

        MoldingRecipe recipe = match.get();
        ItemStack result = recipe.assemble(container, level.registryAccess());
        if (result.isEmpty()) {
            return;
        }

        be.moldInv.setStackInSlot(0, result);
        if (recipe.isPatternConsumed()) {
            pattern.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        be.notifyUpdate();

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}

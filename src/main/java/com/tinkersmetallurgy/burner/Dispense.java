package com.tinkersmetallurgy.burner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tinkersmetallurgy.config.Cfg;

// lighting a burner from a dispenser. vanilla keys dispense behaviour by item and flint and steel
// already has one, so rather than replacing that, the dispenser is caught before it picks a slot and
// asked whether it is pointing at a burner waiting for a light.
public final class Dispense {

    private Dispense() {}

    /**
     * @return true when a burner was lit, so the dispenser should not also fire its normal behaviour
     */
    public static boolean lightBurner(ServerLevel level, BlockPos pos) {
        if (!Cfg.INSTANCE.dispenserBurner.get()) {
            return false;
        }

        BlockState dispenserState = level.getBlockState(pos);
        if (!dispenserState.hasProperty(DispenserBlock.FACING)) {
            return false;
        }

        BlockPos target = pos.relative(dispenserState.getValue(DispenserBlock.FACING));
        BlockState state = level.getBlockState(target);
        if (!(level.getBlockEntity(target) instanceof BurnerEntity burner)) {
            return false;
        }
        if (state.getValue(Burner.LIT) || burner.inputInv.getStackInSlot(0).isEmpty()) {
            return false;
        }
        if (!(level.getBlockEntity(pos) instanceof DispenserBlockEntity dispenser)) {
            return false;
        }

        for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
            ItemStack stack = dispenser.getItem(slot);
            if (stack.isEmpty() || !stack.is(Tags.BURNER_STARTERS)) {
                continue;
            }

            level.setBlockAndUpdate(target, state.setValue(Burner.LIT, true));
            burner.notifyUpdate();
            level.playSound(null, target, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                    level.random.nextFloat() * 0.4F + 0.8F);

            if (stack.isDamageableItem()) {
                if (stack.hurt(1, level.random, null)) {
                    stack.shrink(1);
                    stack.setDamageValue(0);
                }
            } else {
                stack.shrink(1);
            }
            dispenser.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }

        return false;
    }
}

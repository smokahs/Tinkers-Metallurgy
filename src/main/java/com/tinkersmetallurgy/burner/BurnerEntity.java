package com.tinkersmetallurgy.burner;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import com.tinkersmetallurgy.config.Cfg;

import net.createmod.catnip.math.VecHelper;

// the burner's fuel clock. one slot, one stack at a time, burning down by forge's burn times; the heat
// level it advertises lives in the block state so a basin can read it without a block entity lookup.
public class BurnerEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public static final int MAX_HEAT_CAPACITY = 4000;

    // below this much burn time left a fresh item tops the burner up instead of being refused.
    public static final int INSERTION_THRESHOLD = 400;

    protected FuelType activeFuel;
    protected int remainingBurnTime;

    // read once, at construction: a burner that is already burning keeps the terms it started under
    // until the chunk reloads.
    protected final int fanMultiplier;
    protected final int baseMultiplier;
    protected final boolean hotBurners;
    protected final HeatLevel activeHeatLevel;
    protected final HeatLevel empoweredHeatLevel;

    public final ItemStackHandler inputInv;
    public final LazyOptional<IItemHandler> capability;

    final BurnerItemHandler itemHandler;

    public BurnerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inputInv = new ItemStackHandler(1);
        itemHandler = new BurnerItemHandler();
        capability = LazyOptional.of(() -> itemHandler);
        activeFuel = FuelType.NONE;
        remainingBurnTime = 0;
        fanMultiplier = Cfg.INSTANCE.fanMultiplier.get();
        baseMultiplier = Cfg.INSTANCE.baseMultiplier.get();
        hotBurners = Cfg.INSTANCE.hotBurners.get();
        activeHeatLevel = hotBurners ? HeatLevel.KINDLED : Lowheat.LEVEL;
        empoweredHeatLevel = hotBurners ? HeatLevel.SEETHING : HeatLevel.KINDLED;
    }

    public FuelType getActiveFuel() {
        return activeFuel;
    }

    public int getRemainingBurnTime() {
        return remainingBurnTime;
    }

    // burn time spent per tick right now. remaining time is a fuel budget, not a clock, so this is
    // what turns one into the other.
    public int fuelDrainPerTick() {
        return empoweredFromBlock() ? fanMultiplier : baseMultiplier;
    }

    public void setEmpowered(boolean value) {
        if (empoweredFromBlock() == value) {
            return;
        }
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(Burner.EMPOWERED, value));
        notifyUpdate();
    }

    @Override
    public void tick() {
        super.tick();

        if (!litFromBlock()) {
            return;
        }

        if (level.isClientSide) {
            if (!isVirtual()) {
                spawnParticles(heatLevelFromBlock());
            }
            return;
        }

        tickFuel();

        if (remainingBurnTime > 0) {
            remainingBurnTime -= empoweredFromBlock() ? fanMultiplier : baseMultiplier;
        }
        if (remainingBurnTime < 0) {
            remainingBurnTime = 0;
        }
        if (activeFuel == FuelType.NORMAL) {
            updateBlockState();
        }
        if (remainingBurnTime > 0) {
            return;
        }

        activeFuel = FuelType.NONE;
        if (litFromBlock()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(Burner.LIT, false));
            notifyUpdate();
        }
        updateBlockState();
    }

    public void tickFuel() {
        ItemStack stackInSlot = inputInv.getStackInSlot(0);
        if (stackInSlot.isEmpty()) {
            return;
        }

        if (!tryUpdateFuel(stackInSlot, false, false)) {
            return;
        }

        if (stackInSlot.hasCraftingRemainingItem()) {
            inputInv.setStackInSlot(0, stackInSlot.getCraftingRemainingItem());
        } else {
            stackInSlot.shrink(1);
            inputInv.setStackInSlot(0, stackInSlot);
        }

        if (remainingBurnTime > 0 && !getBlockState().getValue(Burner.FUELED)) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(Burner.FUELED, true));
        }
        notifyUpdate();
    }

    /**
     * @return true if the burner took the item's burn time on, so the item should be consumed
     */
    protected boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
        if (!isFuelValid(itemStack)) {
            return false;
        }

        int newBurnTime = ForgeHooks.getBurnTime(itemStack, null);

        if (activeFuel == FuelType.NORMAL) {
            if (remainingBurnTime <= INSERTION_THRESHOLD) {
                newBurnTime += remainingBurnTime;
            } else if (forceOverflow && remainingBurnTime + newBurnTime < MAX_HEAT_CAPACITY) {
                newBurnTime = Math.min(remainingBurnTime + newBurnTime, MAX_HEAT_CAPACITY);
            } else {
                return false;
            }
        }

        if (simulate) {
            return true;
        }

        activeFuel = FuelType.NORMAL;
        remainingBurnTime = newBurnTime;

        HeatLevel previous = heatLevelFromBlock();
        playSound();
        updateBlockState();

        if (previous != heatLevelFromBlock()) {
            level.playSound(null, worldPosition, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    .125f + level.random.nextFloat() * .125f, 1.15f - level.random.nextFloat() * .25f);
        }

        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        capability.invalidate();
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(level, worldPosition, inputInv);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.put("InputInventory", inputInv.serializeNBT());
        compound.putInt("FuelLevel", activeFuel.ordinal());
        compound.putInt("BurnTimeRemaining", remainingBurnTime);
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        inputInv.deserializeNBT(compound.getCompound("InputInventory"));
        int fuel = compound.getInt("FuelLevel");
        activeFuel = fuel >= 0 && fuel < FuelType.values().length ? FuelType.values()[fuel] : FuelType.NONE;
        remainingBurnTime = compound.getInt("BurnTimeRemaining");
        super.read(compound, clientPacket);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (isItemHandlerCap(cap)) {
            return capability.cast();
        }
        return super.getCapability(cap, side);
    }

    public HeatLevel heatLevelFromBlock() {
        return Burner.heatLevelOf(getBlockState());
    }

    public boolean litFromBlock() {
        return getBlockState().getValue(Burner.LIT);
    }

    public boolean empoweredFromBlock() {
        return getBlockState().getValue(Burner.EMPOWERED);
    }

    public void updateBlockState() {
        setBlockHeat(heatLevel());
    }

    protected void setBlockHeat(HeatLevel heat) {
        if (heatLevelFromBlock() == heat) {
            return;
        }
        BlockState state = getBlockState().setValue(Burner.HEAT_LEVEL, heat);
        if (remainingBurnTime == 0) {
            state = state.setValue(Burner.LIT, false).setValue(Burner.FUELED, false);
        }
        level.setBlockAndUpdate(worldPosition, state);
        notifyUpdate();
    }

    protected void playSound() {
        level.playSound(null, worldPosition, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                .125f + level.random.nextFloat() * .125f, .75f - level.random.nextFloat() * .25f);
    }

    protected HeatLevel heatLevel() {
        if (!litFromBlock() || activeFuel != FuelType.NORMAL) {
            return HeatLevel.NONE;
        }
        return empoweredFromBlock() ? empoweredHeatLevel : activeHeatLevel;
    }

    protected void spawnParticles(HeatLevel heatLevel) {
        if (level == null || heatLevel == HeatLevel.NONE) {
            return;
        }

        RandomSource r = level.getRandom();
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        Vec3 smoke = center.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f).multiply(1, 0, 1));

        if (r.nextInt(4) != 0) {
            return;
        }

        boolean empty = level.getBlockState(worldPosition.above())
                .getCollisionShape(level, worldPosition.above())
                .isEmpty();

        if (empty || r.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.SMOKE, smoke.x, smoke.y, smoke.z, 0, 0, 0);
        }

        double yMotion = empty ? .0325f : r.nextDouble() * .0125f;
        Vec3 flame = center.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
                        .multiply(1, .25f, 1)
                        .normalize()
                        .scale((empty ? .25f : .5) + r.nextDouble() * .125f))
                .add(0, .5, 0);

        boolean empowered = empoweredFromBlock();
        double yExtra = empowered ? .02f : 0;
        level.addParticle(ParticleTypes.FLAME, flame.x, flame.y, flame.z, 0, yMotion + yExtra, 0);
        if (empowered) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, flame.x, flame.y, flame.z, 0, yMotion + yExtra, 0);
        }
    }

    public boolean isFuelValid(ItemStack stack) {
        if (ForgeHooks.getBurnTime(stack, null) <= 0) {
            return false;
        }
        if (stack.is(Tags.FUEL_BLACKLIST)) {
            return false;
        }
        return (Cfg.INSTANCE.ignoresFuelTagWhitelist.get() || stack.is(Tags.FUEL_WHITELIST))
                && inputInv.isItemValid(0, stack);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (remainingBurnTime > 0) {
            CreateLang.translate("recipe.heat_requirement." + heatRequirement())
                    .style(ChatFormatting.YELLOW)
                    .forGoggles(tooltip);
        }

        ItemStack stackInSlot = inputInv.getStackInSlot(0);
        if (stackInSlot.isEmpty()) {
            return false;
        }

        CreateLang.translate("addon.basicburner.burner_contents").forGoggles(tooltip);
        CreateLang.text("")
                .add(Component.translatable(stackInSlot.getDescriptionId()).withStyle(ChatFormatting.GRAY))
                .add(CreateLang.text(" x" + stackInSlot.getCount()).style(ChatFormatting.GREEN))
                .forGoggles(tooltip, 1);
        return true;
    }

    // which of create's heat requirement names this burner currently satisfies, for the goggle line.
    public String heatRequirement() {
        if (hotBurners) {
            return empoweredFromBlock() ? "superheated" : "heated";
        }
        return empoweredFromBlock() ? "heated" : "lowheated";
    }

    public enum FuelType {
        NONE,
        NORMAL
    }

    // the burner takes fuel from anything, but only one kind at a time and only while it is fuel: a
    // funnel pointed at it should not be able to stuff it with cobblestone.
    public class BurnerItemHandler implements IItemHandler {

        private static final int MAIN_SLOT = 0;

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inputInv.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inputInv.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isFuelValid(stack) && inputInv.isItemValid(slot, stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != MAIN_SLOT) {
                return stack;
            }
            ItemStack held = inputInv.getStackInSlot(0);
            if (!held.isEmpty() && !stack.is(held.getItem())) {
                return stack;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }

            ItemStack remainder = inputInv.insertItem(slot, stack, simulate);
            if (simulate || remainder == stack) {
                return remainder;
            }

            if (!inputInv.getStackInSlot(0).isEmpty() && !getBlockState().getValue(Burner.LIT)
                    && Cfg.INSTANCE.ignoresBurnerStarters.get()) {
                level.playSound(null, getBlockPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                        level.random.nextFloat() * 0.4F + 0.8F);
                if (!level.isClientSide) {
                    level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(Burner.LIT, true));
                }
            }
            notifyUpdate();
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack held = inputInv.getStackInSlot(0);
            if (held.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = held.copy();
            ItemStack extracted = stack.split(amount);
            if (!simulate) {
                inputInv.setStackInSlot(0, stack);
                notifyUpdate();
            }
            return extracted;
        }
    }
}

package com.tinkersmetallurgy.burner;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;

import com.tinkersmetallurgy.config.Cfg;

import net.createmod.catnip.data.Iterate;

// a blaze burner without the blaze: it holds one stack of solid fuel, has to be lit by hand, and while
// it burns it reads as the heat level below a blaze burner's. ported from create low-heated (MIT).
public class Burner extends HorizontalDirectionalBlock implements IBE<BurnerEntity>, IWrenchable {

    // the property covers the whole enum, LOW included, which is why it is declared here and not
    // borrowed from BlazeBurnerBlock: create's own property was built before the rung was added.
    public static final EnumProperty<HeatLevel> HEAT_LEVEL = EnumProperty.create("low", HeatLevel.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty FUELED = BooleanProperty.create("fueled");
    public static final BooleanProperty EMPOWERED = BooleanProperty.create("empowered");

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public Burner(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HEAT_LEVEL, HeatLevel.NONE)
                .setValue(LIT, false)
                .setValue(FUELED, false)
                .setValue(EMPOWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEAT_LEVEL, LIT, FUELED, EMPOWERED, FACING);
        super.createBlockStateDefinition(builder);
    }

    // a basin only rechecks its heat when something tells it to, and placing a burner underneath is
    // not something it watches for.
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        if (level.getBlockEntity(pos.above()) instanceof BasinBlockEntity basin) {
            basin.notifyChangeOfContents();
        }
    }

    @Override
    public Class<BurnerEntity> getBlockEntityClass() {
        return BurnerEntity.class;
    }

    @Override
    public BlockEntityType<? extends BurnerEntity> getBlockEntityType() {
        return Setup.BURNER_ENTITY.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        boolean wasEmptyHanded = heldItem.isEmpty() && hand == InteractionHand.MAIN_HAND;
        boolean shouldntPlaceItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem);

        if (!state.hasBlockEntity()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof BurnerEntity burner)) {
            return InteractionResult.PASS;
        }

        if (!burner.inputInv.getStackInSlot(0).isEmpty() && !state.getValue(LIT)
                && heldItem.is(Tags.BURNER_STARTERS)) {
            level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                    level.random.nextFloat() * 0.4F + 0.8F);
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.setBlockAndUpdate(pos, state.setValue(LIT, true));
            burner.notifyUpdate();
            return InteractionResult.SUCCESS;
        }

        ItemStack held = burner.inputInv.getStackInSlot(0);
        if (!held.isEmpty() && wasEmptyHanded) {
            player.getInventory().placeItemBackInInventory(held);
            burner.inputInv.setStackInSlot(0, ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
                    1f + level.random.nextFloat());
        }

        if (!wasEmptyHanded && !shouldntPlaceItem) {
            ItemStack remainder = burner.itemHandler.insertItem(0, heldItem.copy(), false);
            if (remainder.getCount() == heldItem.getCount()) {
                return InteractionResult.PASS;
            }
            player.setItemInHand(hand, remainder);
            AllSoundEvents.DEPOT_SLIDE.playOnServer(level, pos);
        }

        burner.notifyUpdate();
        return InteractionResult.SUCCESS;
    }

    // a fan already pointing at the spot should empower the burner the moment it lands, since the fan
    // has no speed change left to report.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(EMPOWERED, empoweredAt(context));
    }

    private static boolean empoweredAt(BlockPlaceContext context) {
        BlockPos burnerPos = context.getClickedPos();
        for (Direction side : Iterate.directions) {
            if (Cfg.INSTANCE.fanHorizontalOnly.get() && !side.getAxis().isHorizontal()) {
                continue;
            }

            BlockPos fanPos = burnerPos.relative(side);
            if (!(context.getLevel().getBlockEntity(fanPos) instanceof EncasedFanBlockEntity fan)) {
                continue;
            }
            if (!burnerPos.equals(fanPos.relative(fan.getBlockState().getValue(EncasedFanBlock.FACING)))) {
                continue;
            }
            if (Mth.abs(fan.getSpeed()) >= Cfg.INSTANCE.fanSpeedRequired.get()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // create low-heated read the heat level's ordinal here, which stopped meaning anything once LOW was
    // appended to the end of the enum and started outranking SEETHING. lit and empowered say the same
    // thing in order.
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(LIT)) {
            return 0;
        }
        return state.getValue(EMPOWERED) ? 2 : 1;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) != 0) {
            return;
        }
        if (state.getValue(HEAT_LEVEL) == HeatLevel.NONE) {
            return;
        }
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
    }

    public static HeatLevel heatLevelOf(BlockState state) {
        return state.hasProperty(HEAT_LEVEL) ? state.getValue(HEAT_LEVEL) : HeatLevel.NONE;
    }

    public static int light(BlockState state) {
        return state.getValue(HEAT_LEVEL) == HeatLevel.NONE ? 0 : 15;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    // fuel dropped onto a burner feeds it, the way a depot takes items thrown at it.
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        if (!(entity instanceof ItemEntity item) || !entity.isAlive() || entity.level().isClientSide) {
            return;
        }

        BurnerEntity burner = null;
        for (BlockPos pos : Iterate.hereAndBelow(entity.blockPosition())) {
            if (burner == null) {
                burner = getBlockEntity(level, pos);
            }
        }
        if (burner == null) {
            return;
        }

        IItemHandler handler = burner.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (handler == null) {
            return;
        }

        ItemStack remainder = handler.insertItem(0, item.getItem(), false);
        if (remainder.isEmpty()) {
            item.discard();
        } else if (remainder.getCount() < item.getItem().getCount()) {
            item.setItem(remainder);
        }
    }
}

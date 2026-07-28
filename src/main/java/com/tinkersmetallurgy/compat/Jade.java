package com.tinkersmetallurgy.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.burner.Burner;
import com.tinkersmetallurgy.burner.BurnerEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

// how long a burner has left, in the jade tooltip. jade is optional and never a dependency: nothing
// here is referenced from anywhere else, so this class is only ever loaded by jade's own scan for
// @WailaPlugin, which cannot happen without jade installed. ported from create low-heated (MIT).
@WailaPlugin
public class Jade implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final String HEAT_LEVEL = "HeatLevel";

    private static final String BURN_TICKS = "BurnTicks";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(this, BurnerEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(this, Burner.class);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof BurnerEntity burner)) {
            return;
        }
        if (burner.getRemainingBurnTime() <= 0) {
            return;
        }

        data.putString(HEAT_LEVEL, burner.heatRequirement());
        // the burn time left is fuel, and an empowered burner spends it faster.
        data.putInt(BURN_TICKS, burner.getRemainingBurnTime() / burner.fuelDrainPerTick());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(BURN_TICKS)) {
            return;
        }

        tooltip.add(Component.translatable("create.recipe.heat_requirement." + data.getString(HEAT_LEVEL)));
        tooltip.append(Component.literal(": "));
        tooltip.append(IThemeHelper.get().seconds(data.getInt(BURN_TICKS)));
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(TinkersMetallurgy.MOD_ID, "basic_burner_info");
    }
}

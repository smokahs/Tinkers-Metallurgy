package com.tinkersmetallurgy;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tinkersmetallurgy.burner.Setup;
import com.tinkersmetallurgy.config.Cfg;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(TinkersMetallurgy.MOD_ID)
public final class TinkersMetallurgy {

    public static final String MOD_ID = "tinkersmetallurgy";

    // we convert from tinkers (TIC) onto create metallurgy (CM).
    public static final String CM = "createmetallurgy";

    public static final String TIC = "tconstruct";

    // sister addon. when loaded, let it own duplicate materials.
    public static final String TIC3NH = "tic3nh";

    public static final Logger LOGGER = LogUtils.getLogger();

    public TinkersMetallurgy() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // config first: the burner's block entity reads it the moment one is placed.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Cfg.SPEC);
        Setup.register(modBus);
    }

    public static boolean nhLoaded() {
        return ModList.get().isLoaded(TIC3NH);
    }
}

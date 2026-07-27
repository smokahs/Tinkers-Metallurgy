package com.tinkersmetallurgy.material;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.tinkersmetallurgy.TinkersMetallurgy;

// registers the generated material pack, only when create metallurgy is installed.
@Mod.EventBusSubscriber(modid = TinkersMetallurgy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Setup {

    private static final String PACK_ID = "tinkersmetallurgy_materials";

    private Setup() {}

    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (!TinkersMetallurgy.cmLoaded()) {
            return;
        }
        Pack pack = Pack.readMetaAndCreate(
                PACK_ID,
                Component.literal("Tinkers' Metallurgy materials"),
                true,
                Datapack::new,
                event.getPackType(),
                Pack.Position.TOP,
                PackSource.BUILT_IN);
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }
}

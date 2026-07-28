package com.tinkersmetallurgy.burner;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import com.tinkersmetallurgy.TinkersMetallurgy;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.lang.FontHelper;

// the burner is the only thing this mod puts in a registry, so its registrate lives here rather than on
// the mod class. blockstates, models, loot and tags ship as files instead of datagen: nothing else here
// needs a data generator, and one block does not justify one.
public final class Setup {

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(TinkersMetallurgy.MOD_ID);

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    @SuppressWarnings("removal")
    public static final BlockEntry<Burner> BURNER = REGISTRATE
            .block("basic_burner", Burner::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .lightLevel(Burner::light))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
            .build()
            .register();

    public static final BlockEntityEntry<BurnerEntity> BURNER_ENTITY = REGISTRATE
            .blockEntity("basic_burner", BurnerEntity::new)
            .validBlocks(BURNER)
            .renderer(() -> Renderer::new)
            .register();

    private Setup() {}

    public static void register(IEventBus modBus) {
        REGISTRATE.registerEventListeners(modBus);
        modBus.addListener(Setup::registerArmPoint);
        modBus.addListener(Setup::buildCreativeTab);
    }

    private static void registerArmPoint(RegisterEvent event) {
        ArmPoint.init();
    }

    // the burner belongs next to the blaze burner it stands in for, and one block does not deserve a
    // creative tab of its own.
    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey()) {
            event.accept(BURNER.get());
        }
    }
}

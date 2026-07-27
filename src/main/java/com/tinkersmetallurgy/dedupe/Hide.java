package com.tinkersmetallurgy.dedupe;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;
import com.tinkersmetallurgy.recipe.Fluids;
import com.tinkersmetallurgy.recipe.Molds;

// hides what conversion made unused: create metallurgy duplicate molten buckets, the tinkers
// smeltery blocks, and the graphite molds.
@Mod.EventBusSubscriber(modid = TinkersMetallurgy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Hide {

    private Hide() {}

    // block side only. tool station, part builder, casts and parts stay.
    private static final Set<String> TIC_SMELTERY_BLOCKS = Set.of(
            "smeltery_controller", "foundry_controller",
            "seared_melter", "scorched_alloyer",
            "seared_table", "seared_basin", "seared_faucet", "seared_channel",
            "scorched_table", "scorched_basin", "scorched_faucet", "scorched_channel",
            "seared_heater", "seared_drain", "seared_duct", "seared_chute",
            "scorched_drain", "scorched_duct", "scorched_chute",
            "seared_tank", "seared_fuel_tank", "seared_fuel_gauge", "seared_ingot_tank", "seared_ingot_gauge",
            "scorched_tank", "scorched_fuel_tank", "scorched_fuel_gauge",
            "scorched_ingot_tank", "scorched_ingot_gauge", "scorched_proxy_tank",
            "seared_casting_tank", "seared_fluid_cannon", "scorched_fluid_cannon",
            "smeltery_io", "seared_glass", "seared_soul_glass");

    private static Set<Item> smelteryBlocks;

    public static boolean hidesSmelteryBlocks() {
        return Cfg.INSTANCE.hideTicSmelteryBlocks.get();
    }

    // also used when filtering recipe viewer catalysts, which register separately from creative.
    public static boolean isHiddenSmelteryBlock(Item item) {
        return hidesSmelteryBlocks() && smelteryBlocks().contains(item);
    }

    private static Set<Item> smelteryBlocks() {
        Set<Item> items = smelteryBlocks;
        if (items == null) {
            items = new HashSet<>();
            for (String path : TIC_SMELTERY_BLOCKS) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(TinkersMetallurgy.TIC, path));
                if (item != null && item != Items.AIR) {
                    items.add(item);
                }
            }
            smelteryBlocks = items;
        }
        return items;
    }

    private static Set<Item> hidden;

    private static Set<Item> hidden() {
        Set<Item> items = hidden;
        if (items == null) {
            items = new HashSet<>();

            if (Cfg.INSTANCE.hideDuplicateCmFluids.get()) {
                for (var fluid : ForgeRegistries.FLUIDS.getValues()) {
                    ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
                    if (id == null || !id.getNamespace().equals(TinkersMetallurgy.CM)) {
                        continue;
                    }
                    if (Fluids.isDuplicate(fluid)) {
                        Item bucket = fluid.getBucket();
                        if (bucket != null && bucket != Items.AIR) {
                            items.add(bucket);
                        }
                    }
                }
            }

            if (Cfg.INSTANCE.hideTicSmelteryBlocks.get()) {
                items.addAll(smelteryBlocks());
            }

            if (Cfg.INSTANCE.replaceGraphiteMolds.get()) {
                items.addAll(Molds.molds());
            }

            hidden = items;
        }
        return items;
    }

    @SubscribeEvent
    public static void hideFromCreative(BuildCreativeModeTabContentsEvent event) {
        Set<Item> items = hidden();
        if (items.isEmpty()) {
            return;
        }
        var iterator = event.getEntries().iterator();
        while (iterator.hasNext()) {
            if (items.contains(iterator.next().getKey().getItem())) {
                iterator.remove();
            }
        }
    }
}

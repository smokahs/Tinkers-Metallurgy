package com.tinkersmetallurgy.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import com.tinkersmetallurgy.TinkersMetallurgy;

// the tinkers fluids win. every converted recipe, and every create metallurgy recipe we rewrite,
// passes through here to land on the tinkers fluid.
public final class Fluids {

    private Fluids() {}

    private static final Set<String> SHARED = Set.of(
            "iron", "gold", "copper", "netherite", "steel", "zinc", "brass", "tungsten",
            "aluminum", "lead", "nickel", "osmium", "silver", "tin",
            "invar", "electrum", "bronze", "constantan");

    // create metallurgy only. these keep their own fluid and are pulled into tinkers as tool
    // materials by the material generator instead.
    public static final List<String> CM_ONLY = List.of("obdurium", "lithium", "void_steel", "necromium");

    public static final String SLAG = "slag";

    @Nullable
    private static Map<Fluid, Fluid> cmToTic;

    private static Map<Fluid, Fluid> map() {
        Map<Fluid, Fluid> m = cmToTic;
        if (m == null) {
            m = new HashMap<>();
            for (String metal : SHARED) {
                pair(m, metal, false);
                pair(m, metal, true);
            }
            cmToTic = m;
            TinkersMetallurgy.LOGGER.info("Fluid canon: {} Create: Metallurgy fluids fold into Tinkers'", m.size());
        }
        return m;
    }

    private static void pair(Map<Fluid, Fluid> m, String metal, boolean flowing) {
        String cmPath = (flowing ? "flowing_molten_" : "molten_") + metal;
        String ticPath = (flowing ? "flowing_molten_" : "molten_") + metal;
        Fluid cm = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(TinkersMetallurgy.CM, cmPath));
        Fluid tic = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(TinkersMetallurgy.TIC, ticPath));
        if (cm != null && tic != null && cm != tic) {
            m.put(cm, tic);
        }
    }

    public static boolean isDuplicate(Fluid fluid) {
        return map().containsKey(fluid);
    }

    // maps a create metallurgy molten fluid onto the tinkers one. other fluids pass through.
    public static Fluid canonical(Fluid fluid) {
        return map().getOrDefault(fluid, fluid);
    }

    public static FluidStack canonical(FluidStack stack) {
        Fluid canon = canonical(stack.getFluid());
        if (canon == stack.getFluid()) {
            return stack;
        }
        FluidStack out = new FluidStack(canon, stack.getAmount());
        if (stack.hasTag()) {
            out.setTag(stack.getTag().copy());
        }
        return out;
    }

    public static void invalidate() {
        cmToTic = null;
    }
}

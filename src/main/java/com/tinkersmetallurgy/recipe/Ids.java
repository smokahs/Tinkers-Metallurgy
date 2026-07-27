package com.tinkersmetallurgy.recipe;

import net.minecraft.resources.ResourceLocation;

import com.tinkersmetallurgy.TinkersMetallurgy;

// converted recipes land in our namespace keyed off the source, e.g.
// tinkersmetallurgy:melting/tconstruct/copper_ingot. keeping the source mod and path makes one easy
// to trace in a crash log, and stops two source mods colliding on the same name.
public final class Ids {

    private Ids() {}

    public static ResourceLocation derive(ResourceLocation source, String category) {
        String path = category + "/" + source.getNamespace() + "/" + source.getPath();
        return new ResourceLocation(TinkersMetallurgy.MOD_ID, sanitise(path));
    }

    // for a source recipe that expands into several, split apart by index.
    public static ResourceLocation derive(ResourceLocation source, String category, int index) {
        return derive(new ResourceLocation(source.getNamespace(), source.getPath() + "_" + index), category);
    }

    // resource locations only accept a few characters. scrub anything else a source path carries.
    private static String sanitise(String path) {
        StringBuilder sb = new StringBuilder(path.length());
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '/' || c == '.';
            sb.append(ok ? c : '_');
        }
        return sb.toString();
    }
}

package com.tinkersmetallurgy.recipe;

import javax.annotation.Nullable;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;

// create asks a recipe for its fluid results without saying which basin they land in, so a mixin
// parks the basin here for the length of the call. processing is single threaded per server tick and
// the mixin clears the slot on the way out even when the call throws, so this cannot leak.
public final class Basin {

    private Basin() {}

    private static final ThreadLocal<BasinBlockEntity> CURRENT = new ThreadLocal<>();

    public static void push(BasinBlockEntity basin) {
        CURRENT.set(basin);
    }

    public static void pop() {
        CURRENT.remove();
    }

    @Nullable
    public static BasinBlockEntity current() {
        return CURRENT.get();
    }
}

package com.tinkersmetallurgy.recipe;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.tinkersmetallurgy.TinkersMetallurgy;

@Mod.EventBusSubscriber(modid = TinkersMetallurgy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Reload {

    private Reload() {}

    @Nullable
    private static RegistryAccess captured;

    @Nullable
    private static Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> pendingByType;

    @Nullable
    private static Map<ResourceLocation, Recipe<?>> pendingByName;

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        captured = event.getRegistryAccess();
    }

    // parks the recipe manager's now writable maps until tags are bound. the maps are live, so
    // conversion changes the manager in place.
    public static void stage(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType,
                             Map<ResourceLocation, Recipe<?>> byName) {
        pendingByType = byType;
        pendingByName = byName;
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // clients get this event too when a server sends its tags, but their recipes arrive already
        // converted over the network. only the data load side has anything to do.
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            return;
        }
        captured = event.getRegistryAccess();
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            convertStaged();
        }
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        convertStaged();
    }

    private static void convertStaged() {
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType = pendingByType;
        Map<ResourceLocation, Recipe<?>> byName = pendingByName;
        pendingByType = null;
        pendingByName = null;
        if (byType != null && byName != null) {
            Convert.inject(byType, byName);
        }
    }

    @Nullable
    public static RegistryAccess registryAccess() {
        RegistryAccess access = captured;
        if (access != null) {
            return access;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.registryAccess();
    }
}

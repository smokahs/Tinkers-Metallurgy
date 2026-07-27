package com.tinkersmetallurgy.mixin;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.tinkersmetallurgy.recipe.Reload;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// makes the manager's maps writable at the end of recipe loading, then hands them to Reload.
// conversion itself runs later, once tags are bound.
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Shadow
    private Map<ResourceLocation, Recipe<?>> byName;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"))
    private void tinkersmetallurgy$stageSmeltery(Map<ResourceLocation, JsonElement> object,
                                                 ResourceManager resourceManager,
                                                 ProfilerFiller profiler,
                                                 CallbackInfo ci) {
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> mutableByType = new HashMap<>();
        this.recipes.forEach((type, map) -> mutableByType.put(type, new HashMap<>(map)));
        Map<ResourceLocation, Recipe<?>> mutableByName = new HashMap<>(this.byName);

        this.recipes = mutableByType;
        this.byName = mutableByName;

        Reload.stage(mutableByType, mutableByName);
    }
}

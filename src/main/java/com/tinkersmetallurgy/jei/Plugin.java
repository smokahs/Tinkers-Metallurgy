package com.tinkersmetallurgy.jei;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.registries.ForgeRegistries;

import slimeknights.tconstruct.plugin.jei.TConstructJEIConstants;

import com.tinkersmetallurgy.TinkersMetallurgy;
import com.tinkersmetallurgy.config.Cfg;
import com.tinkersmetallurgy.config.Cfg.Categories;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

// conversion lists every smeltery recipe twice, so one side has to give way; which one is config,
// see Categories. tinkers wins by default because it folds every material for a part into one
// cycling entry. the molding tab is always kept, being the only place cast creation is written down.
@JeiPlugin
public class Plugin implements IModPlugin {

    private static final ResourceLocation UID = new ResourceLocation(TinkersMetallurgy.MOD_ID, "jei");

    // tinkers tabs hidden even under TINKERS: it has nowhere to draw the crucible heat or the damage
    // per hit that these two turn on. see RecipeFilter.
    private static final List<RecipeType<?>> TIC_CM_SIDE = List.of(
            TConstructJEIConstants.ALLOY,
            TConstructJEIConstants.ENTITY_MELTING);

    private static final List<RecipeType<?>> TIC_SMELTERY = List.of(
            TConstructJEIConstants.CASTING_TABLE,
            TConstructJEIConstants.CASTING_BASIN,
            TConstructJEIConstants.MELTING,
            TConstructJEIConstants.FOUNDRY,
            TConstructJEIConstants.ALLOY,
            TConstructJEIConstants.ENTITY_MELTING);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Cfg.INSTANCE.jeiCategories.get() != Categories.TINKERS) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            CmCasting.register(registration, level);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        catalyst(registration, "casting_table", TConstructJEIConstants.MOLDING);
        catalyst(registration, "casting_basin", TConstructJEIConstants.MOLDING);

        if (Cfg.INSTANCE.jeiCategories.get() != Categories.TINKERS) {
            return;
        }

        catalyst(registration, "foundry_lid", TConstructJEIConstants.MELTING, TConstructJEIConstants.FOUNDRY);
        catalyst(registration, "foundry_basin", TConstructJEIConstants.MELTING, TConstructJEIConstants.FOUNDRY);

        catalyst(registration, "industrial_crucible", TConstructJEIConstants.MELTING,
                TConstructJEIConstants.FOUNDRY);

        catalyst(registration, "casting_table", TConstructJEIConstants.CASTING_TABLE);
        catalyst(registration, "casting_basin", TConstructJEIConstants.CASTING_BASIN);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        IRecipeManager recipes = runtime.getRecipeManager();
        switch (Cfg.INSTANCE.jeiCategories.get()) {
            case CREATE_METALLURGY -> hideTinkersCategories(recipes);
            case TINKERS -> hideCmCasting(recipes);
            case BOTH -> { }
        }
    }

    private static void hideCmCasting(IRecipeManager recipes) {
        for (RecipeType<?> type : CmCategories.casting()) {
            recipes.hideRecipeCategory(type);
        }
        for (RecipeType<?> type : TIC_CM_SIDE) {
            recipes.hideRecipeCategory(type);
        }
        TinkersMetallurgy.LOGGER.info(
                "Kept {} converted recipes out of Create: Metallurgy's categories; hid its casting tabs"
                        + " and Tinkers' alloying and mob boiling ones",
                RecipeFilter.filtered());
    }

    private static void hideTinkersCategories(IRecipeManager recipes) {
        for (RecipeType<?> type : TIC_SMELTERY) {
            recipes.hideRecipeCategory(type);
        }
        TinkersMetallurgy.LOGGER.info(
                "Hid {} Tinkers' smeltery categories; the Create: Metallurgy machines show these recipes",
                TIC_SMELTERY.size());
    }

    // looked up by name rather than referenced, so a block create metallurgy renames goes quiet
    // instead of throwing.
    private static void catalyst(IRecipeCatalystRegistration registration, String name,
                                 RecipeType<?>... types) {
        Block block = block(name);
        if (block != null) {
            registration.addRecipeCatalyst(block, types);
        }
    }

    @Nullable
    private static Block block(String name) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(TinkersMetallurgy.CM, name));
        return block == null || block == Blocks.AIR ? null : block;
    }
}

package com.tinkersmetallurgy.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraftforge.fluids.FluidStack;

import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.plugin.jei.TConstructJEIConstants;

import fr.lucreeper74.createmetallurgy.content.blocks.casting.recipe.CastingRecipe;
import fr.lucreeper74.createmetallurgy.registries.CMRecipeTypes;

import com.tinkersmetallurgy.TinkersMetallurgy;

import mezz.jei.api.registration.IRecipeRegistration;

// conversion only runs tinkers onto create metallurgy, so create metallurgy's own casting recipes
// would vanish with its tabs. relisting them in the tinkers tabs borrows the display only; they
// stay create metallurgy recipes and run as before.
final class CmCasting implements IDisplayableCastingRecipe {

    private final ResourceLocation id;
    private final List<ItemStack> casts;
    private final boolean consumed;
    private final List<FluidStack> fluids;
    private final ItemStack output;
    private final int coolingTime;

    private CmCasting(ResourceLocation id, List<ItemStack> casts, boolean consumed,
                      List<FluidStack> fluids, ItemStack output, int coolingTime) {
        this.id = id;
        this.casts = casts;
        this.consumed = consumed;
        this.fluids = fluids;
        this.output = output;
        this.coolingTime = coolingTime;
    }

    static void register(IRecipeRegistration registration, ClientLevel level) {
        RegistryAccess access = level.registryAccess();
        RecipeType<?> table = CMRecipeTypes.CASTING_IN_TABLE.getType();
        RecipeType<?> basin = CMRecipeTypes.CASTING_IN_BASIN.getType();

        List<IDisplayableCastingRecipe> tableRecipes = new ArrayList<>();
        List<IDisplayableCastingRecipe> basinRecipes = new ArrayList<>();
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            // ours came from the tinkers side anyway, and are already listed there.
            if (!(recipe instanceof CastingRecipe casting)
                    || TinkersMetallurgy.MOD_ID.equals(recipe.getId().getNamespace())) {
                continue;
            }
            CmCasting display = of(casting, access);
            if (display == null) {
                continue;
            }
            if (recipe.getType() == table) {
                tableRecipes.add(display);
            } else if (recipe.getType() == basin) {
                basinRecipes.add(display);
            }
        }

        registration.addRecipes(TConstructJEIConstants.CASTING_TABLE, tableRecipes);
        registration.addRecipes(TConstructJEIConstants.CASTING_BASIN, basinRecipes);
        TinkersMetallurgy.LOGGER.info(
                "Listed {} Create: Metallurgy casting recipes in Tinkers' categories",
                tableRecipes.size() + basinRecipes.size());
    }

    @Nullable
    private static CmCasting of(CastingRecipe recipe, RegistryAccess access) {
        ItemStack output = recipe.getResultItem(access);
        List<FluidStack> fluids = recipe.getFluidIngredient().getMatchingFluidStacks();
        if (output.isEmpty() || fluids.isEmpty()) {
            return null;
        }
        List<ItemStack> casts = Arrays.asList(recipe.getIngredient().getItems());
        return new CmCasting(recipe.getId(), casts, recipe.isMoldConsumed(), fluids, output,
                recipe.getProcessingDuration());
    }

    @Override
    public ResourceLocation getRecipeId() {
        return id;
    }

    @Override
    public boolean hasCast() {
        return !casts.isEmpty();
    }

    @Override
    public List<ItemStack> getCastItems() {
        return casts;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public List<FluidStack> getFluids() {
        return fluids;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public int getCoolingTime() {
        return coolingTime;
    }
}

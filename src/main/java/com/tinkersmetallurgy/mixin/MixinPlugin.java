package com.tinkersmetallurgy.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

// a mixin whose target is missing hard crashes the game. jei is optional; create and create
// metallurgy are required, but mixins are prepared before forge checks dependencies, so without the
// gate a missing one crashes here instead of reaching the missing-mods screen that names it.
// ModList cannot be asked this early, so check whether the target class is on the classpath at all.
public class MixinPlugin implements IMixinConfigPlugin {

    private static final String CM_CASTING_BLOCK =
            "fr.lucreeper74.createmetallurgy.content.blocks.casting.CastingBlock";

    private static final String CREATE_PROCESSING_RECIPE =
            "com.simibubi.create.content.processing.recipe.ProcessingRecipe";

    private static final String JEI_CATALYST_REGISTRATION =
            "mezz.jei.api.registration.IRecipeCatalystRegistration";

    private static final Set<String> CM_MIXINS = Set.of(
            "com.tinkersmetallurgy.mixin.CastingBlockMixin",
            "com.tinkersmetallurgy.mixin.CastingRecipeAccessor",
            "com.tinkersmetallurgy.mixin.CastingRendererMixin");

    private static final Set<String> CREATE_MIXINS = Set.of(
            "com.tinkersmetallurgy.mixin.ProcessingRecipeAccessor",
            "com.tinkersmetallurgy.mixin.BasinRecipeMixin",
            "com.tinkersmetallurgy.mixin.CreateJeiMixin");

    private static final Set<String> JEI_MIXINS = Set.of(
            "com.tinkersmetallurgy.mixin.TicCatalystMixin",
            "com.tinkersmetallurgy.mixin.TicCategoryMixin",
            "com.tinkersmetallurgy.mixin.CreateJeiMixin");

    private boolean cmPresent;
    private boolean createPresent;
    private boolean jeiPresent;

    @Override
    public void onLoad(String mixinPackage) {
        cmPresent = classExists(CM_CASTING_BLOCK);
        createPresent = classExists(CREATE_PROCESSING_RECIPE);
        jeiPresent = classExists(JEI_CATALYST_REGISTRATION);
    }

    // Class.forName defines the class, and defining a mixin target before mixin can transform it
    // fails the whole config with "loaded too early". a resource lookup asks the same question.
    private static boolean classExists(String name) {
        String resource = name.replace('.', '/') + ".class";
        return MixinPlugin.class.getClassLoader().getResource(resource) != null;
    }

    // a mixin listed under several mods needs all of them, so every gate has to agree.
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (CM_MIXINS.contains(mixinClassName) && !cmPresent) {
            return false;
        }
        if (CREATE_MIXINS.contains(mixinClassName) && !createPresent) {
            return false;
        }
        return !JEI_MIXINS.contains(mixinClassName) || jeiPresent;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}

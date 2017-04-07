package codechicken.nei.jei.proxy;

import mezz.jei.Internal;
import mezz.jei.RecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocus.Mode;
import net.minecraft.item.ItemStack;

/**
 * Created by covers1624 on 7/04/2017.
 */
public class JEIProxy implements IJEIProxy {

    @Override
    public boolean isJEIEnabled() {
        return true;//TODO, InGame config.
    }

    @Override
    public void openRecipeGui(ItemStack stack) {
        RecipeRegistry registry = Internal.getRuntime().getRecipeRegistry();
        IFocus focus = registry.createFocus(Mode.OUTPUT, stack);
        if (registry.getRecipeCategories(focus).isEmpty()) {
            return;
        }
        Internal.getRuntime().getRecipesGui().show(focus);
    }

    @Override
    public void openUsageGui(ItemStack stack) {
        RecipeRegistry registry = Internal.getRuntime().getRecipeRegistry();
        IFocus focus = registry.createFocus(Mode.INPUT, stack);
        if (registry.getRecipeCategories(focus).isEmpty()) {
            return;
        }
        Internal.getRuntime().getRecipesGui().show(focus);
    }
}

package codechicken.nei.util.helper.potion;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * Created by covers1624 on 3/21/2016.
 */
public interface IPotionRecipe {

    ItemStack getRecipeOutput();

    ItemStack getRecipeInput();

    Ingredient getRecipeIngredient();
}

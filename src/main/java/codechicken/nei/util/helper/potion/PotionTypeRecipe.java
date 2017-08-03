package codechicken.nei.util.helper.potion;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

/**
 * Created by covers1624 on 3/21/2016.
 * Between different types Water > Mundane > blah.
 */
public class PotionTypeRecipe implements IPotionRecipe {

    private ItemStack input;
    private Ingredient ingredient;
    private ItemStack output;

    public PotionTypeRecipe(ItemStack input, Ingredient ingredient, PotionType outputType) {
        this.ingredient = ingredient;
        this.input = input.copy();
        this.output = PotionUtils.addPotionToItemStack(input, outputType);
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output;
    }

    @Override
    public ItemStack getRecipeInput() {
        return input;
    }

    @Override
    public Ingredient getRecipeIngredient() {
        return ingredient;
    }
}

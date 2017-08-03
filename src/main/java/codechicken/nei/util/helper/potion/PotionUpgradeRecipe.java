package codechicken.nei.util.helper.potion;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by covers1624 on 3/21/2016.
 * Upgrade between different states, Normal > Splash > Lingering.
 */
public class PotionUpgradeRecipe implements IPotionRecipe {

    private ItemStack input;
    private Ingredient ingredient;
    private ItemStack output;

    public PotionUpgradeRecipe(ItemStack input, Ingredient ingredient, Item output) {
        this.input = input;
        this.ingredient = ingredient;
        int stackSize = input.getCount();
        int meta = input.getItemDamage();
        NBTTagCompound tagCompound = input.getTagCompound();
        this.output = new ItemStack(output, stackSize, meta);
        this.output.setTagCompound(tagCompound);
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

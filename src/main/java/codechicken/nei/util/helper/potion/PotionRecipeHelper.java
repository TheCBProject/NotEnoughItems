package codechicken.nei.util.helper.potion;

import codechicken.nei.util.LogHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 3/21/2016.
 * This is used as a local potion registry for nei.
 * Don't bother using this.
 */
public class PotionRecipeHelper {

    private static ArrayList<IPotionRecipe> allRecipes = new ArrayList<>();
    private static ArrayList<IPotionRecipe> normalRecipes = new ArrayList<>();
    private static ArrayList<IPotionRecipe> splashRecipes = new ArrayList<>();
    private static ArrayList<IPotionRecipe> lingeringRecipes = new ArrayList<>();

    public static void addNormalRecipe(Item potionItem, PotionType input, Ingredient ingredient, PotionType output) {
        IPotionRecipe recipe = new PotionTypeRecipe(PotionUtils.addPotionToItemStack(new ItemStack(potionItem), input), ingredient, output);
        normalRecipes.add(recipe);
        allRecipes.add(recipe);
    }

    public static void addNormalRecipe(IPotionRecipe recipe) {
        normalRecipes.add(recipe);
    }

    public static void addSplashRecipe(Item potionItem, PotionType input, Ingredient ingredient, PotionType output) {
        IPotionRecipe recipe = new PotionTypeRecipe(PotionUtils.addPotionToItemStack(new ItemStack(potionItem), input), ingredient, output);
        splashRecipes.add(recipe);
        allRecipes.add(recipe);
    }

    public static void addSplashRecipe(IPotionRecipe recipe) {
        splashRecipes.add(recipe);
    }

    public static void addLingeringRecipe(Item potionItem, PotionType input, Ingredient ingredient, PotionType output) {
        IPotionRecipe recipe = new PotionTypeRecipe(PotionUtils.addPotionToItemStack(new ItemStack(potionItem), input), ingredient, output);
        lingeringRecipes.add(recipe);
        allRecipes.add(recipe);
    }

    public static void addLingeringRecipe(IPotionRecipe recipe) {
        lingeringRecipes.add(recipe);
    }

    public static void init() {//TODO Don't make assumptions as to what the ingredient is to achieve next tear, as Minetweaker may change it.
        LogHelper.trace("Loading portion recipes..");
        try {
            for (PotionHelper.MixPredicate<PotionType> entry : PotionHelper.POTION_TYPE_CONVERSIONS) {
                PotionType input = entry.input;
                Ingredient ingredient =  entry.reagent;
                PotionType output = entry.output;
                addNormalRecipe(Items.POTIONITEM, input, ingredient, output);
                addSplashRecipe(Items.SPLASH_POTION, input, ingredient, output);
                addLingeringRecipe(Items.LINGERING_POTION, input, ingredient, output);
            }

            for (IPotionRecipe recipe : normalRecipes) {
                IPotionRecipe upgradeRecipe = new PotionUpgradeRecipe(recipe.getRecipeOutput(), Ingredient.fromItems(Items.GUNPOWDER), Items.SPLASH_POTION);
                allRecipes.add(upgradeRecipe);
            }

            for (IPotionRecipe recipe : splashRecipes) {
                IPotionRecipe upgradeRecipe = new PotionUpgradeRecipe(recipe.getRecipeOutput(), Ingredient.fromItems(Items.DRAGON_BREATH), Items.LINGERING_POTION);
                allRecipes.add(upgradeRecipe);
            }

            //for (IPotionRecipe recipe : allRecipes) {
            //    LogHelper.info("Input: [%s], Ingredient: [%s], Output: [%s].", recipe.getRecipeInput().toString() + " " + recipe.getRecipeInput().getTagCompound().toString(), recipe.getRecipeIngredient().toString(), recipe.getRecipeOutput().toString() + " " + recipe.getRecipeOutput().getTagCompound().toString());
            //}
        } catch (Exception e) {
            LogHelper.error("Unable to load potion recipes!");
            e.printStackTrace();
        }
    }

    public static List<IPotionRecipe> getRecipes() {
        return allRecipes;
    }

    public static PotionType getPotionTypeFromStack(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tagCompound = itemStack.getTagCompound();
            if (tagCompound.hasKey("Potion")) {
                String potion = tagCompound.getString("Potion");
                PotionType type = PotionType.getPotionTypeForName(potion);
                if (type != null) {
                    return type;
                }
            }
        }
        return null;
    }

}

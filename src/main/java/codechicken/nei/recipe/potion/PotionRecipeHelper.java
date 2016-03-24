package codechicken.nei.recipe.potion;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    private static ArrayList<IPotionRecipe> allRecipes = new ArrayList<IPotionRecipe>();
    private static ArrayList<IPotionRecipe> normalRecipes = new ArrayList<IPotionRecipe>();
    private static ArrayList<IPotionRecipe> splashRecipes = new ArrayList<IPotionRecipe>();
    private static ArrayList<IPotionRecipe> lingeringRecipes = new ArrayList<IPotionRecipe>();

    public static void addNormalRecipe(Item potionItem, PotionType input, Item ingredient, PotionType output) {
        IPotionRecipe recipe = new PotionTypeRecipe(PotionUtils.addPotionToItemStack(new ItemStack(potionItem), input), new ItemStack(ingredient), output);
        normalRecipes.add(recipe);
        allRecipes.add(recipe);
    }

    public static void addNormalRecipe(IPotionRecipe recipe) {
        normalRecipes.add(recipe);
    }

    public static void addSplashRecipe(Item potionItem, PotionType input, Item ingredient, PotionType output) {
        IPotionRecipe recipe = new PotionTypeRecipe(PotionUtils.addPotionToItemStack(new ItemStack(potionItem), input), new ItemStack(ingredient), output);
        splashRecipes.add(recipe);
        allRecipes.add(recipe);
    }

    public static void addSplashRecipe(IPotionRecipe recipe) {
        splashRecipes.add(recipe);
    }

    public static void addLingeringRecipe(Item potionItem, PotionType input, Item ingredient, PotionType output) {
        IPotionRecipe recipe = new PotionTypeRecipe(PotionUtils.addPotionToItemStack(new ItemStack(potionItem), input), new ItemStack(ingredient), output);
        lingeringRecipes.add(recipe);
        allRecipes.add(recipe);
    }

    public static void addLingeringRecipe(IPotionRecipe recipe) {
        lingeringRecipes.add(recipe);
    }

    public static void init() {
        for (PotionHelper.MixPredicate<PotionType> entry : PotionHelper.POTION_TYPE_CONVERSIONS) {
            PotionType input = entry.input;
            PotionHelper.ItemPredicateInstance ingredient = (PotionHelper.ItemPredicateInstance) entry.reagent;
            PotionType output = entry.output;
            addNormalRecipe(Items.potionitem, input, ingredient.item, output);
            addSplashRecipe(Items.splash_potion, input, ingredient.item, output);
            addLingeringRecipe(Items.lingering_potion, input, ingredient.item, output);
        }

        for (IPotionRecipe recipe : normalRecipes) {
            IPotionRecipe upgradeRecipe = new PotionUpgradeRecipe(recipe.getRecipeOutput(), new ItemStack(Items.gunpowder), Items.splash_potion);
            allRecipes.add(upgradeRecipe);
        }

        for (IPotionRecipe recipe : splashRecipes) {
            IPotionRecipe upgradeRecipe = new PotionUpgradeRecipe(recipe.getRecipeOutput(), new ItemStack(Items.dragon_breath), Items.lingering_potion);
            allRecipes.add(upgradeRecipe);
        }

        //for (IPotionRecipe recipe : allRecipes) {
        //    LogHelper.info("Input: [%s], Ingredient: [%s], Output: [%s].", recipe.getRecipeInput().toString() + " " + recipe.getRecipeInput().getTagCompound().toString(), recipe.getRecipeIngredient().toString(), recipe.getRecipeOutput().toString() + " " + recipe.getRecipeOutput().getTagCompound().toString());
        //}

    }

    public static List<IPotionRecipe> getRecipes() {
        return allRecipes;
    }

}

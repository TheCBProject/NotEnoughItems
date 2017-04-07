package codechicken.nei.jei.proxy;

import net.minecraft.item.ItemStack;

/**
 * Created by covers1624 on 7/14/2016.
 */
public interface IJEIProxy {

    boolean isJEIEnabled();

    void openRecipeGui(ItemStack stack);

    void openUsageGui(ItemStack stack);

}

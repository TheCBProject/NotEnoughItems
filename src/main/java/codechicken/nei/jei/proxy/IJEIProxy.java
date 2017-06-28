package codechicken.nei.jei.proxy;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by covers1624 on 7/14/2016.
 */
public interface IJEIProxy {

    boolean isJEIEnabled();

    default void openRecipeGui(ItemStack stack){}

    default void openUsageGui(ItemStack stack){}

    default boolean isBlacklistedJEI(ItemStack stack) {
        return false;
    }

    default Set<Rectangle> getExtraAreas(GuiContainer container) {
        return Collections.emptySet();
    }
}

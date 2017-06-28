package codechicken.nei.util;

import codechicken.nei.util.helper.GuiHelper;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This is an internal class for storing information about items, to be accessed by the API
 */
public class ItemInfo {

    public static final ItemStackMap<String> nameOverrides = new ItemStackMap<>(null);
    public static final ItemStackSet hiddenItems = new ItemStackSet();
    public static final ItemStackSet finiteItems = new ItemStackSet();
    public static final ArrayListMultimap<Item, ItemStack> itemOverrides = ArrayListMultimap.create();
    public static final ArrayListMultimap<Item, ItemStack> itemVariants = ArrayListMultimap.create();
    public static final HashSet<Class<? extends Slot>> fastTransferExemptions = new HashSet<>();
    public static final HashSet<Class<? extends GuiContainer>> fastTransferContainerExemptions = new HashSet<>();

    public static final HashMap<Item, String> itemOwners = new HashMap<>();

    //lookup optimisation
    public static final HashMap<ItemStack, String> itemSearchNames = new HashMap<>();

    public static boolean isHidden(ItemStack stack) {
        return hiddenItems.contains(stack);
    }

    public static boolean isHidden(Item item) {
        return hiddenItems.containsAll(item);
    }

    public static String getNameOverride(ItemStack stack) {
        return nameOverrides.get(stack);
    }

    public static boolean canBeInfinite(ItemStack stack) {
        return !finiteItems.contains(stack);
    }

    public static String getSearchName(ItemStack stack) {
        String s = itemSearchNames.computeIfAbsent(stack, s1 -> TextFormatting.getTextWithoutFormattingCodes(GuiHelper.concatenatedDisplayName(s1, true).toLowerCase()));
        return s;
    }
}

package codechicken.nei.api;

import codechicken.lib.item.filtering.IItemFilter;
import codechicken.lib.item.filtering.IItemFilterProvider;
import codechicken.nei.ItemSorter;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.Option;
import codechicken.nei.layout.LayoutStyle;
import codechicken.nei.util.ItemInfo;
import codechicken.nei.util.ItemList;
import codechicken.nei.util.ItemStackSet;
import codechicken.nei.widget.SearchField;
import codechicken.nei.widget.SearchField.ISearchProvider;
import codechicken.nei.widget.SubsetWidget;
import codechicken.nei.widget.SubsetWidget.SubsetTag;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;

/**
 * This is the main class that handles item property configuration.
 * WARNING: DO NOT access this class until the world has been loaded
 * These methods should be called from INEIConfig implementors
 */
public class API {

    public static void registerNEIGuiHandler(INEIGuiHandler handler) {
        GuiInfo.guiHandlers.add(handler);
    }

    /**
     * Hide an item from the item panel
     * Damage values of OreDictionary.WILDCARD_VALUE and ItemStackMap.WILDCARD_TAG tags function as wildcards for their respective variables
     */
    public static void hideItem(ItemStack item) {
        ItemInfo.hiddenItems.add(item);
    }

    /**
     * Add or replace the name normally shown on the item tooltip
     */
    public static void setOverrideName(ItemStack item, String name) {
        ItemInfo.nameOverrides.put(item, name);
    }

    /**
     * Adds an item to the item panel. Any items added using this function will override the default search pattern.
     *
     * @param item an item with data
     */
    public static void addItemListEntry(ItemStack item) {
        ItemInfo.itemOverrides.put(item.getItem(), item);
    }

    /**
     * Sets the item variants to appear in the item panel, overriding the default search pattern for a given item
     */
    public static void setItemListEntries(Item item, Iterable<ItemStack> items) {
        if (items == null) {
            items = Collections.emptyList();
        }
        ItemInfo.itemOverrides.replaceValues(item, items);
    }

    public static void addOption(Option option) {
        NEIClientConfig.getOptionList().addOption(option);
    }

    /**
     * Add a new Layout Style for the NEI interface
     *
     * @param styleID The Unique ID to be used for storing your style in the config and cycling through avaliable styles
     * @param style   The style to add.
     */
    public static void addLayoutStyle(int styleID, LayoutStyle style) {
        LayoutManager.layoutStyles.put(styleID, style);
    }

    /**
     * Tells NEI not to perform any Fast Transfer operations on slots of a particular class
     *
     * @param slotClass The class of slot to be exempted
     */
    public static void addFastTransferExemptSlot(Class<? extends Slot> slotClass) {
        ItemInfo.fastTransferExemptions.add(slotClass);
    }

    /**
     * Tells NEI not to perform any Fast Transfer operations on a GuiContainer of a specific class.
     *
     * @param guiClass The class of the container to be exempted
     */
    public static void addFastTransferExemptContainer(Class<? extends GuiContainer> guiClass) {
        ItemInfo.fastTransferContainerExemptions.add(guiClass);
    }

    /**
     * Register a filter provider for the item panel.
     *
     * @param filterProvider The filter provider to be registered.
     */
    public static void addItemFilter(IItemFilterProvider filterProvider) {
        ItemList.registerIItemFilterProvider(filterProvider);
    }

    /**
     * Adds a new tag to the item subset dropdown.
     *
     * @param name   The fully qualified name, Eg Blocks.MobSpawners. NOT case sensitive
     * @param filter A filter for matching items that fit in this subset
     */
    public static void addSubset(String name, IItemFilter filter) {
        addSubset(new SubsetTag(name, filter));
    }

    /**
     * Adds a new tag to the item subset dropdown.
     *
     * @param name  The fully qualified name, Eg Blocks.MobSpawners. NOT case sensitive
     * @param items An iterable of itemstacks to be added as a subset
     */
    public static void addSubset(String name, Iterable<ItemStack> items) {
        ItemStackSet filter = new ItemStackSet();
        for (ItemStack item : items) {
            filter.add(item);
        }
        addSubset(new SubsetTag(name, filter));
    }

    /**
     * Adds a new tag to the item subset dropdown.
     */
    public static void addSubset(SubsetTag tag) {
        SubsetWidget.addTag(tag);
    }

    /**
     * Adds a new search provider to the search field
     */
    public static void addSearchProvider(ISearchProvider provider) {
        SearchField.searchProviders.add(provider);
    }

    /**
     * Adds a new sorting option to the item panel sort menu
     *
     * @param name A unique id for this sort option. Will be used in the config for saving and translated in the options gui. Note that if the translation key name.tip exists, it will be used for a tooltip
     */
    public static void addSortOption(String name, Comparator<ItemStack> comparator) {
        ItemSorter.add(name, comparator);
    }

    /**
     * Adds an additional item list entry for an item, sorted after the rest of the items are found through the normal process
     *
     * @param item    The item to add the variant for
     * @param variant The stack to appear in the item panel
     */
    public static void addItemVariant(Item item, ItemStack variant) {
        ItemInfo.itemVariants.put(item, variant);
    }
}

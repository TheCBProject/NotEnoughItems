package codechicken.nei.util;

import codechicken.lib.item.filtering.IItemFilter;
import codechicken.lib.item.filtering.IItemFilterProvider;
import codechicken.lib.thread.RestartableTask;
import codechicken.lib.thread.ThreadOperationTimer;
import codechicken.lib.thread.ThreadOperationTimer.TimeoutException;
import codechicken.nei.ItemSorter;
import codechicken.nei.util.helper.GuiHelper;
import codechicken.nei.widget.ItemPanel;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class ItemList {

    /**
     * Fields are replaced atomically and contents never modified.
     */
    public static volatile List<ItemStack> items = new ArrayList<>();

    /**
     * Fields are replaced atomically and contents never modified.
     */
    public static volatile ListMultimap<Item, ItemStack> itemMap = ArrayListMultimap.create();

    /**
     * Updates to this should be synchronised on this
     * This field is iterated on another thread.
     */
    private static final List<IItemFilterProvider> itemFilterProviders = new LinkedList<>();
    private static final List<ItemsLoadedCallback> loadCallbacks = new LinkedList<>();

    private static HashSet<Item> erroredItems = new HashSet<>();

    public static synchronized void registerIItemFilterProvider(IItemFilterProvider provider) {
        synchronized (itemFilterProviders) {
            itemFilterProviders.add(provider);
        }
    }

    public static synchronized void registerLoadCallback(ItemsLoadedCallback callback) {
        synchronized (loadCallbacks) {
            loadCallbacks.add(callback);
        }
    }

    public static class EverythingItemFilter implements IItemFilter {

        @Override
        public boolean matches(ItemStack item) {
            return true;
        }
    }

    public static class NothingItemFilter implements IItemFilter {

        @Override
        public boolean matches(ItemStack item) {
            return false;
        }
    }

    public static class PatternItemFilter implements IItemFilter {

        public Pattern pattern;

        public PatternItemFilter(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(ItemStack item) {
            return pattern.matcher(ItemInfo.getSearchName(item)).find();
        }
    }

    public static class AllMultiItemFilter implements IItemFilter {

        public List<IItemFilter> filters = new LinkedList<>();

        public AllMultiItemFilter(List<IItemFilter> filters) {
            this.filters = filters;
        }

        public AllMultiItemFilter() {
            this(new LinkedList<>());
        }

        @Override
        public boolean matches(ItemStack item) {
            for (IItemFilter filter : filters) {
                try {
                    if (!filter.matches(item)) {
                        return false;
                    }
                } catch (Exception e) {
                    LogHelper.errorError("Exception filtering " + item + " with " + filter, e);
                }
            }

            return true;
        }
    }

    public static class AnyMultiItemFilter implements IItemFilter {

        public List<IItemFilter> filters = new LinkedList<>();

        public AnyMultiItemFilter(List<IItemFilter> filters) {
            this.filters = filters;
        }

        public AnyMultiItemFilter() {
            this(new LinkedList<>());
        }

        @Override
        public boolean matches(ItemStack item) {
            for (IItemFilter filter : filters) {
                try {
                    if (filter.matches(item)) {
                        return true;
                    }
                } catch (Exception e) {
                    LogHelper.errorError("Exception filtering " + item + " with " + filter, e);
                }
            }

            return false;
        }
    }

    public interface ItemsLoadedCallback {

        void itemsLoaded();
    }

    public static boolean itemMatchesAll(ItemStack item, List<IItemFilter> filters) {
        for (IItemFilter filter : filters) {
            try {
                if (!filter.matches(item)) {
                    return false;
                }
            } catch (Exception e) {
                LogHelper.errorError("Exception filtering " + item + " with " + filter, e);
            }
        }

        return true;
    }

    public static IItemFilter getItemListFilter() {
        return new AllMultiItemFilter(getItemFilters());
    }

    public static List<IItemFilter> getItemFilters() {
        LinkedList<IItemFilter> filters = new LinkedList<>();
        synchronized (itemFilterProviders) {
            for (IItemFilterProvider p : itemFilterProviders) {
                filters.add(p.getFilter());
            }
        }
        return filters;
    }

    public static final RestartableTask loadItems = new RestartableTask("NEI Item Loading") {

        private void damageSearch(Item item, List<ItemStack> permutations) {
            HashSet<String> damageIconSet = new HashSet<>();
            for (int damage = 0; damage < 16; damage++) {
                try {
                    ItemStack stack = new ItemStack(item, 1, damage);
                    IBakedModel model = GuiHelper.getRenderItem().getItemModelMesher().getItemModel(stack);
                    String name = GuiHelper.concatenatedDisplayName(stack, false);
                    String s = name + "@" + (model == null ? 0 : model.hashCode());
                    if (!damageIconSet.contains(s)) {
                        damageIconSet.add(s);
                        permutations.add(stack);
                    }
                } catch (TimeoutException t) {
                    throw t;
                } catch (Throwable t) {
                    LogHelper.errorOnce(t, item.toString(), "Omitting %s:%s %s", item, damage, item.getClass().getSimpleName());
                }
            }
        }

        @Override
        public void execute() {
            ThreadOperationTimer timer = getTimer(500);

            LinkedList<ItemStack> items = new LinkedList<>();
            LinkedList<ItemStack> permutations = new LinkedList<>();
            ListMultimap<Item, ItemStack> itemMap = ArrayListMultimap.create();

            timer.setLimit(500);
            for (Item item : Item.REGISTRY) {
                if (interrupted()) {
                    return;
                }

                if (item == null || erroredItems.contains(item) || item == Items.AIR) {
                    continue;
                }

                try {
                    timer.reset(item);

                    permutations.clear();
                    permutations.addAll(ItemInfo.itemOverrides.get(item));

                    if (permutations.isEmpty()) {
                        item.getSubItems(CreativeTabs.SEARCH, new NonNullList<>(permutations, null));
                    }

                    //TODO, the implementation of damageSearch is wrong, not sure if this is actually needed ever.
                    //if (permutations.isEmpty()) {
                    //    damageSearch(item, permutations);
                    //}

                    permutations.addAll(ItemInfo.itemVariants.get(item));

                    timer.reset();
                    items.addAll(permutations);
                    itemMap.putAll(item, permutations);
                } catch (Throwable t) {
                    LogHelper.errorError("Removing item: %s from list.", t, item);
                    erroredItems.add(item);
                }
            }

            if (interrupted()) {
                return;
            }
            ItemList.items = items;
            ItemList.itemMap = itemMap;
            synchronized (loadCallbacks) {
                for (ItemsLoadedCallback callback : loadCallbacks) {
                    callback.itemsLoaded();
                }
            }

            updateFilter.restart();
        }
    };

    public static final RestartableTask updateFilter = new RestartableTask("NEI Item Filtering") {
        @Override
        public void execute() {
            ArrayList<ItemStack> filtered = new ArrayList<>();
            IItemFilter filter = getItemListFilter();
            for (ItemStack item : items) {
                if (interrupted()) {
                    return;
                }
                try {
                    if (filter.matches(item)) {
                        filtered.add(item);
                    }
                } catch (Throwable e) {
                    LogHelper.errorOnce(e, item.toString(), "Error whilst filtering: %s", item.toString());
                }
            }

            if (interrupted()) {
                return;
            }
            ItemSorter.sort(filtered);
            if (interrupted()) {
                return;
            }
            ItemPanel.updateItemList(filtered);
        }
    };
}

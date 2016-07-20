package codechicken.nei.featurehack;

import codechicken.nei.ItemList;
import codechicken.nei.ItemSorter;
import codechicken.nei.RestartableTask;
import codechicken.nei.api.ItemFilter;
import com.google.common.base.Strings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiContainerCreative.ContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by covers1624 on 7/20/2016.
 */
public class VanillaCreativeTabSearchHooks {

    private static HashMap<CreativeTabs, ArrayList<ItemStack>> tabCache = new HashMap<CreativeTabs, ArrayList<ItemStack>>();

    public static void updateSearchListThreaded(GuiContainerCreative guiContainerCreative) {
        filterTask.setContainerCreative((ContainerCreative) guiContainerCreative.inventorySlots);
        filterTask.setSearchBox(guiContainerCreative.searchField);
        filterTask.setCreativeTab(CreativeTabs.CREATIVE_TAB_ARRAY[guiContainerCreative.getSelectedTabIndex()]);
        filterTask.restart();
    }

    public static final VanillaFilterTask filterTask = new VanillaFilterTask("NEI Vanilla creative tab filtering.");

    public static class VanillaFilterTask extends RestartableTask {
        private ContainerCreative containerCreative;
        private GuiTextField searchBox;
        private CreativeTabs creativeTab;

        public VanillaFilterTask(String name) {
            super(name);
        }

        public void setContainerCreative(ContainerCreative containerCreative) {
            this.containerCreative = containerCreative;
        }

        public void setSearchBox(GuiTextField searchBox) {
            this.searchBox = searchBox;
        }

        public void setCreativeTab(CreativeTabs creativeTab) {
            this.creativeTab = creativeTab;
        }

        public GuiTextField getSearchBox() {
            return searchBox;
        }

        public CreativeTabs getCreativeTab() {
            return creativeTab;
        }


        @Override
        public void execute() {
            ArrayList<ItemStack> filtered = new ArrayList<ItemStack>();
            if (containerCreative == null) {
                stop();
            }
            ItemFilter filter = VanillaFilter.INSTANCE;
            for (ItemStack item : getStacksForTab(getCreativeTab())) {
                if (interrupted()) {
                    return;
                }

                if (filter.matches(item)) {
                    filtered.add(item);
                }
            }

            if (interrupted()) {
                return;
            }
            ItemSorter.sort(filtered);
            if (interrupted()) {
                return;
            }
            containerCreative.itemList = filtered;
            containerCreative.scrollTo(0.0F);
        }


    }

    public static class VanillaFilter implements ItemFilter {

        public static VanillaFilter INSTANCE = new VanillaFilter();

        @Override
        public boolean matches(ItemStack item) {
            GuiTextField textField = filterTask.getSearchBox();
            if (Strings.isNullOrEmpty(textField.getText())){
                return true;
            }
            if (textField.getText().toLowerCase().startsWith("@")) {
                String expectedMod = textField.getText().toLowerCase().replace("@", "");
                if (expectedMod.isEmpty()){
                    return true;
                }
                if (item.getItem().getRegistryName().getResourceDomain().startsWith(expectedMod)) {
                    return true;
                }
            }
            for (String toolTipString : item.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips)) {
                if (TextFormatting.getTextWithoutFormattingCodes(toolTipString).toLowerCase().contains(textField.getText().toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }



    private static List<ItemStack> getStacksForTab(CreativeTabs creativeTab){
        if (creativeTab == CreativeTabs.SEARCH){
            return ItemList.items;
        }
        ArrayList<ItemStack> tabStacks = new ArrayList<ItemStack>();
        if (tabCache.containsKey(creativeTab)){
            tabCache.get(creativeTab);
        } else {
            for (ItemStack stack : ItemList.items){
                if (stack.getItem().getCreativeTab() == creativeTab){
                    tabStacks.add(stack);
                }
            }
            tabCache.put(creativeTab, tabStacks);
        }
        return tabStacks;
    }

}

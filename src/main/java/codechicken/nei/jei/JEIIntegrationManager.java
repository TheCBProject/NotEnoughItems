package codechicken.nei.jei;

import codechicken.lib.annotation.FunctionProxy;
import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.VisibilityData;
import codechicken.nei.jei.proxy.DummyProxy;
import codechicken.nei.jei.proxy.IJEIProxy;
import codechicken.nei.jei.proxy.JEIProxy;
import mezz.jei.Internal;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 7/14/2016.
 * Handles hiding and showing things from jei and nei. Basically an interface between the two.
 */
@SideOnly (Side.CLIENT)
public class JEIIntegrationManager {

    @FunctionProxy
    public static IJEIProxy proxy;

    public static boolean jeiLoaded;

    public static EnumItemBrowser searchBoxOwner = EnumItemBrowser.JEI;
    public static EnumItemBrowser itemPanelOwner = EnumItemBrowser.JEI;

    public static void pushChanges(VisibilityData data) {
        JeiRuntime runtime = Internal.getRuntime();
	    IngredientListOverlay overlay = runtime.getItemListOverlay();
        GuiTextFieldFilter fieldFilter = getTextFieldFilter(overlay);

        if (searchBoxOwner == EnumItemBrowser.JEI) {
            data.showSearchSection = false;
            if (fieldFilter != null) {
                fieldFilter.setVisible(true);
            }
        } else {
            data.showSearchSection = true;
            if (fieldFilter != null) {
                fieldFilter.setVisible(false);
            }
        }
        if (itemPanelOwner == EnumItemBrowser.JEI) {
            data.showItemPanel = false;
            if (!Config.isOverlayEnabled()) {
                Config.toggleOverlayEnabled();
            }
        } else {
            data.showItemPanel = data.showSearchSection = true;
            if (Config.isOverlayEnabled()) {
                Config.toggleOverlayEnabled();
            }
        }

    }

    public static void initConfig(ConfigTagParent tag) {

        setItemPanelOwner(tag.getTag("jei.itemPanel").getIntValue(1));
        setSearchBoxOwner(tag.getTag("jei.searchBox").getIntValue(1));

    }

    public static void openRecipeGui(ItemStack stack) {
        proxy.openRecipeGui(stack);
    }

    public static void openUsageGui(ItemStack stack) {
        proxy.openUsageGui(stack);
    }

    public static boolean isBlacklisted(ItemStack stack) {
        return proxy.isBlacklistedJEI(stack);
    }

    public static boolean setSearchBoxOwner(int ordinal) {
        try {
            searchBoxOwner = EnumItemBrowser.values()[ordinal];
            return true;
        } catch (IndexOutOfBoundsException e) {
            searchBoxOwner = EnumItemBrowser.NEI;
            return false;
        }
    }

    public static boolean setSearchBoxOwner(EnumItemBrowser browser) {
        searchBoxOwner = browser;
        return true;
    }

    public static boolean setItemPanelOwner(EnumItemBrowser browser) {
        itemPanelOwner = browser;
        return true;
    }

    public static boolean setItemPanelOwner(int ordinal) {
        try {
            itemPanelOwner = EnumItemBrowser.values()[ordinal];
            return true;
        } catch (IndexOutOfBoundsException e) {
            itemPanelOwner = EnumItemBrowser.NEI;
            return false;
        }
    }

    public static GuiTextFieldFilter getTextFieldFilter() {
        if (Internal.getRuntime() == null) {
            return null;
        }
        return getTextFieldFilter(Internal.getRuntime().getItemListOverlay());
    }

    private static GuiTextFieldFilter getTextFieldFilter(IngredientListOverlay overlay) {
        if (overlay == null) {
            return null;
        }
        try {
            Field field = overlay.getClass().getDeclaredField("searchField");
            field.setAccessible(true);
            return (GuiTextFieldFilter) field.get(overlay);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static IngredientFilter getItemFilter(GuiTextFieldFilter fieldFilter) {
        if (fieldFilter == null) {
            return null;
        }
        try {
            Field field = fieldFilter.getClass().getDeclaredField("ingredientFilter");
            field.setAccessible(true);
            return (IngredientFilter) field.get(fieldFilter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFilterText(String text) {
        if (Internal.getRuntime() != null && Internal.getRuntime().getItemListOverlay() != null) {
            IngredientListOverlay overlay = Internal.getRuntime().getItemListOverlay();
            overlay.setFilterText(text);
        }
    }

    public static List<Object> getFilteredItems() {
        if (Internal.getRuntime() != null && Internal.getRuntime().getItemListOverlay() != null) {
            IngredientListOverlay overlay = Internal.getRuntime().getItemListOverlay();
            IngredientFilter filter = getItemFilter(getTextFieldFilter(overlay));
            if (filter != null) {
                return filter.getFilteredIngredients();
            }
        }
        return new ArrayList<>();
    }

    public static KeyBinding getShowUses() {
        return KeyBindings.showUses;
    }

    public static KeyBinding getShowRecipes() {
        return KeyBindings.showRecipe;
    }

    public static KeyBinding getFocusSearch() {
        return KeyBindings.focusSearch;
    }

    public static KeyBinding getRecipeBack() {
        return KeyBindings.recipeBack;
    }

    public static KeyBinding getToggleOverlay() {
        return KeyBindings.toggleOverlay;
    }

    /**
     * Gracefully handles JEI integration breaking, sets SearchBox and ItemPanel back to defaults, disables further integration.
     *
     * @param throwable The error that occurred.
     */
    public static void handleJEIError(Throwable throwable) {
        //TODO
    }

    public static String proxyCallback() {
        if (Loader.isModLoaded("jei")) {
            jeiLoaded = true;
            return JEIProxy.class.getName();
        }
        jeiLoaded = false;
        return DummyProxy.class.getName();
    }

}

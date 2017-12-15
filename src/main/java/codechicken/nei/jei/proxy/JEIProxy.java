package codechicken.nei.jei.proxy;

import codechicken.nei.api.API;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.guihook.IContainerObjectHandler;
import codechicken.nei.handler.NEIClientEventHandler;
import codechicken.nei.util.LogHelper;
import mezz.jei.Internal;
import mezz.jei.api.*;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by covers1624 on 7/04/2017.
 */
public class JEIProxy implements IJEIProxy {

    private static IJeiHelpers helpers;

    private static Set<Rectangle> extraAreasCache;

    public JEIProxy() {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        API.registerNEIGuiHandler(new INEIGuiHandler() {
            @Override
            public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
                if (extraAreasCache == null) {
                    extraAreasCache = getExtraAreas(gui);
                }
                for (Rectangle rectangle : extraAreasCache) {
                    if (rectangle.intersects(x, y, w, h)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean isJEIEnabled() {
        return true;
    }

    @Override
    public void openRecipeGui(ItemStack stack) {
        RecipeRegistry registry = Internal.getRuntime().getRecipeRegistry();
        IFocus<ItemStack> focus = registry.createFocus(Mode.OUTPUT, stack);
        if (registry.getRecipeCategories(focus).isEmpty()) {
            return;
        }
        Internal.getRuntime().getRecipesGui().show(focus);
    }

    @Override
    public void openUsageGui(ItemStack stack) {
        RecipeRegistry registry = Internal.getRuntime().getRecipeRegistry();
        IFocus<ItemStack> focus = registry.createFocus(Mode.INPUT, stack);
        if (registry.getRecipeCategories(focus).isEmpty()) {
            return;
        }
        Internal.getRuntime().getRecipesGui().show(focus);
    }

    @Override
    public boolean isBlacklistedJEI(ItemStack stack) {
        return stack.isEmpty() || helpers.getIngredientBlacklist().isIngredientBlacklisted(stack);
    }

    @Override
    public Set<Rectangle> getExtraAreas(GuiContainer container) {
        try {
            JeiRuntime runtime = Internal.getRuntime();
            if (runtime != null) {
                Set<Rectangle> rectangles = new HashSet<>();
                for (IAdvancedGuiHandler<GuiContainer> handler : runtime.getActiveAdvancedGuiHandlers(container)) {
                    List<Rectangle> ret = handler.getGuiExtraAreas(container);
                    if (ret != null) {
                        rectangles.addAll(ret);
                    }
                }
                return rectangles;
            }

        } catch (Throwable e) {
            LogHelper.errorOnce(e, "ExtraAreas", "Error thrown whilst accessing JEI internals!");
        }
        return null;
    }

    @JEIPlugin
    public static class Plugin implements IModPlugin {

        @Override
        public void register(IModRegistry registry) {
            helpers = registry.getJeiHelpers();
        }
    }

    public static class EventHandler {

        @SubscribeEvent
        public void tickEvent(ClientTickEvent event) {
            if (event.phase == Phase.END) {
                extraAreasCache = null;
            }
        }
    }
}

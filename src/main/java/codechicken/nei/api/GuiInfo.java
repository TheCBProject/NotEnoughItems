package codechicken.nei.api;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;

import java.util.HashSet;
import java.util.LinkedList;

public class GuiInfo {

    public static LinkedList<INEIGuiHandler> guiHandlers = new LinkedList<>();
    public static HashSet<Class<? extends GuiContainer>> customSlotGuis = new HashSet<>();

    public static void load() {
        customSlotGuis.add(GuiContainerCreative.class);
    }

    public static void clearGuiHandlers() {
        guiHandlers.removeIf(ineiGuiHandler -> ineiGuiHandler instanceof GuiContainer);
    }

    public static boolean hasCustomSlots(GuiContainer gui) {
        return customSlotGuis.contains(gui.getClass());
    }
}

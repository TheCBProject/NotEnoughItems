package codechicken.nei.recipe;

import codechicken.nei.OffsetPositioner;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IStackPositioner;
import com.google.common.base.Objects;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.HashMap;

public class RecipeInfo {

    private static class OverlayKey {

        String ident;
        Class<? extends GuiContainer> guiClass;

        public OverlayKey(Class<? extends GuiContainer> classz, String ident) {
            this.guiClass = classz;
            this.ident = ident;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OverlayKey)) {
                return false;
            }
            OverlayKey key = (OverlayKey) obj;
            return Objects.equal(ident, key.ident) && guiClass == key.guiClass;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(ident, guiClass);
        }
    }

    static HashMap<OverlayKey, IOverlayHandler> overlayMap = new HashMap<>();
    static HashMap<OverlayKey, IStackPositioner> positionerMap = new HashMap<>();
    static HashMap<Class<? extends GuiContainer>, int[]> offsets = new HashMap<>();

    public static void registerOverlayHandler(Class<? extends GuiContainer> classz, IOverlayHandler handler, String ident) {
        overlayMap.put(new OverlayKey(classz, ident), handler);
    }

    public static void registerGuiOverlay(Class<? extends GuiContainer> classz, String ident, IStackPositioner positioner) {
        positionerMap.put(new OverlayKey(classz, ident), positioner);
        if (positioner instanceof OffsetPositioner && !offsets.containsKey(classz)) {
            OffsetPositioner p = (OffsetPositioner) positioner;
            setGuiOffset(classz, p.offsetX, p.offsetY);
        }
    }

    public static void setGuiOffset(Class<? extends GuiContainer> classz, int x, int y) {
        offsets.put(classz, new int[] { x, y });
    }

    public static boolean hasDefaultOverlay(GuiContainer gui, String ident) {
        return positionerMap.containsKey(new OverlayKey(gui.getClass(), ident));
    }

    public static boolean hasOverlayHandler(GuiContainer gui, String ident) {
        return overlayMap.containsKey(new OverlayKey(gui.getClass(), ident));
    }

    public static IOverlayHandler getOverlayHandler(GuiContainer gui, String ident) {
        return overlayMap.get(new OverlayKey(gui.getClass(), ident));
    }

    public static IStackPositioner getStackPositioner(GuiContainer gui, String ident) {
        return positionerMap.get(new OverlayKey(gui.getClass(), ident));
    }

    public static int[] getGuiOffset(GuiContainer gui) {
        int[] offset = offsets.get(gui.getClass());
        return offset == null ? new int[] { 5, 11 } : offset;
    }
}

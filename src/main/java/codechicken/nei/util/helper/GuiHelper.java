package codechicken.nei.util.helper;

import codechicken.lib.util.ClientUtils;
import codechicken.nei.guihook.IContainerObjectHandler;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.handler.NEIClientEventHandler;
import codechicken.nei.util.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static codechicken.lib.gui.GuiDraw.fontRenderer;
import static codechicken.lib.gui.GuiDraw.getMousePosition;

/**
 * Created by covers1624 on 31/03/2017.
 */
public class GuiHelper {

    private static int modelviewDepth = -1;

    public static ItemStack getStackMouseOver(GuiContainer window, boolean useContainerSlots) {
        Point mousePos = getMousePosition();

        for (IContainerObjectHandler objectHandler : NEIClientEventHandler.objectHandlers) {
            ItemStack item = objectHandler.getStackUnderMouse(window, mousePos.x, mousePos.y);
            if (!item.isEmpty()) {
                return item;
            }
        }
        if (useContainerSlots) {
            Slot slot = getSlotMouseOver(window);
            if (slot != null) {
                return slot.getStack();
            }
        }

        return ItemStack.EMPTY;
    }

    public static Slot getSlotMouseOver(GuiContainer window) {
        Point mousePos = getMousePosition();
        if (objectUnderMouse(window, mousePos.x, mousePos.y)) {
            return null;
        }

        return window.getSlotAtPosition(mousePos.x, mousePos.y);
    }

    /**
     * Returns true if there is an object of yours obscuring the slot that the mouse would otherwise be hovering over.
     */
    public static boolean objectUnderMouse(GuiContainer container, int mousex, int mousey) {

        for (IContainerObjectHandler objectHandler : NEIClientEventHandler.objectHandlers) {
            if (objectHandler.objectUnderMouse(container, mousex, mousey)) {
                return true;
            }
        }

        return false;
    }

    public static void clickSlot(GuiContainer window, int slotIndex, int button, ClickType clickType) {
        window.mc.playerController.windowClick(window.inventorySlots.windowId, slotIndex, button, clickType, window.mc.player);
    }

    public static boolean shouldShowTooltip(GuiScreen window) {
        if (!ClientUtils.inWorld()) {
            return false;
        }
        for (IContainerObjectHandler handler : NEIClientEventHandler.objectHandlers) {
            if (!handler.shouldShowTooltip(window)) {
                return false;
            }
        }

        return Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty();
    }

    public static void enable3DRender() {
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    public static void enable2DRender() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
    }

    public static void enableMatrixStackLogging() {
        modelviewDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
    }

    public static void disableMatrixStackLogging() {
        modelviewDepth = -1;
    }

    public static boolean checkMatrixStack() {
        return modelviewDepth < 0 || GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) == modelviewDepth;
    }

    public static void restoreMatrixStack() {
        if (modelviewDepth >= 0) {
            for (int i = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH); i > modelviewDepth; i--) {
                GlStateManager.popMatrix();
            }
        }
    }

    public static RenderItem getRenderItem() {
        return Minecraft.getMinecraft().getRenderItem();
    }

    public static FontRenderer getFontRenderer(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() != null) {
            FontRenderer f = stack.getItem().getFontRenderer(stack);
            if (f != null) {
                return f;
            }
        }
        return fontRenderer;
    }

    public static void drawItem(int i, int j, ItemStack itemstack) {
        drawItem(i, j, itemstack, getFontRenderer(itemstack));
    }

    public static void drawItem(int i, int j, ItemStack itemstack, FontRenderer fontRenderer) {
        enable3DRender();
        RenderItem drawItems = getRenderItem();
        float zLevel = drawItems.zLevel += 100F;
        try {
            drawItems.renderItemAndEffectIntoGUI(itemstack, i, j);
            drawItems.renderItemOverlays(fontRenderer, itemstack, i, j);

            if (!checkMatrixStack()) {
                throw new IllegalStateException("Modelview matrix stack too deep");
            }
            if (Tessellator.getInstance().getBuffer().isDrawing) {
                throw new IllegalStateException("Still drawing");
            }
        } catch (Exception e) {
            LogHelper.errorOnce(e, itemstack.toString(), "Error whilst rendering: " + itemstack);

            restoreMatrixStack();
            if (Tessellator.getInstance().getBuffer().isDrawing) {
                Tessellator.getInstance().draw();
            }

            drawItems.zLevel = zLevel;
            drawItems.renderItemIntoGUI(new ItemStack(Blocks.STONE), i, j);
        }

        enable2DRender();
        drawItems.zLevel = zLevel - 100;
    }

    /**
     * Extra lines are often used for more information. For example enchantments, potion effects and mob spawner contents.
     *
     * @param itemstack       The item to get the name for.
     * @param gui             An instance of the currentscreen passed to tooltip handlers. If null, only gui inspecific handlers should respond
     * @param includeHandlers If true tooltip handlers will add to the item tip
     * @return A list of Strings representing the text to be displayed on each line of the tool tip.
     */
    public static List<String> itemDisplayNameMultiline(ItemStack itemstack, GuiContainer gui, boolean includeHandlers) {
        List<String> namelist = null;
        try {
            namelist = itemstack.getTooltip(Minecraft.getMinecraft().player, Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL);
        } catch (Throwable ignored) {
        }

        if (namelist == null) {
            namelist = new ArrayList<>();
        }

        if (namelist.size() == 0) {
            namelist.add("Unnamed");
        }

        if (namelist.get(0) == null || namelist.get(0).equals("")) {
            namelist.set(0, "Unnamed");
        }

        if (includeHandlers) {
            for (IContainerTooltipHandler handler : NEIClientEventHandler.tooltipHandlers) {
                handler.handleItemDisplayName(gui, itemstack, namelist);
            }
        }

        namelist.set(0, itemstack.getRarity().rarityColor.toString() + namelist.get(0));
        for (int i = 1; i < namelist.size(); i++) {
            namelist.set(i, "\u00a77" + namelist.get(i));
        }

        return namelist;
    }

    /**
     * The general name of this item.
     *
     * @param itemstack The {@link ItemStack} to get the name for.
     * @return The first line of the multiline display name.
     */
    public static String itemDisplayNameShort(ItemStack itemstack) {
        List<String> list = itemDisplayNameMultiline(itemstack, null, false);
        return list.get(0);
    }

    /**
     * Concatenates the multiline display name into one line for easy searching using string and {@link Pattern} functions.
     *
     * @param itemstack The stack to get the name for
     * @return The multiline display name of this item separated by '#'
     */
    public static String concatenatedDisplayName(ItemStack itemstack, boolean includeHandlers) {
        List<String> list = itemDisplayNameMultiline(itemstack, null, includeHandlers);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String name : list) {
            if (first) {
                first = false;
            } else {
                sb.append("#");
            }
            sb.append(name);
        }
        return TextFormatting.getTextWithoutFormattingCodes(sb.toString());
    }

}

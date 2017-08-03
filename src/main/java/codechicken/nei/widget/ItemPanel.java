package codechicken.nei.widget;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.config.KeyBindings;
import codechicken.nei.jei.JEIIntegrationManager;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.NEIClientUtils;
import codechicken.nei.util.NEIServerUtils;
import codechicken.nei.util.helper.GuiHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;

import static codechicken.lib.gui.GuiDraw.drawRect;

public class ItemPanel extends Widget {

    /**
     * Should not be externally modified, use updateItemList
     */
    public static ArrayList<ItemStack> items = new ArrayList<>();
    /**
     * Swapped into visible items on update
     */
    private static ArrayList<ItemStack> _items = items;

    public static void updateItemList(ArrayList<ItemStack> newItems) {
        _items = newItems;
    }

    public class ItemPanelSlot {

        public ItemStack item;
        public int slotIndex;

        public ItemPanelSlot(int index) {
            item = items.get(index);
            slotIndex = index;
        }
    }

    public ItemStack draggedStack = ItemStack.EMPTY;
    public int mouseDownSlot = -1;

    private int marginLeft;
    private int marginTop;
    private int rows;
    private int columns;

    private boolean[] validSlotMap;
    private int firstIndex;
    private int itemsPerPage;

    private int page;
    private int numPages;

    public void resize() {
        items = _items;

        marginLeft = x + (w % 18) / 2;
        marginTop = y + (h % 18) / 2;
        columns = w / 18;
        rows = h / 18;
        //sometimes width and height can be negative with certain resizing
        if (rows < 0) {
            rows = 0;
        }
        if (columns < 0) {
            columns = 0;
        }

        calculatePage();
        updateValidSlots();
    }

    private void calculatePage() {
        if (itemsPerPage == 0) {
            numPages = 0;
        } else {
            numPages = (int) Math.ceil((float) items.size() / (float) itemsPerPage);
        }

        if (firstIndex >= items.size()) {
            firstIndex = 0;
        }

        if (numPages == 0) {
            page = 0;
        } else {
            page = firstIndex / itemsPerPage + 1;
        }
    }

    private void updateValidSlots() {
        GuiContainer gui = NEIClientUtils.getGuiContainer();
        validSlotMap = new boolean[rows * columns];
        itemsPerPage = 0;
        for (int i = 0; i < validSlotMap.length; i++) {
            if (slotValid(gui, i)) {
                validSlotMap[i] = true;
                itemsPerPage++;
            }
        }
    }

    private boolean slotValid(GuiContainer gui, int i) {
        Rectangle4i rect = getSlotRect(i);
        for (INEIGuiHandler handler : GuiInfo.guiHandlers) {
            if (handler.hideItemPanelSlot(gui, rect.x, rect.y, rect.w, rect.h)) {
                return false;
            }
        }
        return true;
    }

    public Rectangle4i getSlotRect(int i) {
        return getSlotRect(i / columns, i % columns);
    }

    public Rectangle4i getSlotRect(int row, int column) {
        return new Rectangle4i(marginLeft + column * 18, marginTop + row * 18, 18, 18);
    }

    @Override
    public void draw(int mousex, int mousey) {
        if (itemsPerPage == 0) {
            return;
        }

        GuiHelper.enableMatrixStackLogging();
        int index = firstIndex;
        for (int i = 0; i < rows * columns && index < items.size(); i++) {
            if (validSlotMap[i]) {
                Rectangle4i rect = getSlotRect(i);
                if (rect.contains(mousex, mousey)) {
                    drawRect(rect.x, rect.y, rect.w, rect.h, 0xee555555);//highlight
                }

                GuiHelper.drawItem(rect.x + 1, rect.y + 1, items.get(index));

                index++;
            }
        }
        GuiHelper.disableMatrixStackLogging();
    }

    @Override
    public void postDraw(int mousex, int mousey) {
        if (!draggedStack.isEmpty()) {
            RenderItem drawItems = GuiHelper.getRenderItem();
            drawItems.zLevel += 100;
            GuiHelper.drawItem(mousex - 8, mousey - 8, draggedStack);
            drawItems.zLevel -= 100;
        }
    }

    @Override
    public void mouseDragged(int mousex, int mousey, int button, long heldTime) {
        if (mouseDownSlot >= 0 && draggedStack.isEmpty() && NEIClientUtils.getHeldItem().isEmpty() && NEIClientConfig.hasSMPCounterPart() && !GuiInfo.hasCustomSlots(NEIClientUtils.getGuiContainer())) {
            ItemPanelSlot mouseOverSlot = getSlotMouseOver(mousex, mousey);
            ItemStack stack = new ItemPanelSlot(mouseDownSlot).item;
            if (!stack.isEmpty() && (mouseOverSlot == null || mouseOverSlot.slotIndex != mouseDownSlot || heldTime > 500)) {
                int amount = NEIClientConfig.getItemQuantity();
                if (amount == 0) {
                    amount = stack.getMaxStackSize();
                }

                draggedStack = NEIServerUtils.copyStack(stack, amount);
            }
        }
    }

    @Override
    public boolean handleClick(int mousex, int mousey, int button) {
        if (handleDraggedClick(mousex, mousey, button)) {
            return true;
        }

        if (!NEIClientUtils.getHeldItem().isEmpty()) {
            for (INEIGuiHandler handler : GuiInfo.guiHandlers) {
                if (handler.hideItemPanelSlot(NEIClientUtils.getGuiContainer(), mousex, mousey, 1, 1)) {
                    return false;
                }
            }

            if (NEIClientConfig.canPerformAction("delete") && NEIClientConfig.canPerformAction("item")) {
                if (button == 1) {
                    NEIClientUtils.decreaseSlotStack(-999);
                } else {
                    NEIClientUtils.deleteHeldItem();
                }
            } else {
                NEIClientUtils.dropHeldItem();
            }

            return true;
        }

        ItemPanelSlot hoverSlot = getSlotMouseOver(mousex, mousey);
        if (hoverSlot != null) {
            if (button == 2) {
                ItemStack stack = hoverSlot.item;
                if (!stack.isEmpty()) {
                    int amount = NEIClientConfig.getItemQuantity();
                    if (amount == 0) {
                        amount = stack.getMaxStackSize();
                    }

                    draggedStack = NEIServerUtils.copyStack(stack, amount);
                }
            } else {
                mouseDownSlot = hoverSlot.slotIndex;
            }
            return true;
        }
        return false;
    }

    private boolean handleDraggedClick(int mousex, int mousey, int button) {
        if (draggedStack.isEmpty()) {
            return false;
        }

        GuiContainer gui = NEIClientUtils.getGuiContainer();
        boolean handled = false;
        for (INEIGuiHandler handler : GuiInfo.guiHandlers) {
            if (handler.handleDragNDrop(gui, mousex, mousey, draggedStack, button)) {
                handled = true;
                if (draggedStack.getCount() == 0) {
                    draggedStack = ItemStack.EMPTY;
                    return true;
                }
            }
        }

        if (handled) {
            return true;
        }

        Slot overSlot = gui.getSlotAtPosition(mousex, mousey);
        if (overSlot != null && overSlot.isItemValid(draggedStack)) {
            if (NEIClientConfig.canCheatItem(draggedStack)) {
                int contents = overSlot.getHasStack() ? overSlot.getStack().getCount() : 0;
                int add = button == 0 ? draggedStack.getCount() : 1;
                if (overSlot.getHasStack() && !NEIServerUtils.areStacksSameType(draggedStack, overSlot.getStack())) {
                    contents = 0;
                }
                int total = Math.min(contents + add, Math.min(overSlot.getSlotStackLimit(), draggedStack.getMaxStackSize()));

                if (total > contents) {
                    int slotNumber = overSlot.slotNumber;
                    if (gui instanceof GuiContainerCreative) {
                        //Because Mojang..
                        slotNumber = slotNumber - gui.inventorySlots.inventorySlots.size() + 9 + 36;
                    }
                    NEIClientUtils.setSlotContents(slotNumber, NEIServerUtils.copyStack(draggedStack, total), true);
                    NEIClientPacketHandler.sendGiveItem(NEIServerUtils.copyStack(draggedStack, total), false, false);
                    draggedStack.shrink(total - contents);
                }
                if (draggedStack.getCount() == 0) {
                    draggedStack = ItemStack.EMPTY;
                }
            } else {
                draggedStack = ItemStack.EMPTY;
            }
        } else if (mousex < gui.getGuiLeft() || mousey < gui.getGuiTop() || mousex >= gui.getGuiLeft() + gui.getXSize() || mousey >= gui.getGuiTop() + gui.getYSize()) {
            draggedStack = ItemStack.EMPTY;
        }

        return true;
    }

    @Override
    public boolean handleClickExt(int mousex, int mousey, int button) {
        return handleDraggedClick(mousex, mousey, button);
    }

    @Override
    public void mouseUp(int mousex, int mousey, int button) {
        ItemPanelSlot hoverSlot = getSlotMouseOver(mousex, mousey);
        if (hoverSlot != null && hoverSlot.slotIndex == mouseDownSlot && draggedStack.isEmpty()) {
            ItemStack item = hoverSlot.item;
            if (!NEIClientConfig.canCheatItem(item)) {
                if (button == 0) {
                    JEIIntegrationManager.openRecipeGui(item);
                } else if (button == 1) {
                    JEIIntegrationManager.openUsageGui(item);
                }

                draggedStack = ItemStack.EMPTY;
                mouseDownSlot = -1;
                return;
            }

            NEIClientUtils.cheatItem(item, button, -1);
        }

        mouseDownSlot = -1;
    }

    @Override
    public boolean onMouseWheel(int i, int mousex, int mousey) {
        if (!contains(mousex, mousey)) {
            return false;
        }

        scroll(-i);
        return true;
    }

    @Override
    public boolean handleKeyPress(int keyID, char keyChar) {
        if (KeyBindings.get("nei.options.keys.gui.next").isActiveAndMatches(keyID)) {
            scroll(1);
            return true;
        }
        if (KeyBindings.get("nei.options.keys.gui.prev").isActiveAndMatches(keyID)) {
            scroll(-1);
            return true;
        }

        Point mouse = GuiDraw.getMousePosition();
        ItemPanelSlot hoverSlot = getSlotMouseOver(mouse.x, mouse.y);
        if (hoverSlot != null && draggedStack.isEmpty()) {
            ItemStack item = hoverSlot.item;
            if (KeyBindings.get("nei.options.keys.gui.recipe").isActiveAndMatches(keyID)) {
                JEIIntegrationManager.openRecipeGui(item);
                return true;
            } else if (KeyBindings.get("nei.options.keys.gui.usage").isActiveAndMatches(keyID)) {
                JEIIntegrationManager.openUsageGui(item);
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack getStackMouseOver(int mousex, int mousey) {
        ItemPanelSlot slot = getSlotMouseOver(mousex, mousey);
        return slot == null ? ItemStack.EMPTY : slot.item;
    }

    public ItemPanelSlot getSlotMouseOver(int mousex, int mousey) {
        int index = firstIndex;
        for (int i = 0; i < rows * columns && index < items.size(); i++) {
            if (validSlotMap[i]) {
                if (getSlotRect(i).contains(mousex, mousey)) {
                    return new ItemPanelSlot(index);
                }
                index++;
            }
        }

        return null;
    }

    public void scroll(int i) {
        if (itemsPerPage != 0) {
            int oldIndex = firstIndex;
            firstIndex += i * itemsPerPage;
            if (firstIndex >= items.size()) {
                firstIndex = 0;
            }
            if (firstIndex < 0) {
                if (oldIndex > 0) {
                    firstIndex = 0;
                } else {
                    firstIndex = (items.size() - 1) / itemsPerPage * itemsPerPage;
                }
            }

            calculatePage();
        }
    }

    public int getPage() {
        return page;
    }

    public int getNumPages() {
        return numPages;
    }

    @Override
    public boolean contains(int px, int py) {
        GuiContainer gui = NEIClientUtils.getGuiContainer();
        Rectangle4i rect = new Rectangle4i(px, py, 1, 1);
        for (INEIGuiHandler handler : GuiInfo.guiHandlers) {
            if (handler.hideItemPanelSlot(gui, rect.x, rect.y, rect.w, rect.h)) {
                return false;
            }
        }

        return super.contains(px, py);
    }
}

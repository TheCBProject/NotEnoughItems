package codechicken.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.guihook.IInputHandler;
import codechicken.nei.handler.FastTransferManager;
import codechicken.nei.handler.NEIEventHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.ItemInfo;
import codechicken.nei.util.NEIClientUtils;
import codechicken.nei.util.helper.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.awt.*;

import static codechicken.lib.gui.GuiDraw.getMousePosition;

public class NEIController implements /*IContainerSlotClickHandler,*/ IInputHandler {

    private static NEIController instance = new NEIController();

    public static FastTransferManager fastTransferManager;

    private static boolean deleteMode;
    private static int pickedUpFromSlot;

    private static int selectedItem;
    private ItemStack firstheld;

    public static void load() {
        //GuiContainerManager.addSlotClickHandler(instance);
        NEIEventHandler.addInputHandler(instance);
    }

    public static void load(GuiContainer gui) {
        deleteMode = false;
        GuiInfo.clearGuiHandlers();
        fastTransferManager = null;
        if (!NEIClientConfig.isEnabled()) {
            return;
        }

        fastTransferManager = new FastTransferManager();
        if (gui instanceof INEIGuiHandler) {
            API.registerNEIGuiHandler((INEIGuiHandler) gui);
        }
    }

    public static boolean isSpreading(GuiContainer gui) {
        return gui.dragSplitting && gui.dragSplittingSlots.size() > 1;
    }

    public static void processCreativeCycling(InventoryPlayer inventory) {
        if (NEIClientConfig.invCreativeMode() && NEIClientUtils.controlKey()) {
            if (selectedItem != inventory.currentItem) {
                if (inventory.currentItem == selectedItem + 1 || (inventory.currentItem == 0 && selectedItem == 8))//foward
                {
                    NEIClientPacketHandler.sendCreativeScroll(1);
                    inventory.currentItem = selectedItem;
                } else if (inventory.currentItem == selectedItem - 1 || (inventory.currentItem == 8 && selectedItem == 0)) {
                    NEIClientPacketHandler.sendCreativeScroll(-1);
                    inventory.currentItem = selectedItem;
                }
            }
        }

        selectedItem = inventory.currentItem;
    }

    //TODO Fast transfer.
    /*@Override
    public void beforeSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, ClickType clickType) {
        if (!NEIClientConfig.isEnabled()) {
            return;
        }

        firstheld = NEIClientUtils.getHeldItem();
    }

    @Override
    public boolean handleSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, ClickType clickType, boolean eventConsumed) {
        if (eventConsumed || !NEIClientConfig.isEnabled() || isSpreading(gui)) {
            return eventConsumed;
        }

        if (deleteMode && slotIndex >= 0 && slot != null) {
            if (NEIClientUtils.shiftKey() && button == 0) {
                ItemStack itemstack1 = slot.getStack();
                if (itemstack1 != null) {
                    NEIClientUtils.deleteItemsOfType(itemstack1);
                }

            } else if (button == 1) {
                NEIClientUtils.decreaseSlotStack(slot.slotNumber);
            } else {
                NEIClientUtils.deleteSlotStack(slot.slotNumber);
            }
            return true;
        }

        if (button == 1 && slot instanceof SlotCrafting)//right click
        {
            for (int i1 = 0; i1 < 64; i1++)//click this slot 64 times
            {
                manager.handleSlotClick(slot.slotNumber, button, ClickType.PICKUP);
            }
            return true;
        }

        if (NEIClientUtils.controlKey() && slot != null && !slot.getStack().isEmpty() && slot.isItemValid(slot.getStack())) {
            NEIClientUtils.cheatItem(slot.getStack(), button, 1);
            return true;
        }

        if (GuiInfo.hasCustomSlots(gui)) {
            return false;
        }

        if (slotIndex >= 0 && NEIClientUtils.shiftKey() && NEIClientUtils.getHeldItem() != null && !slot.getHasStack()) {
            ItemStack held = NEIClientUtils.getHeldItem();
            manager.handleSlotClick(slot.slotNumber, button, ClickType.PICKUP);
            if (slot.isItemValid(held) && !ItemInfo.fastTransferExemptions.contains(slot.getClass()) && !ItemInfo.fastTransferContainerExemptions.contains(gui.getClass())) {
                fastTransferManager.performMassTransfer(gui, pickedUpFromSlot, slotIndex, held);
            }

            return true;
        }

        if (slotIndex == -999 && NEIClientUtils.shiftKey() && button == 0 && !ItemInfo.fastTransferContainerExemptions.contains(gui.getClass())) {
            fastTransferManager.throwAll(gui, pickedUpFromSlot);
            return true;
        }

        return false;
    }

    @Override
    public void afterSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, ClickType clickType) {
        if (!NEIClientConfig.isEnabled()) {
            return;
        }

        ItemStack nowHeld = NEIClientUtils.getHeldItem();

        if (firstheld != nowHeld) {
            pickedUpFromSlot = slotIndex;
        }
    }*/

    @Override
    public boolean lastKeyTyped(GuiScreen gui, char keyChar, int keyCode) {
        if (gui instanceof GuiContainer) {
            GuiContainer container = ((GuiContainer) gui);
            if (!NEIClientConfig.isEnabled() || GuiInfo.hasCustomSlots(container) || isSpreading(container)) {
                return false;
            }

            Slot slot = GuiHelper.getSlotMouseOver(container);
            if (slot == null) {
                return false;
            }

            int slotIndex = slot.slotNumber;

            if (keyCode == Minecraft.getMinecraft().gameSettings.keyBindDrop.getKeyCode() && NEIClientUtils.shiftKey() && !ItemInfo.fastTransferContainerExemptions.contains(container.getClass())) {
                FastTransferManager.clickSlot(container, slotIndex);
                fastTransferManager.throwAll(container, slotIndex);
                FastTransferManager.clickSlot(container, slotIndex);

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(GuiScreen gui, int mousex, int mousey, int scrolled) {
        if (gui instanceof GuiContainer) {
            GuiContainer container = ((GuiContainer) gui);
            if (!NEIClientConfig.isEnabled() || GuiInfo.hasCustomSlots(container)) {
                return false;
            }

            Point mousePos = getMousePosition();
            Slot mouseover = container.getSlotAtPosition(mousePos.x, mousePos.y);
            if (mouseover != null && mouseover.getHasStack() && !ItemInfo.fastTransferContainerExemptions.contains(container.getClass())) {
                if (scrolled > 0) {
                    fastTransferManager.transferItem(container, mouseover.slotNumber);
                } else {
                    fastTransferManager.retrieveItem(container, mouseover.slotNumber);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(GuiScreen gui, char keyChar, int keyCode) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiScreen gui, int mousex, int mousey, int button) {
        return false;
    }

    @Override
    public void onKeyTyped(GuiScreen gui, char keyChar, int keyID) {
    }

    @Override
    public void onMouseClicked(GuiScreen gui, int mousex, int mousey, int button) {
    }

    @Override
    public void onMouseDragged(GuiScreen gui, int mousex, int mousey, int button, long heldTime) {
    }

    @Override
    public void onMouseUp(GuiScreen gui, int mousex, int mousey, int button) {
    }

    public static boolean canUseDeleteMode() {
        return !(NEIClientUtils.getGuiContainer() instanceof GuiContainerCreative);
    }

    public static void toggleDeleteMode() {
        if (canUseDeleteMode()) {
            deleteMode = !deleteMode;
        }
    }

    public static boolean getDeleteMode() {
        return deleteMode;
    }
}

package codechicken.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.guihook.IInputHandler;
import codechicken.nei.handler.FastTransferManager;
import codechicken.nei.handler.NEIClientEventHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.ItemInfo;
import codechicken.nei.util.LogHelper;
import codechicken.nei.util.NEIClientUtils;
import codechicken.nei.util.helper.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.awt.*;

import static codechicken.lib.gui.GuiDraw.getMousePosition;

public class NEIController implements IInputHandler {

    private static NEIController instance = new NEIController();

    public static FastTransferManager fastTransferManager;

    private static boolean deleteMode;
    private static int pickedUpFromSlot;

    private static int selectedItem;
    private ItemStack heldTracker;

    public static void load() {
        NEIClientEventHandler.addInputHandler(instance);
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
    public boolean mouseScrolled(GuiScreen gui, int mouseX, int mouseY, int scrolled) {
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
    public void onMouseClicked(GuiScreen gui, int mouseX, int mouseY, int button) {
        if (gui instanceof GuiContainer) {
            Slot slot = GuiHelper.getSlotMouseOver(((GuiContainer) gui));
            if (slot != null) {
                //Holding tracker.
                heldTracker = NEIClientUtils.getHeldItem();
            }
        }
    }

    @Override
    public boolean mouseClicked(GuiScreen screen, int mouseX, int mouseY, int button) {

        if (!(screen instanceof GuiContainer) || !NEIClientConfig.isEnabled() || isSpreading(((GuiContainer) screen))) {
            return false;
        }
        GuiContainer gui = ((GuiContainer) screen);
        Slot slot = GuiHelper.getSlotMouseOver(gui);
        int slotID = -1;

        boolean outsideGui = (mouseX < gui.getGuiLeft() || mouseY < gui.getGuiTop() || mouseX >= gui.getGuiLeft() + gui.getXSize() || mouseY >= gui.getGuiTop() + gui.getYSize()) && slot == null;

        if (slot != null) {
            slotID = slot.slotNumber;
        }

        if (outsideGui) {
            slotID = -999;
        }


        //Shift / Click delete.
        if (deleteMode && slot != null && slot.slotNumber >= 0) {
            if (NEIClientUtils.shiftKey() && button == 0) {
                ItemStack itemstack1 = slot.getStack();
                if (!itemstack1.isEmpty()) {
                    NEIClientUtils.deleteItemsOfType(itemstack1);
                }

            } else if (button == 1) {
                NEIClientUtils.decreaseSlotStack(slot.slotNumber);
            } else {
                NEIClientUtils.deleteSlotStack(slot.slotNumber);
            }
            return true;
        }

        //Creative click the slot 64 times because mojang.
        //Seems to already be a thing.. Gonna leave this here and wipe it in a cleanup pass.
        //if (button == 1 && slot instanceof SlotCrafting)//right click
        //{
        //    for (int i1 = 0; i1 < 64; i1++)//click this slot 64 times
        //    {
        //        manager.handleSlotClick(slot.slotNumber, button, ClickType.PICKUP);
        //    }
        //    return true;
        //}

        //Control click slot = give me an item.
        if (NEIClientUtils.controlKey() && slot != null && !slot.getStack().isEmpty() && slot.isItemValid(slot.getStack())) {
            NEIClientUtils.cheatItem(slot.getStack(), button, 1);
            return true;
        }

        //Custom slots or container? No thanx, bia!
        if (GuiInfo.hasCustomSlots(gui) || ItemInfo.fastTransferContainerExemptions.contains(gui.getClass())) {
            return false;
        }
        //Disabled for now.
        //if (slotID >= 0 && NEIClientUtils.shiftKey() && !NEIClientUtils.getHeldItem().isEmpty() && !slot.getHasStack()) {
        //    ItemStack held = NEIClientUtils.getHeldItem();
        //    GuiHelper.clickSlot(container, slot.slotNumber, button, ClickType.PICKUP);
        //    if (slot.isItemValid(held)) {
        //        fastTransferManager.performMassTransfer(container, pickedUpFromSlot, slot.slotNumber, held);
        //    }
        //
        //    return true;
        //}

        if (slotID == -999 && NEIClientUtils.shiftKey() && button == 0 && !NEIClientUtils.getHeldItem().isEmpty()) {
            fastTransferManager.throwAll(gui, pickedUpFromSlot);
            return true;
        }

        return false;
    }

    @Override
    public void onMouseClickedPost(GuiScreen gui, int mouseX, int mouseY, int button) {
        if (!(gui instanceof GuiContainer) || !NEIClientConfig.isEnabled()) {
            return;
        }
        Slot slot = GuiHelper.getSlotMouseOver(((GuiContainer) gui));
        if (slot != null) {

            ItemStack nowHeld = NEIClientUtils.getHeldItem();

            if (heldTracker != nowHeld) {
                pickedUpFromSlot = slot.slotNumber;
            }
        }
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

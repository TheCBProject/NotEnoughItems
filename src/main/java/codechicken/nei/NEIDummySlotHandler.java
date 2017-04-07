package codechicken.nei;

import codechicken.lib.inventory.container.ContainerExtended;
import codechicken.lib.inventory.container.SlotDummy;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.NEIClientUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class NEIDummySlotHandler implements INEIGuiHandler {

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        Slot slot = gui.getSlotAtPosition(mouseX, mouseY);
        if (slot instanceof SlotDummy && slot.isItemValid(draggedStack) && gui.inventorySlots instanceof ContainerExtended) {
            ((SlotDummy) slot).slotClick(draggedStack, button, NEIClientUtils.shiftKey());
            NEIClientPacketHandler.sendDummySlotSet(slot.slotNumber, slot.getStack());
            return true;
        }
        return false;
    }
}

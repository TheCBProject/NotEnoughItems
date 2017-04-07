package codechicken.nei.handler;

import codechicken.lib.util.ItemUtils;
import codechicken.nei.api.IInfiniteItemHandler;
import codechicken.nei.util.NEIServerUtils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class InfiniteStackSizeHandler implements IInfiniteItemHandler {

    @Override
    public void onPickup(ItemStack heldItem) {
        heldItem.setCount(1);
    }

    @Override
    public void onPlaceInfinite(ItemStack heldItem) {
        heldItem.setCount(111);
    }

    @Override
    public boolean canHandleItem(ItemStack stack) {
        return !stack.isItemStackDamageable();
    }

    @Override
    public boolean isItemInfinite(ItemStack stack) {
        return stack.getCount() == -1 || stack.getCount() > 100;
    }

    @Override
    public void replenishInfiniteStack(InventoryPlayer inv, int slotNo) {
        ItemStack stack = inv.getStackInSlot(slotNo);
        stack.setCount(111);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (i == slotNo) {
                continue;
            }

            if (NEIServerUtils.areStacksSameType(stack, inv.getStackInSlot(i))) {
                inv.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    public ItemStack getInfiniteItem(ItemStack typeStack) {
        return ItemUtils.copyStack(typeStack, -1);
    }
}

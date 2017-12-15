package codechicken.nei.util;

import codechicken.lib.inventory.InventoryRange;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.util.LangProxy;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.widget.action.NEIActions;
import com.google.common.collect.Iterables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

import static codechicken.nei.NEIClientConfig.*;

public class NEIClientUtils extends NEIServerUtils {

    public static LangProxy lang = new LangProxy("nei");

    private static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static String translate(String key, Object... params) {
        return lang.format(key, params);
    }

    public static void printChatMessage(ITextComponent msg) {
        if (mc().ingameGUI != null) {
            mc().ingameGUI.getChatGUI().printChatMessage(msg);
        }
    }

    public static void deleteHeldItem() {
        deleteSlotStack(-999);
    }

    public static void dropHeldItem() {
        mc().playerController.windowClick(((GuiContainer) mc().currentScreen).inventorySlots.windowId, -999, 0, ClickType.PICKUP, mc().player);
    }

    public static void deleteSlotStack(int slotNumber) {
        setSlotContents(slotNumber, ItemStack.EMPTY, true);
    }

    public static void decreaseSlotStack(int slotNumber) {
        ItemStack stack = slotNumber == -999 ? getHeldItem() : mc().player.openContainer.getSlot(slotNumber).getStack();
        if (stack.isEmpty()) {
            return;
        }

        if (stack.getCount() == 1) {
            deleteSlotStack(slotNumber);
        } else {
            stack = stack.copy();
            stack.shrink(1);
            setSlotContents(slotNumber, stack, true);
        }
    }

    public static void deleteEverything() {
        NEIClientPacketHandler.sendDeleteAllItems();
    }

    public static void deleteItemsOfType(ItemStack type) {
        Container c = getGuiContainer().inventorySlots;
        for (int i = 0; i < c.inventorySlots.size(); i++) {
            Slot slot = c.getSlot(i);
            if (slot == null) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == type.getItem() && stack.getItemDamage() == type.getItemDamage()) {
                setSlotContents(i, ItemStack.EMPTY, true);
                slot.putStack(ItemStack.EMPTY);
            }
        }
    }

    public static ItemStack getHeldItem() {
        return mc().player.inventory.getItemStack();
    }

    public static void setSlotContents(int slot, ItemStack item, boolean containerInv) {
        NEIClientPacketHandler.sendSetSlot(slot, item, containerInv);

        if (slot == -999) {
            mc().player.inventory.setItemStack(item);
        }
    }

    /**
     * @param mode -1 = normal cheats, 0 = no infinites, 1 = replenish stack
     */
    public static void cheatItem(ItemStack stack, int button, int mode) {
        if (!canCheatItem(stack)) {
            return;
        }

        if (mode == -1 && button == 0 && shiftKey() && NEIClientConfig.hasSMPCounterPart()) {
            cheatItem(stack, button, 0);
        } else if (button == 1) {
            giveStack(stack, 1);
        } else {
            if (mode == 1 && stack.getCount() < stack.getMaxStackSize()) {
                giveStack(stack, stack.getMaxStackSize() - stack.getCount());
            } else {
                int amount = getItemQuantity();
                if (amount == 0) {
                    amount = stack.getMaxStackSize();
                }
                giveStack(stack, amount);
            }
        }
    }

    public static void giveStack(ItemStack itemstack) {
        giveStack(itemstack, itemstack.getCount());
    }

    public static void giveStack(ItemStack itemstack, int i) {
        giveStack(itemstack, i, false);
    }

    public static void giveStack(ItemStack base, int i, boolean infinite) {
        ItemStack stack = copyStack(base, i);
        if (hasSMPCounterPart()) {
            ItemStack typestack = copyStack(stack, 1);
            if (!infinite && !canItemFitInInventory(mc().player, stack) && (mc().currentScreen instanceof GuiContainer)) {
                GuiContainer gui = getGuiContainer();
                List<Iterable<Integer>> handlerSlots = new LinkedList<>();
                for (INEIGuiHandler handler : GuiInfo.guiHandlers) {
                    handlerSlots.add(handler.getItemSpawnSlots(gui, typestack));
                }

                int increment = typestack.getMaxStackSize();
                int given = 0;
                for (int slotNo : Iterables.concat(handlerSlots)) {
                    Slot slot = gui.inventorySlots.getSlot(slotNo);
                    if (!slot.isItemValid(typestack) || !InventoryUtils.canStack(slot.getStack(), typestack)) {
                        continue;
                    }

                    int qty = Math.min(stack.getCount() - given, increment);
                    int current = slot.getHasStack() ? slot.getStack().getCount() : 0;
                    qty = Math.min(qty, slot.getSlotStackLimit() - current);

                    ItemStack newStack = copyStack(typestack, qty + current);
                    slot.putStack(newStack);
                    setSlotContents(slotNo, newStack, true);
                    given += qty;
                    if (given >= stack.getCount()) {
                        break;
                    }
                }
                if (given > 0) {
                    NEIClientPacketHandler.sendGiveItem(copyStack(typestack, given), false, false);
                }
            } else {
                NEIClientPacketHandler.sendGiveItem(stack, infinite, true);
            }
        } else {
            for (int given = 0; given < stack.getCount(); ) {
                int qty = Math.min(stack.getCount() - given, stack.getMaxStackSize());
                sendCommand(getStringSetting("command.item"), mc().player.getName(), Item.REGISTRY.getNameForObject(stack.getItem()), qty, stack.getItemDamage(), stack.hasTagCompound() ? stack.getTagCompound().toString() : "", Item.getIdFromItem(stack.getItem()));
                given += qty;
            }
        }
    }

    public static boolean canItemFitInInventory(EntityPlayer player, ItemStack itemstack) {
        return InventoryUtils.getInsertibleQuantity(new InventoryRange(player.inventory, 0, 36), itemstack) > 0;
    }

    public static boolean shiftKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static boolean controlKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static int getGamemode() {
        if (invCreativeMode()) {
            return 2;
        } else if (mc().playerController.isInCreativeMode()) {
            return 1;
        } else if (mc().playerController.getCurrentGameType().hasLimitedInteractions()) {
            return 3;
        } else {
            return 0;
        }
    }

    public static boolean isValidGamemode(String s) {
        return s.equals("survival") || canPerformAction(s) && Arrays.asList(getStringArrSetting("inventory.gamemodes")).contains(s);
    }

    public static int getNextGamemode() {
        int mode = getGamemode();
        int nmode = mode;
        while (true) {
            nmode = (nmode + 1) % NEIActions.gameModes.length;
            if (nmode == mode || isValidGamemode(NEIActions.gameModes[nmode])) {
                break;
            }
        }
        return nmode;
    }

    public static void cycleGamemode() {
        int mode = getGamemode();
        int nmode = getNextGamemode();
        if (mode == nmode) {
            return;
        }

        if (hasSMPCounterPart()) {
            NEIClientPacketHandler.sendGameMode(nmode);
        } else {
            sendCommand(getStringSetting("command.creative"), getGameType(nmode).getID(), mc().player.getName());
        }
    }

    public static long getTime() {
        return mc().world.getWorldInfo().getWorldTime();
    }

    public static void setTime(long l) {
        mc().world.getWorldInfo().setWorldTime(l);
    }

    public static void setHourForward(int hour) {
        long day = (getTime() / 24000L) * 24000L;
        long newTime = day + 24000L + hour * 1000;

        if (hasSMPCounterPart()) {
            NEIClientPacketHandler.sendSetTime(hour);
        } else {
            sendCommand(getStringSetting("command.time"), newTime);
        }
    }

    public static void sendCommand(String command, Object... args) {
        try {
            if (command.length() == 0) {
                return;
            }

            NumberFormat numberformat = NumberFormat.getIntegerInstance();
            numberformat.setGroupingUsed(false);
            MessageFormat messageformat = new MessageFormat(command);
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Integer || args[i] instanceof Long) {
                    messageformat.setFormatByArgumentIndex(i, numberformat);
                }
            }

            mc().player.sendChatMessage(messageformat.format(args));
        } catch (Exception e) {
            e.printStackTrace();
            mc().player.sendMessage(new TextComponentString("[NEI] Error parsing arguments for server command. See logs."));
        }
    }

    public static boolean isRaining() {
        return mc().world.getWorldInfo().isRaining();
    }

    public static void toggleRaining() {
        if (hasSMPCounterPart()) {
            NEIClientPacketHandler.sendToggleRain();
        } else {
            sendCommand(getStringSetting("command.rain"), isRaining() ? 0 : 1);
        }
    }

    public static void healPlayer() {
        if (hasSMPCounterPart()) {
            NEIClientPacketHandler.sendHeal();
        } else {
            sendCommand(getStringSetting("command.heal"), mc().player.getName());
        }
    }

    public static void toggleMagnetMode() {
        if (hasSMPCounterPart()) {
            NEIClientPacketHandler.sendToggleMagnetMode();
        }
    }

    public static ArrayList<int[]> concatIntegersToRanges(List<Integer> damages) {
        ArrayList<int[]> ranges = new ArrayList<>();
        if (damages.size() == 0) {
            return ranges;
        }

        Collections.sort(damages);
        int start = -1;
        int next = 0;
        for (Integer i : damages) {
            if (start == -1) {
                start = next = i;
                continue;
            }
            if (next + 1 != i) {
                ranges.add(new int[] { start, next });
                start = next = i;
                continue;
            }
            next = i;
        }
        ranges.add(new int[] { start, next });
        return ranges;
    }

    public static ArrayList<int[]> addIntegersToRanges(List<int[]> ranges, List<Integer> damages) {
        for (int[] range : ranges) {
            for (int integer = range[0]; integer <= range[1]; integer++) {
                damages.add(integer);
            }
        }

        return concatIntegersToRanges(damages);
    }

    public static boolean safeKeyDown(int keyCode) {
        try {
            return Keyboard.isKeyDown(keyCode);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public static void setItemQuantity(int i) {
        world.nbt.setInteger("quantity", i);
        world.saveNBT();
        LayoutManager.quantity.setText(Integer.toString(i));
    }

    public static GuiContainer getGuiContainer() {
        if (mc().currentScreen instanceof GuiContainer) {
            return (GuiContainer) mc().currentScreen;
        }

        return null;
    }

    public static boolean altKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public static void playClickSound() {
        mc().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}

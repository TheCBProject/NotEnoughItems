package codechicken.nei.network;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ClientUtils;
import codechicken.nei.ClientHandler;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.container.ContainerCreativeInv;
import codechicken.nei.container.ExtendedCreativeInv;
import codechicken.nei.gui.GuiEnchantmentModifier;
import codechicken.nei.gui.GuiExtendedCreativeInv;
import codechicken.nei.gui.GuiPotionCreator;
import codechicken.nei.handler.MagnetModeHandler;
import codechicken.nei.util.LogHelper;
import codechicken.nei.util.NEIClientUtils;
import codechicken.nei.util.NEIServerUtils;
import codechicken.nei.widget.action.NEIActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class NEIClientPacketHandler implements IClientPacketHandler {

    public static final String channel = "NEI";

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient netHandler) {
        switch (packet.getType()) {
            case 1:
                handleServerSideCheck(packet.readUByte(), packet.readString(), mc.world);
                break;
            case 10:
                handleLoginState(packet);
                break;
            case 11:
                handleActionDisableStateChange(packet);
                break;
            case 12:
                handleActionEnabled(packet);
                break;
            case 13:
                MagnetModeHandler.INSTANCE.trackMagnetItem(packet.readInt(), mc.world);
                break;
            case 14:
                handleGameMode(mc, packet.readUByte());
                break;
            case 21:
                ClientUtils.openSMPGui(packet.readUByte(), new GuiEnchantmentModifier(mc.player.inventory, mc.world));
                break;
            case 23:
                if (packet.readBoolean()) {
                    ClientUtils.openSMPGui(packet.readUByte(), new GuiExtendedCreativeInv(new ContainerCreativeInv(mc.player, new ExtendedCreativeInv(null, Side.CLIENT))));
                } else {
                    mc.displayGuiScreen(new GuiInventory(mc.player));
                }
                break;
            case 24:
                ClientUtils.openSMPGui(packet.readUByte(), new GuiPotionCreator(mc.player.inventory));
                break;
        }
    }

    /**
     * Handles setting the clients game mode.
     *
     * @param mc   The MC instance.
     * @param mode The Mode to change to.
     */
    private void handleGameMode(Minecraft mc, int mode) {
        mc.playerController.setGameType(NEIServerUtils.getGameType(mode));
    }

    private void handleActionEnabled(PacketCustom packet) {
        String name = packet.readString();
        if (packet.readBoolean()) {
            NEIClientConfig.enabledActions.add(name);
        } else {
            NEIClientConfig.enabledActions.remove(name);
        }
    }

    /**
     * Handles when an action is disabled.
     * For Example. Noon, Weather.
     *
     * @param packet The packet to handle.
     */
    private void handleActionDisableStateChange(PacketCustom packet) {
        String name = packet.readString();
        if (packet.readBoolean()) {
            NEIClientConfig.disabledActions.add(name);
        } else {
            NEIClientConfig.disabledActions.remove(name);
        }
    }

    /**
     * Handles the LoginState sent by the server.
     * Resets and loads from the packet the following things:
     * Permissible actions.
     * Disabled actions.
     * Enabled actions.
     * Banned items.
     *
     * @param packet Packet to handle.
     */
    private void handleLoginState(PacketCustom packet) {
        NEIClientConfig.permissibleActions.clear();
        int num = packet.readUByte();
        for (int i = 0; i < num; i++) {
            NEIClientConfig.permissibleActions.add(packet.readString());
        }

        NEIClientConfig.disabledActions.clear();
        num = packet.readUByte();
        for (int i = 0; i < num; i++) {
            NEIClientConfig.disabledActions.add(packet.readString());
        }

        NEIClientConfig.enabledActions.clear();
        num = packet.readUByte();
        for (int i = 0; i < num; i++) {
            NEIClientConfig.enabledActions.add(packet.readString());
        }

        NEIClientConfig.bannedItems.clear();
        num = packet.readInt();
        for (int i = 0; i < num; i++) {
            NEIClientConfig.bannedItems.add(packet.readItemStack());
        }

        if (NEIClientUtils.getGuiContainer() != null) {
            LayoutManager.instance().refresh(NEIClientUtils.getGuiContainer());
        }
    }

    /**
     * Handles the servers ServerSideCheck.
     * Checks both local and remote protocol versions for a mismatch.
     * If no mismatch is found it does the following:
     * Notifies ClientHandler of a world change.
     * Resets all local data of that dimension.
     * Requests the server for a LoginState.
     * Finally it sets the availability of a ServerSide counterpart as true.
     *
     * @param serverProtocol The servers protocol version.
     * @param worldName      The dimension data to load.
     * @param world          The clients current world object.
     */
    private void handleServerSideCheck(int serverProtocol, String worldName, World world) {
        if (serverProtocol > NEIActions.protocol) {
            NEIClientUtils.printChatMessage(new TextComponentTranslation("nei.chat.mismatch.client"));
        } else if (serverProtocol < NEIActions.protocol) {
            NEIClientUtils.printChatMessage(new TextComponentTranslation("nei.chat.mismatch.server"));
        } else {
            try {
                ClientHandler.INSTANCE.loadWorld(world);
                NEIClientConfig.loadWorld(getSaveName(worldName));
                NEIClientConfig.setHasSMPCounterPart(true);
                sendRequestLoginInfo();
            } catch (Exception e) {
                LogHelper.errorError("Error handling SMP Check", e);
            }
        }
    }

    private static String getSaveName(String worldName) {
        if (Minecraft.getMinecraft().isSingleplayer()) {
            return "local/" + ClientUtils.getWorldSaveName();
        }

        return "remote/" + ClientUtils.getServerIP().replace(':', '~') + "/" + worldName;
    }

    /**
     * Tells the server to cheat a specific item to the player.
     *
     * @param stack    The stack to cheat.
     * @param infinite If the stack should be infinite or not.
     * @param doSpawn  If the server should actually give the item.
     */
    public static void sendGiveItem(ItemStack stack, boolean infinite, boolean doSpawn) {
        PacketCustom packet = new PacketCustom(channel, 1);
        packet.writeItemStack(stack);
        packet.writeBoolean(infinite);
        packet.writeBoolean(doSpawn);
        packet.sendToServer();
    }

    /**
     * Tells the server to delete all items in the current inventory.
     */
    public static void sendDeleteAllItems() {
        PacketCustom packet = new PacketCustom(channel, 4);
        packet.sendToServer();
    }

    @Deprecated //TODO ??
    public static void sendStateLoad(ItemStack[] state) {
        sendDeleteAllItems();
        for (int slot = 0; slot < state.length; slot++) {
            ItemStack item = state[slot];
            if (item.isEmpty()) {
                continue;
            }
            sendSetSlot(slot, item, false);
        }

        PacketCustom packet = new PacketCustom(channel, 11);
        packet.sendToServer();
    }

    /**
     * Sets a specific slot on the players inventory to the given item.
     *
     * @param slot      The slot to change.
     * @param stack     The stack to set the slot to.
     * @param container if the inventory is a container.
     */
    public static void sendSetSlot(int slot, ItemStack stack, boolean container) {
        PacketCustom packet = new PacketCustom(channel, 5);
        packet.writeBoolean(container);
        packet.writeShort(slot);
        packet.writeItemStack(stack);
        packet.sendToServer();
    }

    /**
     * Sent to the server to request a LoginState.
     */
    private static void sendRequestLoginInfo() {
        PacketCustom packet = new PacketCustom(channel, 10);
        packet.sendToServer();
    }

    /**
     * Sent to the server when MagnetMode is toggled.
     */
    public static void sendToggleMagnetMode() {
        PacketCustom packet = new PacketCustom(channel, 6);
        packet.sendToServer();
    }

    /**
     * Sent to the server to change the time.
     */
    public static void sendSetTime(int hour) {
        PacketCustom packet = new PacketCustom(channel, 7);
        packet.writeByte(hour);
        packet.sendToServer();
    }

    /**
     * Sent to the server to heal the player.
     */
    public static void sendHeal() {
        PacketCustom packet = new PacketCustom(channel, 8);
        packet.sendToServer();
    }

    /**
     * Sent to the server to toggle the rain.
     */
    public static void sendToggleRain() {
        PacketCustom packet = new PacketCustom(channel, 9);
        packet.sendToServer();
    }

    /**
     * Sent to the server to open the Enchantment gui.
     */
    public static void sendOpenEnchantmentWindow() {
        PacketCustom packet = new PacketCustom(channel, 21);
        packet.sendToServer();
    }

    /**
     * Notifies the server about an enchantment being modified inside NEI's enchantment gui.
     *
     * @param enchID The Enchantment ID.
     * @param level  The Enchantments level.
     * @param add    If the enchantment is being added or removed.
     */
    public static void sendModifyEnchantment(int enchID, int level, boolean add) {
        PacketCustom packet = new PacketCustom(channel, 22);
        packet.writeByte(enchID);
        packet.writeByte(level);
        packet.writeBoolean(add);
        packet.sendToServer();
    }

    /**
     * Sent to the server when a user disables a specific action.
     * For example Noon, Weather.
     * Is handled on the server then relayed to all clients in the dimension.
     *
     * @param name   The identifier for the action.
     * @param enable The actions new state.
     */
    public static void sendActionDisableStateChange(String name, boolean enable) {
        PacketCustom packet = new PacketCustom(channel, 12);
        packet.writeString(name);
        packet.writeBoolean(enable);
        packet.sendToServer();
    }

    /**
     * Tells the server that the player has requested a GameMode change.
     *
     * @param mode The mode to change to.
     */
    public static void sendGameMode(int mode) {
        new PacketCustom(channel, 13).writeByte(mode).sendToServer();
    }

    /**
     * Tells the server open NEI's extended creative inv.
     *
     * @param open Is it open or close.
     */
    public static void sendCreativeInv(boolean open) {
        PacketCustom packet = new PacketCustom(channel, 23);
        packet.writeBoolean(open);
        packet.sendToServer();
    }

    public static void sendCreativeScroll(int steps) {
        PacketCustom packet = new PacketCustom(channel, 14);
        packet.writeInt(steps);
        packet.sendToServer();
    }

    public static void sendMobSpawnerID(int x, int y, int z, String mobtype) {
        PacketCustom packet = new PacketCustom(channel, 15);
        packet.writePos(new BlockPos(x, y, z));
        packet.writeString(mobtype);
        packet.sendToServer();
    }

    public static PacketCustom createContainerPacket() {
        return new PacketCustom(channel, 20);
    }

    public static void sendOpenPotionWindow() {
        ItemStack[] potionStore = new ItemStack[9];
        ArrayUtils.fillArray(potionStore, ItemStack.EMPTY);
        InventoryUtils.readItemStacksFromTag(potionStore, NEIClientConfig.global.nbt.getCompoundTag("potionStore").getTagList("items", 10));
        PacketCustom packet = new PacketCustom(channel, 24);
        for (ItemStack stack : potionStore) {
            packet.writeItemStack(stack);
        }
        packet.sendToServer();
    }

    public static void sendDummySlotSet(int slotNumber, ItemStack stack) {
        PacketCustom packet = new PacketCustom(channel, 25);
        packet.writeShort(slotNumber);
        packet.writeItemStack(stack);
        packet.sendToServer();
    }
}

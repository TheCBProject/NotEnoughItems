package codechicken.nei.network;

import codechicken.lib.inventory.container.ContainerExtended;
import codechicken.lib.inventory.container.SlotDummy;
import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ServerUtils;
import codechicken.nei.NEIServerConfig;
import codechicken.nei.PlayerSave;
import codechicken.nei.container.ContainerCreativeInv;
import codechicken.nei.container.ContainerEnchantmentModifier;
import codechicken.nei.container.ContainerPotionCreator;
import codechicken.nei.container.ExtendedCreativeInv;
import codechicken.nei.util.ItemStackMap;
import codechicken.nei.util.LogHelper;
import codechicken.nei.util.NEIServerUtils;
import codechicken.nei.widget.action.NEIActions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.util.LinkedList;
import java.util.Set;

public class NEIServerPacketHandler implements IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, EntityPlayerMP sender, INetHandlerPlayServer netHandler) {
        if (!NEIServerConfig.authenticatePacket(sender, packet)) {
            return;
        }

        switch (packet.getType()) {
            case 1:
                handleGiveItem(sender, packet);
                break;
            case 4:
                NEIServerUtils.deleteAllItems(sender);
                break;
            case 5:
                setInventorySlot(sender, packet);
                break;
            case 6:
                NEIServerUtils.toggleMagnetMode(sender);
                break;
            case 7:
                NEIServerUtils.setHourForward(sender.world, packet.readUByte(), true);
                break;
            case 8:
                NEIServerUtils.healPlayer(sender);
                break;
            case 9:
                NEIServerUtils.toggleRaining(sender.world, true);
                break;
            case 10:
                sendLoginState(sender);
                break;
            case 11:
                sender.sendAllContents(sender.openContainer, sender.openContainer.getInventory());
                break;
            case 12:
                handleActionDisableStateChange(sender, packet);
                break;
            case 13:
                NEIServerUtils.setGamemode(sender, packet.readUByte());
                break;
            case 14:
                NEIServerUtils.cycleCreativeInv(sender, packet.readInt());
                break;
            case 15:
                handleMobSpawnerID(sender.world, packet.readPos(), packet.readString());
                break;
            case 20:
                handleContainerPacket(sender, packet);
                break;
            case 21:
                openEnchantmentGui(sender);
                break;
            case 22:
                modifyEnchantment(sender, packet.readUByte(), packet.readUByte(), packet.readBoolean());
                break;
            case 23:
                processCreativeInv(sender, packet.readBoolean());
                break;
            case 24:
                openPotionGui(sender, packet);
                break;
            case 25:
                handleDummySlotSet(sender, packet);
                break;
        }
    }

    private void handleDummySlotSet(EntityPlayerMP sender, PacketCustom packet) {
        Slot slot = sender.openContainer.getSlot(packet.readShort());
        if (slot instanceof SlotDummy) {
            slot.putStack(packet.readItemStack());
        }
    }

    private void handleContainerPacket(EntityPlayerMP sender, PacketCustom packet) {
        if (sender.openContainer instanceof ContainerExtended) {
            ((ContainerExtended) sender.openContainer).handleInputPacket(packet);
        }
    }

    private void handleMobSpawnerID(World world, BlockPos coord, String mobtype) {
        TileEntity tile = world.getTileEntity(coord);
        if (tile instanceof TileEntityMobSpawner) {
            ((TileEntityMobSpawner) tile).getSpawnerBaseLogic().setEntityId(new ResourceLocation(mobtype));
            tile.markDirty();
            IBlockState state = world.getBlockState(coord);
            world.notifyBlockUpdate(coord, state, state, 4);
        }
    }

    /**
     * Handles when a client disables an action.
     * For Example, Noon, Weather.
     * If the player is permitted to change the action, a packet is then relayed to all clients in the dimension.
     *
     * @param sender The player that changed the actions state.
     * @param packet The packet containing data.
     */
    private void handleActionDisableStateChange(EntityPlayerMP sender, PacketCustom packet) {
        String name = packet.readString();
        if (NEIServerConfig.canPlayerPerformAction(sender.getName(), name)) {
            NEIServerConfig.setDisableActionState(sender.dimension, name, packet.readBoolean());
        }
    }

    public static void processCreativeInv(EntityPlayerMP sender, boolean open) {
        if (open) {
            ServerUtils.openSMPContainer(sender, new ContainerCreativeInv(sender, new ExtendedCreativeInv(NEIServerConfig.getSaveForPlayer(sender.getName()), Side.SERVER)), (player, windowId) -> {
                PacketCustom packet = new PacketCustom(channel, 23);
                packet.writeBoolean(true);
                packet.writeByte(windowId);
                packet.sendToPlayer(player);
            });
        } else {
            sender.closeContainer();
            PacketCustom packet = new PacketCustom(channel, 23);
            packet.writeBoolean(false);
            packet.sendToPlayer(sender);
        }
    }

    private void handleGiveItem(EntityPlayerMP player, PacketCustom packet) {
        NEIServerUtils.givePlayerItem(player, packet.readItemStack(), packet.readBoolean(), packet.readBoolean());
    }

    private void setInventorySlot(EntityPlayerMP player, PacketCustom packet) {
        boolean container = packet.readBoolean();
        int slot = packet.readShort();
        ItemStack item = packet.readItemStack();

        ItemStack old = NEIServerUtils.getSlotContents(player, slot, container);
        boolean deleting = item.isEmpty() || !old.isEmpty() && NEIServerUtils.areStacksSameType(item, old) && item.getCount() < old.getCount();
        if (NEIServerConfig.canPlayerPerformAction(player.getName(), deleting ? "delete" : "item")) {
            NEIServerUtils.setSlotContents(player, slot, item, container);
        }
    }

    @Deprecated
    private void modifyEnchantment(EntityPlayerMP player, int e, int lvl, boolean add) {
        ContainerEnchantmentModifier containerem = (ContainerEnchantmentModifier) player.openContainer;
        if (add) {
            containerem.addEnchantment(e, lvl);
        } else {
            containerem.removeEnchantment(e);
        }
    }

    private void openEnchantmentGui(EntityPlayerMP player) {
        ServerUtils.openSMPContainer(player, new ContainerEnchantmentModifier(player.inventory, player.world), (player1, windowId) -> {
            PacketCustom packet = new PacketCustom(channel, 21);
            packet.writeByte(windowId);
            packet.sendToPlayer(player1);
        });
    }

    private void openPotionGui(EntityPlayerMP player, PacketCustom packet) {
        InventoryBasic b = new InventoryBasic("potionStore", true, 9);
        for (int i = 0; i < b.getSizeInventory(); i++) {
            b.setInventorySlotContents(i, packet.readItemStack());
        }
        ServerUtils.openSMPContainer(player, new ContainerPotionCreator(player.inventory, b), (player1, windowId) -> {
            PacketCustom packet1 = new PacketCustom(channel, 24);
            packet1.writeByte(windowId);
            packet1.sendToPlayer(player1);
        });
    }

    /**
     * Sends a packet to all clients in the dimension notifying them of a specific actions disable state change.
     *
     * @param dim     The dimension the change happened for.
     * @param name    The name of the action.
     * @param disable The actions state.
     */
    public static void sendActionDisabledState(int dim, String name, boolean disable) {
        new PacketCustom(channel, 11).writeString(name).writeBoolean(disable).sendToDimension(dim);
    }

    /**
     * Sends an actions state change to the client.
     *
     * @param player The player in which the action has changed for.
     * @param name   The actions identifier.
     * @param enable The actions state.
     */
    public static void sendActionStateChange(EntityPlayerMP player, String name, boolean enable) {
        new PacketCustom(channel, 12).writeString(name).writeBoolean(enable).sendToPlayer(player);
    }

    /**
     * Requested by the client when a successful ServerSide Check has happened.
     * Sends the following to the client:
     * Permissible actions.
     * Disabled actions.
     * Enabled actions.
     * Banned Items.
     *
     * @param player The player Requesting a LoginState.
     */
    private void sendLoginState(EntityPlayerMP player) {
        LinkedList<String> actions = new LinkedList<>();
        LinkedList<String> disabled = new LinkedList<>();
        LinkedList<String> enabled = new LinkedList<>();
        LinkedList<ItemStack> bannedItems = new LinkedList<>();
        PlayerSave playerSave = NEIServerConfig.getSaveForPlayer(player.getName());

        for (String name : NEIActions.nameActionMap.keySet()) {
            if (NEIServerConfig.canPlayerPerformAction(player.getName(), name)) {
                actions.add(name);
            }
            if (NEIServerConfig.isActionDisabled(player.dimension, name)) {
                disabled.add(name);
            }
            if (playerSave.isActionEnabled(name)) {
                enabled.add(name);
            }
        }
        for (ItemStackMap.Entry<Set<String>> entry : NEIServerConfig.bannedItems.entries()) {
            if (!NEIServerConfig.isPlayerInList(player.getName(), entry.value, true)) {
                bannedItems.add(entry.key);
            }
        }

        PacketCustom packet = new PacketCustom(channel, 10);

        packet.writeByte(actions.size());
        for (String s : actions) {
            packet.writeString(s);
        }

        packet.writeByte(disabled.size());
        for (String s : disabled) {
            packet.writeString(s);
        }

        packet.writeByte(enabled.size());
        for (String s : enabled) {
            packet.writeString(s);
        }

        packet.writeInt(bannedItems.size());
        for (ItemStack stack : bannedItems) {
            packet.writeItemStack(stack);
        }

        packet.sendToPlayer(player);
    }

    /**
     * Sends the current protocol version and world to the client.
     * Called every time the player changes dimensions.
     * If successful on the client, it will request a LoginState.
     *
     * @param player The player to send the ServerSide check to.
     */
    public static void sendServerSideCheck(EntityPlayerMP player) {
        LogHelper.debug("Sending ServerSide check to: " + player.getName());
        PacketCustom packet = new PacketCustom(channel, 1);
        packet.writeByte(NEIActions.protocol);
        packet.writeString(player.world.getWorldInfo().getWorldName());

        packet.sendToPlayer(player);
    }

    /**
     * Adds a tracked MagnetItem on the client.
     *
     * @param player Player to send to.
     * @param item   EntityItem to send.
     */
    public static void sendTrackedMagnetItem(EntityPlayerMP player, EntityItem item) {
        PacketCustom packet = new PacketCustom(channel, 13);
        packet.writeInt(item.getEntityId());

        packet.sendToPlayer(player);
    }

    public static final String channel = "NEI";
}

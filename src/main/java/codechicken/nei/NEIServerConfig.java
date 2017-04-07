package codechicken.nei;

import codechicken.lib.config.ConfigFile;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.CommonUtils;
import codechicken.lib.util.ServerUtils;
import codechicken.nei.network.NEIServerPacketHandler;
import codechicken.nei.util.ItemStackMap;
import codechicken.nei.util.LogHelper;
import codechicken.nei.util.NEIServerUtils;
import codechicken.nei.widget.action.NEIActions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class NEIServerConfig {

    private static MinecraftServer server;

    public static File saveDir;
    public static ConfigFile serverConfig;
    public static Map<Integer, NBTTagCompound> dimTags = new HashMap<>();
    public static HashMap<String, PlayerSave> playerSaves = new HashMap<>();
    public static ItemStackMap<Set<String>> bannedItems = new ItemStackMap<>(null);

    public static void load(World world) {
        if (ServerUtils.mc() != server) {
            LogHelper.debug("Loading NEI Server");
            server = ServerUtils.mc();
            saveDir = new File(DimensionManager.getCurrentSaveRootDirectory(), "NEI");

            dimTags.clear();
            loadConfig();
            loadBannedItems();
        }
        loadWorld(world);
    }

    public static File getSaveDir(World world) {
        return new File(CommonUtils.getSaveLocation(world), "NEI");
    }

    private static void loadWorld(World world) {
        try {
            File file = new File(getSaveDir(world), "world.dat");
            NBTTagCompound tag = NEIServerUtils.readNBT(file);
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            dimTags.put(world.provider.getDimension(), tag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadConfig() {
        File file = new File(saveDir, "server.cfg");
        serverConfig = new ConfigFile(file);

        serverConfig.setNewLineMode(1);
        serverConfig.getTag("permissions").useBraces();
        serverConfig.getTag("permissions").setComment("List of players who can use these features.\nEg. time=CodeChicken, Friend1");

        setDefaultFeature("time");
        setDefaultFeature("rain");
        setDefaultFeature("heal");
        setDefaultFeature("magnet");
        setDefaultFeature("creative");
        setDefaultFeature("creative+");
        setDefaultFeature("adventure");
        setDefaultFeature("enchant");
        setDefaultFeature("potion");
        setDefaultFeature("save-state");
        setDefaultFeature("item");
        setDefaultFeature("delete");
        setDefaultFeature("notify-item", "CONSOLE, OP");
    }

    private static void setDefaultFeature(String featurename, String... names) {
        if (names.length == 0) {
            names = new String[] { "OP" };
        }

        String list = "";
        for (int i = 0; i < names.length; i++) {
            if (i >= 1) {
                list += ", ";
            }
            list += names[i];
        }
        serverConfig.getTag("permissions." + featurename).setDefaultValue(list);
    }

    private static void saveWorld(int dim) {
        try {
            File file = new File(getSaveDir(DimensionManager.getWorld(dim)), "world.dat");
            NEIServerUtils.writeNBT(dimTags.get(dim), file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a player has permission to change a specific actions state.
     *
     * @param playerName The player in question.
     * @param name       The identifier for the action.
     * @return If the player has permission.
     */
    public static boolean canPlayerPerformAction(String playerName, String name) {
        return isPlayerInList(playerName, getPlayerList("permissions." + NEIActions.base(name)), true);
    }

    /**
     * Checks if a player is in the specified list.
     *
     * @param playerName The players name.
     * @param list       The list of players.
     * @param allowCards Allows some wild cars when checking, Specifically, OP, Server Owner, and the "ALL" user.
     * @return If the player is in the list.
     */
    public static boolean isPlayerInList(String playerName, Set<String> list, boolean allowCards) {
        if (playerName.equals("CONSOLE")) {
            return list.contains(playerName);
        }

        playerName = playerName.toLowerCase();

        if (allowCards) {
            if (list.contains("ALL")) {
                return true;
            }
            if ((ServerUtils.isPlayerOP(playerName) || ServerUtils.isPlayerOwner(playerName)) && list.contains("OP")) {
                return true;
            }
        }

        return list.contains(playerName);
    }

    /**
     * Checks if the action is disabled.
     * For Example. Noon, weather.
     *
     * @param dim  The dimension to check.
     * @param name The actions identifier.
     * @return If the action is disabled.
     */
    public static boolean isActionDisabled(int dim, String name) {
        return dimTags.get(dim).getBoolean("disabled" + name);
    }

    /**
     * Disabled a specific action.
     * Also notifies all clients in the dimension of the change.
     *
     * @param dim     The dimension the change has happened for.
     * @param name    The name of the action.
     * @param disable The state of the action.
     */
    public static void setDisableActionState(int dim, String name, boolean disable) {
        dimTags.get(dim).setBoolean("disabled" + name, disable);
        NEIServerPacketHandler.sendActionDisabledState(dim, name, disable);
        saveWorld(dim);
    }

    public static HashSet<String> getPlayerList(String tag) {
        String[] list = serverConfig.getTag(tag).getValue("").replace(" ", "").split(",");
        return new HashSet<>(Arrays.asList(list));
    }

    public static void addPlayerToList(String playername, String tag) {
        HashSet<String> list = getPlayerList(tag);

        if (!playername.equals("CONSOLE") && !playername.equals("ALL") && !playername.equals("OP")) {
            playername = playername.toLowerCase();
        }

        list.add(playername);
        savePlayerList(tag, list);
    }

    public static void remPlayerFromList(String playername, String tag) {
        HashSet<String> list = getPlayerList(tag);

        if (!playername.equals("CONSOLE") && !playername.equals("ALL") && !playername.equals("OP")) {
            playername = playername.toLowerCase();
        }

        list.remove(playername);
        savePlayerList(tag, list);
    }

    private static void savePlayerList(String tag, Collection<String> list) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); i++) {
            if (i != 0) {
                sb.append(", ");
            }

            sb.append(iterator.next());
        }

        serverConfig.getTag(tag).setValue(sb.toString());
    }

    private static void loadBannedItems() {
        bannedItems.clear();
        File file = new File(saveDir, "banneditems.cfg");
        if (!file.exists()) {
            bannedItems.put(new ItemStack(Blocks.COMMAND_BLOCK), new HashSet<>(Arrays.asList("NONE")));
            saveBannedItems();
            return;
        }
        try {
            FileReader r = new FileReader(file);
            int line = 0;
            for (String s : IOUtils.readLines(r)) {
                if (s.charAt(0) == '#' || s.trim().length() == 0) {
                    continue;
                }
                int delim = s.lastIndexOf('=');
                if (delim < 0) {
                    LogHelper.error("line %s: Missing =", line);
                    continue;
                }
                try {
                    NBTTagCompound key = JsonToNBT.getTagFromJson(s.substring(0, delim));
                    Set<String> values = new HashSet<>();
                    for (String s2 : s.substring(delim + 1).split(",")) {
                        values.add(s2.trim());
                    }
                    bannedItems.put(InventoryUtils.loadPersistant(key), values);
                } catch (Exception e) {
                    LogHelper.error("line %s: %s", line, e.getMessage());
                }
            }
            r.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveBannedItems() {
        File file = new File(saveDir, "banneditems.cfg");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter p = new PrintWriter(file);
            p.println("#Saved in this format for external editing. The format isn't that hard to figure out. If you think you're up to it, modify it here!");

            for (ItemStackMap.Entry<Set<String>> entry : bannedItems.entries()) {
                NBTTagCompound key = InventoryUtils.savePersistant(entry.key, new NBTTagCompound());
                key.removeTag("Count");
                if (key.getByte("Damage") == 0) {
                    key.removeTag("Damage");
                }

                p.print(key.toString());
                p.print("=[");
                int i = 0;
                for (String s : entry.value) {
                    if (i++ != 0) {
                        p.print(", ");
                    }
                    p.print(s);
                }
                p.println("]");
            }
            p.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PlayerSave getSaveForPlayer(String username) {
        return playerSaves.get(username);
    }

    public static void loadPlayer(EntityPlayer player) {
        LogHelper.debug("Loading Player: " + player.getGameProfile().getName());
        playerSaves.put(player.getName(), new PlayerSave((EntityPlayerMP) player, new File(saveDir, "players")));
    }

    public static void unloadPlayer(EntityPlayer player) {
        LogHelper.debug("Unloading Player: " + player.getName());
        PlayerSave playerSave = playerSaves.remove(player.getName());
        if (playerSave != null) {
            playerSave.save();
        }
    }

    public static boolean authenticatePacket(EntityPlayerMP sender, PacketCustom packet) {
        switch (packet.getType()) {
            case 1:
                return canPlayerPerformAction(sender.getName(), "item");
            case 4:
                return canPlayerPerformAction(sender.getName(), "delete");
            case 6:
                return canPlayerPerformAction(sender.getName(), "magnet");
            case 7:
                return canPlayerPerformAction(sender.getName(), "time");
            case 8:
                return canPlayerPerformAction(sender.getName(), "heal");
            case 9:
                return canPlayerPerformAction(sender.getName(), "rain");
            case 14:
            case 23:
                return canPlayerPerformAction(sender.getName(), "creative+");
            case 21:
            case 22:
                return canPlayerPerformAction(sender.getName(), "enchant");
            case 24:
                return canPlayerPerformAction(sender.getName(), "potion");
        }
        return true;
    }
}

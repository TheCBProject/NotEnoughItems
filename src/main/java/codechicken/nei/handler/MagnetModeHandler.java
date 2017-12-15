package codechicken.nei.handler;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIServerConfig;
import codechicken.nei.PlayerSave;
import codechicken.nei.network.NEIServerPacketHandler;
import codechicken.nei.util.NEIClientUtils;
import codechicken.nei.util.NEIServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by covers1624 on 30/03/2017.
 *
 * This class is dedicated to MagnetMode.. Duh
 * It both, tracks magnet items on the client and sends items to be tracked from the server.
 */
public class MagnetModeHandler {

    public static final MagnetModeHandler INSTANCE = new MagnetModeHandler();

    private List<EntityItem> clientMagnetItems = new ArrayList<>();

    public static final float DISTANCE_XZ = 16;
    public static final float DISTANCE_Y = 8;
    public static final double MAX_SPEED_XZ = 0.5;
    public static final double MAX_SPEED_Y = 0.5;
    public static final double SPEED_XZ = 0.05;
    public static final double SPEED_Y = 0.07;

    public void trackMagnetItem(int entityID, World world) {
        Entity e = world.getEntityByID(entityID);
        if (e instanceof EntityItem) {
            clientMagnetItems.add(((EntityItem) e));
        }
    }

    public void nukeClientMagnetTracker() {
        clientMagnetItems.clear();
    }

    @SubscribeEvent
    @SideOnly (Side.CLIENT)
    public void clientTickEvent(TickEvent.ClientTickEvent event) {

        if (event.phase == Phase.END || Minecraft.getMinecraft().world == null) {
            return;
        }
        if (!NEIClientConfig.isMagnetModeEnabled()) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;

        Iterator<EntityItem> iterator = clientMagnetItems.iterator();
        while (iterator.hasNext()) {
            EntityItem item = iterator.next();
            if (item.cannotPickup()) {
                continue;
            }
            if (item.isDead) {
                iterator.remove();
            }
            if (!NEIClientUtils.canItemFitInInventory(player, item.getItem())) {
                continue;
            }
            double dx = player.posX - item.posX;
            double dy = player.posY + player.getEyeHeight() - item.posY;
            double dz = player.posZ - item.posZ;
            double absxz = Math.sqrt(dx * dx + dz * dz);
            double absy = Math.abs(dy);
            if (absxz > DISTANCE_XZ || absy > DISTANCE_Y) {
                continue;
            }

            if (absxz > 1) {
                dx /= absxz;
                dz /= absxz;
            }

            if (absy > 1) {
                dy /= absy;
            }

            double vx = item.motionX + SPEED_XZ * dx;
            double vy = item.motionY + SPEED_Y * dy;
            double vz = item.motionZ + SPEED_XZ * dz;

            double absvxz = Math.sqrt(vx * vx + vz * vz);
            double absvy = Math.abs(vy);

            double rationspeedxz = absvxz / MAX_SPEED_XZ;
            if (rationspeedxz > 1) {
                vx /= rationspeedxz;
                vz /= rationspeedxz;
            }

            double rationspeedy = absvy / MAX_SPEED_Y;
            if (rationspeedy > 1) {
                vy /= rationspeedy;
            }

            if (absvxz < 0.2 && absxz < 0.2) {
                item.setDead();
            }

            item.setVelocity(vx, vy, vz);
        }
    }

    @SubscribeEvent (priority = EventPriority.HIGH)//We need this to fire before our main handler so PlayerSave has time to write to disk if there is changes.
    public void tickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == Phase.START && event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = ((EntityPlayerMP) event.player);
            PlayerSave save = NEIServerConfig.getSaveForPlayer(player.getName());
            if (save == null) {
                return;
            }

            if (!save.isActionEnabled("magnet") || player.isDead) {
                return;
            }

            List<EntityItem> items = player.world.getEntitiesWithinAABB(EntityItem.class, player.getEntityBoundingBox().grow(DISTANCE_XZ, DISTANCE_Y, DISTANCE_XZ));
            for (EntityItem item : items) {
                if (item.cannotPickup()) {
                    continue;
                }
                if (!NEIServerUtils.canItemFitInInventory(player, item.getItem())) {
                    continue;
                }
                if (save.magneticItems.add(item)) {
                    NEIServerPacketHandler.sendTrackedMagnetItem(player, item);
                }

                double dx = player.posX - item.posX;
                double dy = player.posY + player.getEyeHeight() - item.posY;
                double dz = player.posZ - item.posZ;
                double absxz = Math.sqrt(dx * dx + dz * dz);
                double absy = Math.abs(dy);
                if (absxz > DISTANCE_XZ) {
                    continue;
                }

                if (absxz < 1) {
                    item.onCollideWithPlayer(player);
                }

                if (absxz > 1) {
                    dx /= absxz;
                    dz /= absxz;
                }

                if (absy > 1) {
                    dy /= absy;
                }

                double vx = item.motionX + SPEED_XZ * dx;
                double vy = item.motionY + SPEED_Y * dy;
                double vz = item.motionZ + SPEED_XZ * dz;

                double absvxz = Math.sqrt(vx * vx + vz * vz);
                double absvy = Math.abs(vy);

                double rationspeedxz = absvxz / MAX_SPEED_XZ;
                if (rationspeedxz > 1) {
                    vx /= rationspeedxz;
                    vz /= rationspeedxz;
                }

                double rationspeedy = absvy / MAX_SPEED_Y;
                if (absvy > 1) {
                    vy /= rationspeedy;
                }

                item.motionX = vx;
                item.motionY = vy;
                item.motionZ = vz;
            }
        }
    }

}

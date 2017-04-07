package codechicken.nei;

import codechicken.nei.network.NEIServerPacketHandler;
import codechicken.nei.util.NEIServerUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ServerHandler {

    public static final ServerHandler INSTANCE = new ServerHandler();

    @SubscribeEvent
    public void tickEvent(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.START && !event.world.isRemote && NEIServerConfig.dimTags.containsKey(event.world.provider.getDimension()))//fake worlds that don't call Load
        {
            processDisabledProperties(event.world);
        }
    }

    @SubscribeEvent
    public void loadEvent(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            NEIServerConfig.load(event.getWorld());
        }
    }

    private void processDisabledProperties(World world) {
        NEIServerUtils.advanceDisabledTimes(world);
        if (NEIServerUtils.isRaining(world) && NEIServerConfig.isActionDisabled(world.provider.getDimension(), "rain")) {
            NEIServerUtils.toggleRaining(world, false);
        }
    }

    @SubscribeEvent
    public void loginEvent(PlayerLoggedInEvent event) {
        NEIServerConfig.loadPlayer(event.player);
        NEIServerPacketHandler.sendServerSideCheck((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void logoutEvent(PlayerLoggedOutEvent event) {
        NEIServerConfig.unloadPlayer(event.player);
    }

    @SubscribeEvent
    public void dimChangeEvent(PlayerChangedDimensionEvent event) {
        NEIServerConfig.getSaveForPlayer(event.player.getName()).onWorldReload();
    }

    @SubscribeEvent
    public void loginEvent(PlayerRespawnEvent event) {
        NEIServerConfig.getSaveForPlayer(event.player.getName()).onWorldReload();
    }
}

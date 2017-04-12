package codechicken.nei.handler;

import codechicken.nei.NEIServerConfig;
import codechicken.nei.PlayerSave;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

/**
 * Created by covers1624 on 12/04/2017.
 */
public class NEIServerEventHandler {

    @SubscribeEvent (priority = EventPriority.LOW)// We need to fire after other handlers.
    public void tickEvent(TickEvent.PlayerTickEvent event) {

        if (event.phase == Phase.START && event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            PlayerSave save = NEIServerConfig.getSaveForPlayer(player.getName());
            if (save == null) {
                return;
            }
            save.updateOpChange();
            save.save();
        }
    }

}

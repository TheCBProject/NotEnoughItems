package codechicken.nei.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.nei.ServerHandler;
import codechicken.nei.handler.MagnetModeHandler;
import codechicken.nei.handler.NEIServerEventHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.network.NEIServerPacketHandler;
import codechicken.nei.widget.action.NEIActions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by covers1624 on 29/03/2017.
 */
public class Proxy {

    public void preInit(FMLPreInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new NEIServerEventHandler());
    }

    public void init(FMLInitializationEvent event) {

        PacketCustom.assignHandler(NEIClientPacketHandler.channel, new NEIServerPacketHandler());
        MinecraftForge.EVENT_BUS.register(ServerHandler.INSTANCE);
        //Item.getItemFromBlock(Blocks.MOB_SPAWNER).setHasSubtypes(true);//TODO
        NEIActions.init();

        MinecraftForge.EVENT_BUS.register(MagnetModeHandler.INSTANCE);
    }

    public RuntimeException throwCME(final String message) {
        throw new RuntimeException(message);
    }

    public void loadComplete(FMLLoadCompleteEvent event) {

    }
}

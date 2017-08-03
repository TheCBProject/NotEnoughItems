package codechicken.nei.proxy;

import codechicken.lib.packet.PacketCustom;
import codechicken.nei.ClientHandler;
import codechicken.nei.ItemMobSpawner;
import codechicken.nei.client.render.WorldOverlayRenderer;
import codechicken.nei.config.KeyBindings;
import codechicken.nei.handler.KeyManager;
import codechicken.nei.handler.NEIClientEventHandler;
import codechicken.nei.init.NEIInitialization;
import codechicken.nei.jei.gui.ContainerEventHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by covers1624 on 29/03/2017.
 */
public class ProxyClient extends Proxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ItemMobSpawner.register();
        MinecraftForge.EVENT_BUS.register(NEIClientEventHandler.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ItemMobSpawner.initRender();
        PacketCustom.assignHandler(NEIClientPacketHandler.channel, new NEIClientPacketHandler());

        MinecraftForge.EVENT_BUS.register(new ContainerEventHandler());
        MinecraftForge.EVENT_BUS.register(ClientHandler.INSTANCE);

        MinecraftForge.EVENT_BUS.register(WorldOverlayRenderer.INSTANCE);
        KeyManager.trackers.add(WorldOverlayRenderer.INSTANCE);

        KeyBindings.register();
        NEIClientEventHandler.INSTANCE.init();
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        NEIInitialization.bootNEI();
    }

    @Override
    public RuntimeException throwCME(final String message) {
        final GuiScreen errorGui = new GuiErrorScreen(null, null) {
            @Override
            public void handleMouseInput() {
            }

            @Override
            public void handleKeyboardInput() {
            }

            @Override
            public void drawScreen(int par1, int par2, float par3) {
                drawDefaultBackground();
                String[] s_msg = message.split("\n");
                for (int i = 0; i < s_msg.length; ++i) {
                    drawCenteredString(fontRenderer, s_msg[i], width / 2, height / 3 + 12 * i, 0xFFFFFFFF);
                }
            }
        };

        @SuppressWarnings ("serial")
        CustomModLoadingErrorDisplayException e = new CustomModLoadingErrorDisplayException() {
            @Override
            public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
                Minecraft.getMinecraft().displayGuiScreen(errorGui);
            }

            @Override
            public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            }
        };
        throw e;
    }

}

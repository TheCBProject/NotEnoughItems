package codechicken.nei;

import codechicken.lib.util.ClientUtils;
import codechicken.nei.client.render.WorldOverlayRenderer;
import codechicken.nei.handler.KeyManager;
import codechicken.nei.handler.MagnetModeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly (Side.CLIENT)
public class ClientHandler {

    public static final ClientHandler INSTANCE = new ClientHandler();

    private World lastworld;
    private GuiScreen lastGui;

    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            if (loadWorld(mc.world)) {
                NEIClientConfig.setHasSMPCounterPart(false);
                NEIClientConfig.setInternalEnabled(false);

                if (!Minecraft.getMinecraft().isSingleplayer() && ClientUtils.inWorld())//wait for server to initiate in singleplayer
                {
                    NEIClientConfig.loadWorld("remote/" + ClientUtils.getServerIP().replace(':', '~'));
                }
            }

            if (!NEIClientConfig.isEnabled()) {
                return;
            }

            KeyManager.tickKeyStates();

            if (mc.currentScreen == null) {
                NEIController.processCreativeCycling(mc.player.inventory);
            }
        }

        GuiScreen gui = mc.currentScreen;
        if (gui != lastGui) {
            if (gui instanceof GuiMainMenu) {
                lastworld = null;
            } else if (gui instanceof GuiWorldSelection) {
                NEIClientConfig.reloadSaves();
            }
        }
        lastGui = gui;
    }

    /**
     * Called to reset specific handlers and cache last world the client was in.
     *
     * @param world The world being loaded.
     * @return If any actions were performed.
     */
    public boolean loadWorld(World world) {
        if (world != lastworld) {
            MagnetModeHandler.INSTANCE.nukeClientMagnetTracker();
            WorldOverlayRenderer.reset();
            lastworld = world;
            return true;
        }
        return false;
    }
}

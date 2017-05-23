package codechicken.nei.asm;

import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 9/05/2017.
 */
public class ASMHooks {

    public interface IContainerForegroundRenderHook {

        void draw(GuiContainer gui);
    }

    private static List<IContainerForegroundRenderHook> containerForegroundRenderHooks = new LinkedList<>();

    public static void addContainerForegroundHook(IContainerForegroundRenderHook hook) {
        containerForegroundRenderHooks.add(hook);
    }

    public static void handleForegroundRender(GuiContainer container) {
        for (IContainerForegroundRenderHook hook : containerForegroundRenderHooks) {
            hook.draw(container);
        }
    }
}

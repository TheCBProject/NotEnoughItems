package codechicken.nei.jei.gui;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.SearchField;
import codechicken.nei.jei.EnumItemBrowser;
import codechicken.nei.jei.JEIIntegrationManager;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

/**
 * Created by covers1624 on 7/24/2016.
 * <p/>
 * Used to sniff input from events before JEI cancels them.
 */
public class ContainerEventHandler {

    private long lastSearchBoxClickTime;

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)//We need to be called before JEI.
    public void onGuiMouseEventpre(MouseInputEvent.Pre event) {
        if (Mouse.getEventButton() == -1 || event.getGui() == null || !Mouse.getEventButtonState()) {
            return;
        }
        GuiScreen guiScreen = event.getGui();
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        if (fieldFilter != null && fieldFilter.isMouseOver(Mouse.getEventX() * guiScreen.width / guiScreen.mc.displayWidth, guiScreen.height - Mouse.getEventY() * guiScreen.height / guiScreen.mc.displayHeight - 1)) {
            if (fieldFilter.isFocused() && (System.currentTimeMillis() - lastSearchBoxClickTime < 500)) {//double click
                NEIClientConfig.world.nbt.setBoolean("searchinventories", !SearchField.searchInventories());
                NEIClientConfig.world.saveNBT();
                lastSearchBoxClickTime = 0L;
            }
            lastSearchBoxClickTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)//we need to be called after JEI has registered the key press and updated the search box.
    public void onKeyTypedPost(KeyboardInputEvent.Post event) {
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        if (fieldFilter != null && JEIIntegrationManager.searchBoxOwner == EnumItemBrowser.JEI) {
            NEIClientConfig.setSearchExpression(fieldFilter.getText(), false);
            LayoutManager.searchField.setText(fieldFilter.getText(), false);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)//We need to be called after JEI as this is is a render overlay.
    public void onDrawBackgroundEventPost(BackgroundDrawnEvent event) {
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        if (fieldFilter == null || !SearchField.searchInventories() || JEIIntegrationManager.searchBoxOwner != EnumItemBrowser.JEI) {
            return;
        }

        int x = fieldFilter.xPosition;
        int y = fieldFilter.yPosition;
        int h = fieldFilter.height;
        int w = fieldFilter.width;

        GuiDraw.drawGradientRect(x - 1, y - 1, 1, h + 2, 0xFFFFFF00, 0xFFC0B000);//Left
        GuiDraw.drawGradientRect(x - 1, y - 1, w + 2, 1, 0xFFFFFF00, 0xFFC0B000);//Top
        GuiDraw.drawGradientRect(x + w, y - 1, 1, h + 2, 0xFFFFFF00, 0xFFC0B000);//Left
        GuiDraw.drawGradientRect(x - 1, y + h, w + 2, 1, 0xFFFFFF00, 0xFFC0B000);//Bottom

    }

}

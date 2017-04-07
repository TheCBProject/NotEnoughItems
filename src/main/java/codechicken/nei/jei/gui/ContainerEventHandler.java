package codechicken.nei.jei.gui;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.util.ClientUtils;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.jei.EnumItemBrowser;
import codechicken.nei.jei.JEIIntegrationManager;
import codechicken.nei.widget.SearchField;
import mezz.jei.gui.ItemListOverlayInternal;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.util.MouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

/**
 * Created by covers1624 on 7/24/2016.
 * <p/>
 * Used to sniff input from events before JEI cancels them.
 */
@SideOnly (Side.CLIENT)
public class ContainerEventHandler {

    private long lastSearchBoxClickTime;

    @SubscribeEvent (priority = EventPriority.LOWEST, receiveCanceled = true)//We need to be called before JEI.
    public void onGuiMouseEventpre(MouseInputEvent.Pre event) {
        if (Mouse.getEventButton() == -1 || event.getGui() == null || !Mouse.getEventButtonState()) {
            return;
        }
        MouseHelper mouse = new MouseHelper();
        int eventButton = Mouse.getEventButton();
        if (JEIIntegrationManager.searchBoxOwner == EnumItemBrowser.JEI) {
            GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
            if (fieldFilter != null && fieldFilter.isMouseOver(mouse.getX(), mouse.getY())) {
                if (eventButton == 0) {
                    if (fieldFilter.isFocused() && (System.currentTimeMillis() - lastSearchBoxClickTime < 500)) {//double click
                        NEIClientConfig.world.nbt.setBoolean("searchinventories", !SearchField.searchInventories());
                        NEIClientConfig.world.saveNBT();
                        lastSearchBoxClickTime = 0L;
                    } else {
                        lastSearchBoxClickTime = System.currentTimeMillis();
                    }
                } else if (eventButton == 1) {
                    NEIClientConfig.setSearchExpression("", false);
                    LayoutManager.searchField.setText("", false);
                }
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST, receiveCanceled = true)//we need to be called after JEI has registered the key press and updated the search box.
    public void onKeyTypedPost(KeyboardInputEvent.Post event) {
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        if (fieldFilter != null && JEIIntegrationManager.searchBoxOwner == EnumItemBrowser.JEI && isNEIInWorld() && fieldFilter.isFocused()) {
            NEIClientConfig.setSearchExpression(fieldFilter.getText(), false);
            LayoutManager.searchField.setText(fieldFilter.getText(), false);
        }
    }

    private IClickedIngredient<?> getIngeredientUnderMouseForKey() {
        ItemListOverlayInternal internal = JEIIntegrationManager.getItemListOverlayInternal();
        if (internal == null) {
            return null;
        }
        MouseHelper helper = new MouseHelper();
        return internal.getIngredientUnderMouse(helper.getX(), helper.getY());
    }

    private boolean isContainerTextFieldFocused() {
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        GuiTextField textField = null;

        if (gui instanceof GuiContainerCreative) {
            textField = ((GuiContainerCreative) gui).searchField;
        } else if (gui instanceof GuiRepair) {
            textField = ((GuiRepair) gui).nameField;
        }

        if (JEIIntegrationManager.searchBoxOwner == EnumItemBrowser.NEI) {
            return isFocussed(textField) || LayoutManager.searchField.focused();
        }

        return isFocussed(textField) || isFocussed(fieldFilter);
    }

    private boolean isFocussed(GuiTextField textField) {
        return textField != null && textField.getVisible() && textField.isEnabled && textField.isFocused();
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)//We need to be called after JEI as this is is a render overlay.
    public void onDrawBackgroundEventPost(BackgroundDrawnEvent event) {
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        if (!ClientUtils.inWorld() || !isNEIInWorld() || fieldFilter == null || !SearchField.searchInventories() || JEIIntegrationManager.searchBoxOwner != EnumItemBrowser.JEI) {
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

    private boolean isNEIInWorld() {
        return NEIClientConfig.world != null && NEIClientConfig.world.nbt != null;
    }

}

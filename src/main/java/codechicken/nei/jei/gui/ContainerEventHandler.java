package codechicken.nei.jei.gui;

import codechicken.lib.asm.ObfMapping;
import codechicken.lib.gui.GuiDraw;
import codechicken.lib.util.ReflectionManager;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.SearchField;
import codechicken.nei.jei.EnumItemBrowser;
import codechicken.nei.jei.JEIIntegrationManager;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.util.NEIClientUtils;
import mezz.jei.GuiEventHandler;
import mezz.jei.JeiStarter;
import mezz.jei.JustEnoughItems;
import mezz.jei.config.Config;
import mezz.jei.gui.ItemListOverlayInternal;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.input.InputHandler;
import mezz.jei.util.MouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Created by covers1624 on 7/24/2016.
 * <p/>
 * Used to sniff input from events before JEI cancels them.
 */
public class ContainerEventHandler {

    private long lastSearchBoxClickTime;

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)//We need to be called before JEI.
    public void onGuiMouseEventpre(MouseInputEvent.Pre event) {
        if (Mouse.getEventButton() == -1 || event.getGui() == null || !Mouse.getEventButtonState()) {
            return;
        }
        if (JEIIntegrationManager.searchBoxOwner == EnumItemBrowser.JEI) {
            GuiScreen guiScreen = event.getGui();
            GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
            if (fieldFilter != null && fieldFilter.isMouseOver(Mouse.getEventX() * guiScreen.width / guiScreen.mc.displayWidth, guiScreen.height - Mouse.getEventY() * guiScreen.height / guiScreen.mc.displayHeight - 1)) {
                if (fieldFilter.isFocused() && (System.currentTimeMillis() - lastSearchBoxClickTime < 500)) {//double click
                    NEIClientConfig.world.nbt.setBoolean("searchinventories", !SearchField.searchInventories());
                    NEIClientConfig.world.saveNBT();
                    lastSearchBoxClickTime = 0L;
                } else {
                    lastSearchBoxClickTime = System.currentTimeMillis();
                }
            }
        }
        int eventKey = Mouse.getEventButton();
        if (JEIIntegrationManager.itemPannelOwner == EnumItemBrowser.JEI && Minecraft.getMinecraft().thePlayer != null) {
            if (!Config.isCheatItemsEnabled() && Minecraft.getMinecraft().currentScreen instanceof GuiContainer) {
                if (!isContainerTextFieldFocused()) {
                    IClickedIngredient ingredient = getIngeredientUnderMouseForKey();
                    if (ingredient != null) {
                        if (eventKey == 1 || (eventKey == 0 && NEIClientUtils.shiftKey())) {
                            GuiUsageRecipe.openRecipeGui("item", ingredient.getValue());
                        }
                        if (eventKey == 0) {
                            GuiCraftingRecipe.openRecipeGui("item", ingredient.getValue());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)//we need to be called after JEI has registered the key press and updated the search box.
    public void onKeyTypedPost(KeyboardInputEvent.Post event) {
        GuiTextFieldFilter fieldFilter = JEIIntegrationManager.getTextFieldFilter();
        if (fieldFilter != null && JEIIntegrationManager.searchBoxOwner == EnumItemBrowser.JEI) {
            NEIClientConfig.setSearchExpression(fieldFilter.getText(), false);
            LayoutManager.searchField.setText(fieldFilter.getText(), false);
        }

        if (JEIIntegrationManager.itemPannelOwner == EnumItemBrowser.JEI && Minecraft.getMinecraft().thePlayer != null) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer) {
                if (!event.isCanceled()) {
                    if (!isContainerTextFieldFocused()) {
                        IClickedIngredient ingredient = getIngeredientUnderMouseForKey();
                        if (ingredient != null) {
                            int eventKey = Keyboard.getEventKey();
                            if (eventKey == NEIClientConfig.getKeyBinding("gui.usage") || (eventKey == NEIClientConfig.getKeyBinding("gui.recipe") && NEIClientUtils.shiftKey())) {
                                GuiUsageRecipe.openRecipeGui("item", ingredient.getValue());
                            }
                            if (eventKey == NEIClientConfig.getKeyBinding("gui.recipe")) {
                                GuiCraftingRecipe.openRecipeGui("item", ingredient.getValue());
                            }
                        }

                    }
                }
            }
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

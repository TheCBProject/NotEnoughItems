package codechicken.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.api.API;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.config.KeyBindings;
import codechicken.nei.gui.GuiExtendedCreativeInv;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.guihook.IContainerObjectHandler;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.guihook.IInputHandler;
import codechicken.nei.handler.KeyManager;
import codechicken.nei.handler.KeyManager.IKeyStateTracker;
import codechicken.nei.handler.NEIClientEventHandler;
import codechicken.nei.layout.LayoutStyle;
import codechicken.nei.layout.LayoutStyleMinecraft;
import codechicken.nei.layout.LayoutStyleTMIOld;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.ItemInfo;
import codechicken.nei.util.ItemList;
import codechicken.nei.util.LogHelper;
import codechicken.nei.util.helper.GuiHelper;
import codechicken.nei.widget.*;
import codechicken.nei.widget.action.NEIActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static codechicken.lib.gui.GuiDraw.drawRect;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;
import static codechicken.lib.texture.TextureUtils.changeTexture;
import static codechicken.nei.NEIClientConfig.*;
import static codechicken.nei.util.NEIClientUtils.*;

public class LayoutManager implements IInputHandler, IContainerTooltipHandler, IContainerDrawHandler, IContainerObjectHandler, IKeyStateTracker {

    private static LayoutManager instance;

    private static Widget inputFocused;
    /**
     * Sorted bottom first
     */
    private static TreeSet<Widget> drawWidgets = new TreeSet<>(new WidgetZOrder(false));
    /**
     * Sorted top first
     */
    private static TreeSet<Widget> controlWidgets = new TreeSet<>(new WidgetZOrder(true));

    private static boolean showItemPanel;

    public static ItemPanel itemPanel;
    public static SubsetWidget dropDown;
    public static TextField searchField;

    public static Button options;

    public static Button prev;
    public static Button next;
    public static Label pageLabel;
    public static Button more;
    public static Button less;
    public static ItemQuantityField quantity;

    public static SaveLoadButton[] stateButtons;
    public static Button[] deleteButtons;

    public static Button delete;
    public static ButtonCycled gamemode;
    public static Button rain;
    public static Button magnet;
    public static Button[] timeButtons = new Button[4];
    public static Button heal;

    public static HashMap<Integer, LayoutStyle> layoutStyles = new HashMap<>();

    public static void load() {
        API.addLayoutStyle(0, new LayoutStyleMinecraft());
        API.addLayoutStyle(1, new LayoutStyleTMIOld());

        instance = new LayoutManager();
        KeyManager.trackers.add(instance);
        NEIClientEventHandler.addInputHandler(instance);
        NEIClientEventHandler.addTooltipHandler(instance);
        NEIClientEventHandler.addDrawHandler(instance);
        NEIClientEventHandler.addObjectHandler(instance);
        init();
    }

    @Override
    public void onMouseClicked(GuiScreen gui, int mouseX, int mouseY, int button) {
        if (isHidden()) {
            return;
        }

        for (Widget widget : controlWidgets) {
            widget.onGuiClick(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(GuiScreen gui, int mouseX, int mouseY, int button) {
        if (isHidden()) {
            return false;
        }

        if (!isEnabled()) {
            return options.contains(mouseX, mouseY) && options.handleClick(mouseX, mouseY, button);
        }

        for (Widget widget : controlWidgets) {
            widget.onGuiClick(mouseX, mouseY);
            if (widget.contains(mouseX, mouseY) ? widget.handleClick(mouseX, mouseY, button) : widget.handleClickExt(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean objectUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (!isHidden() && isEnabled()) {
            for (Widget widget : controlWidgets) {
                if (widget.contains(mousex, mousey)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean keyTyped(GuiScreen gui, char keyChar, int keyID) {
        if (isEnabled() && !isHidden()) {
            if (inputFocused != null) {
                return inputFocused.handleKeyPress(keyID, keyChar);
            }

            for (Widget widget : controlWidgets) {
                if (widget.handleKeyPress(keyID, keyChar)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onKeyTyped(GuiScreen gui, char keyChar, int keyID) {
    }

    @Override
    public boolean lastKeyTyped(GuiScreen gui, char keyChar, int keyID) {
        if (KeyBindings.get("nei.options.keys.gui.hide").isActiveAndMatches(keyID)) {
            toggleBooleanSetting("inventory.hidden");
            return true;
        }
        if (isEnabled() && !isHidden()) {
            for (Widget widget : controlWidgets) {
                if (inputFocused == null) {
                    widget.lastKeyTyped(keyID, keyChar);
                }
            }
        }
        return false;
    }

    @Override
    public void onMouseUp(GuiScreen gui, int mouseX, int mouseY, int button) {
        if (!isHidden() && isEnabled()) {
            for (Widget widget : controlWidgets) {
                widget.mouseUp(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void onMouseDragged(GuiScreen gui, int mouseX, int mouseY, int button, long heldTime) {
        if (!isHidden() && isEnabled()) {
            for (Widget widget : controlWidgets) {
                widget.mouseDragged(mouseX, mouseY, button, heldTime);
            }
        }
    }

    @Override
    public ItemStack getStackUnderMouse(GuiContainer gui, int mousex, int mousey) {
        if (!isHidden() && isEnabled()) {
            for (Widget widget : controlWidgets) {
                ItemStack stack = widget.getStackMouseOver(mousex, mousey);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void renderObjects(GuiContainer gui, int mousex, int mousey) {
        if (!isHidden()) {
            layout(gui);
            if (isEnabled()) {
                getLayoutStyle().drawBackground(gui);
                for (Widget widget : drawWidgets) {
                    widget.draw(mousex, mousey);
                }
            } else {
                options.draw(mousex, mousey);
            }

            GlStateManager.enableLighting();
            GlStateManager.disableDepth();
        } else {
            showItemPanel = false;
        }
    }

    @Override
    public void postRenderObjects(GuiContainer gui, int mousex, int mousey) {
        if (!isHidden() && isEnabled()) {
            for (Widget widget : drawWidgets) {
                widget.postDraw(mousex, mousey);
            }
        }
    }

    @Override
    public void handleTooltip(GuiScreen gui, int mousex, int mousey, List<String> currenttip) {
        if (!isHidden() && isEnabled() && GuiHelper.shouldShowTooltip(gui)) {
            for (Widget widget : controlWidgets) {
                widget.handleTooltip(mousex, mousey, currenttip);
            }
        }
    }

    @Override
    public void handleItemDisplayName(GuiScreen gui, ItemStack stack, List<String> currenttip) {
        //TODO, Implement this in a cleaner way.
//        String overridename = ItemInfo.getNameOverride(stack);
//        if (overridename != null) {
//            currenttip.set(0, overridename);
//        }
//
//        String mainname = currenttip.get(0);
//        if (showIDs()) {
//            mainname += " " + Item.getIdFromItem(stack.getItem());
//            if (stack.getItemDamage() != 0) {
//                mainname += ":" + stack.getItemDamage();
//            }
//
//            currenttip.set(0, mainname);
//        }
    }

    public static void layout(GuiContainer gui) {
        VisibilityData visiblity = new VisibilityData();
        if (isHidden()) {
            //showItemPanel = false;
            visiblity.showNEI = false;
        }
        if (gui.height - gui.getYSize() <= 40) {
            visiblity.showSearchSection = false;
        }
        if (gui.getGuiLeft() - 4 < 76) {
            visiblity.showWidgets = false;
        }

        for (INEIGuiHandler handler : GuiInfo.guiHandlers) {
            handler.modifyVisibility(gui, visiblity);
        }

        visiblity.translateDependancies();

        getLayoutStyle().layout(gui, visiblity);

        updateWidgetVisiblities(gui, visiblity);
    }

    private static void init() {
        itemPanel = new ItemPanel();
        dropDown = new SubsetWidget();
        searchField = new SearchField("search");

        options = new Button("Options") {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (!rightclick) {
                    getOptionList().openGui(getGuiContainer(), false);
                    return true;
                }
                return false;
            }

            @Override
            public String getRenderLabel() {
                return translate("inventory.options");
            }
        };
        prev = new Button("Prev") {
            public boolean onButtonPress(boolean rightclick) {
                if (!rightclick) {
                    LayoutManager.itemPanel.scroll(-1);
                    return true;
                }
                return false;
            }

            @Override
            public String getRenderLabel() {
                return translate("inventory.prev");
            }
        };
        next = new Button("Next") {
            public boolean onButtonPress(boolean rightclick) {
                if (!rightclick) {
                    LayoutManager.itemPanel.scroll(1);
                    return true;
                }
                return false;
            }

            @Override
            public String getRenderLabel() {
                return translate("inventory.next");
            }
        };
        pageLabel = new Label("(0/0)", true);
        more = new Button("+") {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (rightclick) {
                    return false;
                }

                int modifier = controlKey() ? 64 : shiftKey() ? 10 : 1;

                int quantity = getItemQuantity() + modifier;
                if (quantity < 0) {
                    quantity = 0;
                }

                setItemQuantity(quantity);
                return true;
            }
        };
        less = new Button("-") {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (rightclick) {
                    return false;
                }

                int modifier = controlKey() ? -64 : shiftKey() ? -10 : -1;

                int quantity = getItemQuantity() + modifier;
                if (quantity < 0) {
                    quantity = 0;
                }

                setItemQuantity(quantity);
                return true;
            }
        };
        quantity = new ItemQuantityField("quantity");

        stateButtons = new SaveLoadButton[7];
        deleteButtons = new Button[7];

        for (int i = 0; i < 7; i++) {
            final int savestate = i;
            stateButtons[i] = new SaveLoadButton("") {
                @Override
                public boolean onButtonPress(boolean rightclick) {
                    if (isStateSaved(savestate)) {
                        loadState(savestate);
                    } else {
                        saveState(savestate);
                    }
                    return true;
                }

                @Override
                public void onTextChange() {
                    NBTTagCompound statelist = global.nbt.getCompoundTag("statename");
                    global.nbt.setTag("statename", statelist);

                    statelist.setString("" + savestate, label);
                    global.saveNBT();
                }
            };
            deleteButtons[i] = new Button("x") {
                @Override
                public boolean onButtonPress(boolean rightclick) {
                    if (!rightclick) {
                        clearState(savestate);
                        return true;
                    }
                    return false;
                }
            };
        }

        delete = new Button() {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if ((state & 0x3) == 2) {
                    return false;
                }

                ItemStack held = getHeldItem();
                if (!held.isEmpty()) {
                    if (shiftKey()) {
                        deleteHeldItem();
                        deleteItemsOfType(held);
                    } else if (rightclick) {
                        decreaseSlotStack(-999);
                    } else {
                        deleteHeldItem();
                    }
                } else if (shiftKey()) {
                    deleteEverything();
                } else {
                    NEIController.toggleDeleteMode();
                }

                return true;
            }

            public String getButtonTip() {
                if ((state & 0x3) != 2) {
                    if (shiftKey()) {
                        return translate("inventory.delete.inv");
                    }
                    if (NEIController.canUseDeleteMode()) {
                        return getStateTip("delete", state);
                    }
                }
                return null;
            }

            @Override
            public void postDraw(int mousex, int mousey) {
                if (contains(mousex, mousey) && !getHeldItem().isEmpty() && (state & 0x3) != 2) {
                    GuiDraw.drawTip(mousex + 9, mousey, translate("inventory.delete." + (shiftKey() ? "all" : "one"), GuiHelper.itemDisplayNameShort(getHeldItem())));
                }
            }
        };
        gamemode = new ButtonCycled() {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (!rightclick) {
                    cycleGamemode();
                    return true;
                }
                return false;
            }

            public String getButtonTip() {
                return translate("inventory.gamemode." + getNextGamemode());
            }
        };
        gamemode.icons = new Rectangle4i[3];
        rain = new Button() {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (handleDisabledButtonPress("rain", rightclick)) {
                    return true;
                }

                if (!rightclick) {
                    toggleRaining();
                    return true;
                }
                return false;
            }

            public String getButtonTip() {
                return getStateTip("rain", state);
            }
        };
        magnet = new Button() {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (!rightclick) {
                    toggleMagnetMode();
                    return true;
                }
                return false;
            }

            public String getButtonTip() {
                return getStateTip("magnet", state);
            }
        };
        for (int i = 0; i < 4; i++) {
            final int zone = i;
            timeButtons[i] = new Button() {
                @Override
                public boolean onButtonPress(boolean rightclick) {
                    if (handleDisabledButtonPress(NEIActions.timeZones[zone], rightclick)) {
                        return true;
                    }

                    if (!rightclick) {
                        setHourForward(zone * 6);
                        return true;
                    }
                    return false;
                }

                @Override
                public String getButtonTip() {
                    return getTimeTip(NEIActions.timeZones[zone], state);
                }

            };
        }
        heal = new Button() {
            @Override
            public boolean onButtonPress(boolean rightclick) {
                if (!rightclick) {
                    healPlayer();
                    return true;
                }
                return false;
            }

            @Override
            public String getButtonTip() {
                return translate("inventory.heal");
            }
        };

        delete.state |= 0x4;
        gamemode.state |= 0x4;
        rain.state |= 0x4;
        magnet.state |= 0x4;
    }

    private static String getStateTip(String name, int state) {
        String sfx = (state & 0x3) == 2 ? "enable" : (state & 0x3) == 1 ? "0" : "1";

        return translate("inventory." + name + "." + sfx);
    }

    private static String getTimeTip(String name, int state) {
        String sfx = (state & 0x3) == 2 ? "enable" : "set";
        return translate("inventory." + name + "." + sfx);
    }

    private static boolean handleDisabledButtonPress(String ident, boolean rightclick) {
        if (!NEIActions.canDisable.contains(ident)) {
            return false;
        }
        if (rightclick != disabledActions.contains(ident)) {
            return setPropertyDisabled(ident, rightclick);
        }
        return false;
    }

    private static boolean setPropertyDisabled(String ident, boolean disable) {
        if (disable && NEIActions.base(ident).equals("time")) {
            int count = 0;
            for (int i = 0; i < 4; i++) {
                if (disabledActions.contains(NEIActions.timeZones[i])) {
                    count++;
                }
            }
            if (count == 3) {
                return false;
            }
        }
        if (hasSMPCounterPart()) {
            NEIClientPacketHandler.sendActionDisableStateChange(ident, disable);
        }

        return true;
    }

    @Override
    public void load(GuiContainer gui) {
        if (isEnabled()) {
            setInputFocused(null);

            ItemList.loadItems.restart();

            getLayoutStyle().init();
            layout(gui);
        }

        NEIController.load(gui);

        if (checkCreativeInv(gui) && gui.mc.currentScreen instanceof GuiContainerCreative)//override creative with creative+
        {
            gui.mc.displayGuiScreen(null);//close the screen and wait for the server to open it for us
        }
    }

    @Override
    public void refresh(GuiContainer gui) {
    }

    public boolean checkCreativeInv(GuiContainer gui) {
        if (gui instanceof GuiContainerCreative && invCreativeMode()) {
            NEIClientPacketHandler.sendCreativeInv(true);
            return true;
        } else if (gui instanceof GuiExtendedCreativeInv && !invCreativeMode()) {
            NEIClientPacketHandler.sendCreativeInv(false);
            return true;
        }
        return false;
    }

    public static void updateWidgetVisiblities(GuiContainer gui, VisibilityData visiblity) {
        Set<Widget> newWidgets = new HashSet<>();

        if (!visiblity.showNEI) {
            //showItemPanel = false;
            return;
        }

        newWidgets.add(options);
        showItemPanel = visiblity.showItemPanel;
        if (visiblity.showItemPanel) {
            newWidgets.add(itemPanel);
            newWidgets.add(prev);
            newWidgets.add(next);
            newWidgets.add(pageLabel);
            if (canPerformAction("item")) {
                newWidgets.add(more);
                newWidgets.add(less);
                newWidgets.add(quantity);
            }
        }

        if (visiblity.showSearchSection) {
            newWidgets.add(dropDown);
            newWidgets.add(searchField);
        }

        if (canPerformAction("item") && hasSMPCounterPart() && visiblity.showStateButtons) {
            for (int i = 0; i < 7; i++) {
                newWidgets.add(stateButtons[i]);
                if (isStateSaved(i)) {
                    newWidgets.add(deleteButtons[i]);
                }
            }
        }
        if (visiblity.showUtilityButtons) {
            if (canPerformAction("time")) {
                newWidgets.addAll(Arrays.asList(timeButtons).subList(0, 4));
            }
            if (canPerformAction("rain")) {
                newWidgets.add(rain);
            }
            if (canPerformAction("heal")) {
                newWidgets.add(heal);
            }
            if (canPerformAction("magnet")) {
                newWidgets.add(magnet);
            }
            if (isValidGamemode("creative") || isValidGamemode("creative+") || isValidGamemode("adventure")) {
                newWidgets.add(gamemode);
            }
            if (canPerformAction("delete")) {
                newWidgets.add(delete);
            }
        }

        TreeSet<Widget> newDrawWidgets = new TreeSet<>(new WidgetZOrder(false));
        TreeSet<Widget> newControlWidgets = new TreeSet<>(new WidgetZOrder(true));
        newDrawWidgets.addAll(newWidgets);
        newControlWidgets.addAll(newWidgets);

        drawWidgets = newDrawWidgets;
        controlWidgets = newControlWidgets;
    }

    public static LayoutStyle getLayoutStyle(int id) {
        LayoutStyle style = layoutStyles.get(id);
        if (style == null) {
            style = layoutStyles.get(0);
        }
        return style;
    }

    public static LayoutStyle getLayoutStyle() {
        return getLayoutStyle(NEIClientConfig.getLayoutStyle());
    }

    @Deprecated//TODO, This is Un-Synchronized. We throw errors if we add widgets this way.
    private static void addWidget(Widget widget) {
        drawWidgets.add(widget);
        controlWidgets.add(widget);
    }

    @Override
    public void guiTick(GuiContainer gui) {
        if (checkCreativeInv(gui)) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        for (Widget widget : controlWidgets) {
            widget.update();
        }
    }

    @Override
    public boolean mouseScrolled(GuiScreen gui, int mouseX, int mouseY, int scrolled) {
        if (isHidden() || !isEnabled()) {
            return false;
        }

        for (Widget widget : controlWidgets) {
            if (widget.onMouseWheel(scrolled, mouseX, mouseY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldShowTooltip(GuiScreen gui) {
        return itemPanel.draggedStack.isEmpty() && gui instanceof GuiContainer;
    }

    public static Widget getInputFocused() {
        return inputFocused;
    }

    public static void setInputFocused(Widget widget) {
        if (inputFocused != null) {
            inputFocused.loseFocus();
        }

        inputFocused = widget;
        if (inputFocused != null) {
            inputFocused.gainFocus();
        }
    }

    @Override
    public void renderSlotOverlay(GuiContainer window, Slot slot) {
        ItemStack item = slot.getStack();
        if (world.nbt.getBoolean("searchinventories") && (item == null ? !getSearchExpression().equals("") : !ItemList.getItemListFilter().matches(item))) {
            GlStateManager.disableLighting();
            //GlStateManager.depthFunc(GL11.GL_EQUAL);
            GlStateManager.translate(0, 0, 350);
            drawRect(slot.xPos, slot.yPos, 16, 16, 0x80000000);
            GlStateManager.translate(0, 0, -350);
            //GlStateManager.depthFunc(GL11.GL_LESS);
            GlStateManager.enableLighting();
        }
    }

    public static void drawIcon(int x, int y, Rectangle4i image) {
        changeTexture("nei:textures/nei_sprites.png");
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawTexturedModalRect(x, y, image.x, image.y, image.w, image.h);
        GlStateManager.disableBlend();
    }

    public static void drawButtonBackground(int x, int y, int w, int h, boolean edges, int type) {
        int wtiles = 0;
        int ew = w;//end width
        if (w / 2 > 100) {
            wtiles = (w - 200) / 50 + 1;
            ew = 200;
        }

        int w1 = ew / 2;
        int h1 = h / 2;
        int w2 = (ew + 1) / 2;
        int h2 = (h + 1) / 2;

        int x2 = x + w - w2;
        int y2 = y + h - h2;

        int ty = 46 + type * 20;
        int te = (edges ? 0 : 1);//tex edges

        int ty1 = ty + te;
        int tx1 = te;
        int tx3 = 75;
        //halfway the 1 is for odd number adjustment
        int ty2 = ty + 20 - h2 - te;
        int tx2 = 200 - w2 - te;

        changeTexture("textures/gui/widgets.png");
        drawTexturedModalRect(x, y, tx1, ty1, w1, h1);//top left
        drawTexturedModalRect(x, y2, tx1, ty2, w1, h2);//bottom left

        for (int tile = 0; tile < wtiles; tile++) {
            int tilex = x + w1 + 50 * tile;
            drawTexturedModalRect(tilex, y, tx3, ty1, 50, h1);//top
            drawTexturedModalRect(tilex, y2, tx3, ty2, 50, h2);//bottom
        }

        drawTexturedModalRect(x2, y, tx2, ty1, w2, h1);//top right
        drawTexturedModalRect(x2, y2, tx2, ty2, w2, h2);//bottom right
    }

    public static LayoutManager instance() {
        return instance;
    }

    @Override
    public void tickKeyStates() {
        if (Minecraft.getMinecraft().currentScreen != null) {
            return;
        }

        if (KeyBindings.get("nei.options.keys.world.dawn").isPressed()) {
            timeButtons[0].onButtonPress(false);
        }
        if (KeyBindings.get("nei.options.keys.world.noon").isPressed()) {
            timeButtons[1].onButtonPress(false);
        }
        if (KeyBindings.get("nei.options.keys.world.dusk").isPressed()) {
            timeButtons[2].onButtonPress(false);
        }
        if (KeyBindings.get("nei.options.keys.world.midnight").isPressed()) {
            timeButtons[3].onButtonPress(false);
        }
        if (KeyBindings.get("nei.options.keys.world.rain").isPressed()) {
            rain.onButtonPress(false);
        }
        if (KeyBindings.get("nei.options.keys.world.heal").isPressed()) {
            heal.onButtonPress(false);
        }
        if (KeyBindings.get("nei.options.keys.world.creative").isPressed()) {
            gamemode.onButtonPress(false);
        }
    }

    public static boolean isItemPanelActive() {
        return showItemPanel;
    }
}

package codechicken.nei.handler;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.state.GlStateTracker;
import codechicken.nei.config.KeyBindings;
import codechicken.nei.guihook.IContainerDrawHandler;
import codechicken.nei.guihook.IContainerObjectHandler;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.guihook.IInputHandler;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.helper.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.PotionShiftEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static codechicken.nei.NEIClientConfig.canPerformAction;

/**
 * Created by covers1624 on 29/03/2017.
 */
@SideOnly (Side.CLIENT)
public class NEIClientEventHandler {

    public static NEIClientEventHandler INSTANCE = new NEIClientEventHandler();

    public static final LinkedList<IInputHandler> inputHandlers = new LinkedList<>();
    public static final LinkedList<IContainerObjectHandler> objectHandlers = new LinkedList<>();
    public static final LinkedList<IContainerDrawHandler> drawHandlers = new LinkedList<>();
    public static final LinkedList<IContainerTooltipHandler> tooltipHandlers = new LinkedList<>();
    private static List<IContainerTooltipHandler> instanceTooltipHandlers;
    private static GuiScreen lastGui;

    private final Set<Triple<Class<?>, Class<? extends GuiButton>, Integer>> bookStripList = new HashSet<>();

    /**
     * Register a new Input handler;
     *
     * @param handler The handler to register
     */
    public static void addInputHandler(IInputHandler handler) {

        inputHandlers.add(handler);
    }

    /**
     * Register a new Tooltip render handler;
     *
     * @param handler The handler to register
     */
    public static void addTooltipHandler(IContainerTooltipHandler handler) {
        tooltipHandlers.add(handler);
    }

    /**
     * Register a new Drawing handler;
     *
     * @param handler The handler to register
     */
    public static void addDrawHandler(IContainerDrawHandler handler) {
        drawHandlers.add(handler);
    }

    /**
     * Register a new Object handler;
     *
     * @param handler The handler to register
     */
    public static void addObjectHandler(IContainerObjectHandler handler) {
        objectHandlers.add(handler);
    }

    private NEIClientEventHandler() {
    }

    public void init() {
    }

    @SubscribeEvent (priority = EventPriority.HIGHEST)
    public void onKeyTypedPre(KeyboardInputEvent.Pre event) {

        GuiScreen gui = event.getGui();
        if (gui instanceof GuiContainer) {
            char c = Keyboard.getEventCharacter();
            int eventKey = Keyboard.getEventKey();

            if (eventKey == 0 && c >= 32 || Keyboard.getEventKeyState()) {
                //TODO, do we want canceled events to appear here?
                inputHandlers.forEach(handler -> handler.onKeyTyped(gui, c, eventKey));
                for (IInputHandler handler : inputHandlers) {
                    if (handler.keyTyped(gui, c, eventKey)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyTypedPost(KeyboardInputEvent.Post event) {

        GuiScreen gui = event.getGui();
        if (gui instanceof GuiContainer) {
            char c = Keyboard.getEventCharacter();
            int eventKey = Keyboard.getEventKey();

            if (eventKey == 0 && c >= 32 || Keyboard.getEventKeyState()) {

                if (eventKey != 1) {
                    for (IInputHandler inputhander : inputHandlers) {
                        if (inputhander.lastKeyTyped(gui, c, eventKey)) {
                            event.setCanceled(true);
                            return;
                        }
                    }
                }

                if (KeyBindings.get("nei.options.keys.gui.enchant").isActiveAndMatches(eventKey) && canPerformAction("enchant")) {
                    NEIClientPacketHandler.sendOpenEnchantmentWindow();
                    event.setCanceled(true);
                }
                if (KeyBindings.get("nei.options.keys.gui.potion").isActiveAndMatches(eventKey) && canPerformAction("potion")) {
                    NEIClientPacketHandler.sendOpenPotionWindow();
                    event.setCanceled(true);
                }
            }
        }

    }

    @SubscribeEvent (priority = EventPriority.HIGHEST)//TODO, DragN'Drop isn't working properly.
    public void onMouseEventPre(MouseInputEvent.Pre event) {

        GuiScreen gui = event.getGui();
        if (gui instanceof GuiContainer) {
            Point mousePos = GuiDraw.getMousePosition();
            int mouseButton = Mouse.getEventButton();

            if (Mouse.getEventButtonState()) {
                gui.lastMouseEvent = Minecraft.getSystemTime();

                inputHandlers.forEach(handler -> handler.onMouseClicked(gui, mousePos.x, mousePos.y, mouseButton));

                for (IInputHandler handler : inputHandlers) {
                    if (handler.mouseClicked(gui, mousePos.x, mousePos.y, mouseButton)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            } else if (mouseButton != -1) {
                inputHandlers.forEach(handler -> handler.onMouseUp(gui, mousePos.x, mousePos.y, mouseButton));
            } else if (mouseButton != -1 && gui.lastMouseEvent > 0) {
                long heldTime = Minecraft.getSystemTime() - gui.lastMouseEvent;
                inputHandlers.forEach(handler -> handler.onMouseDragged(gui, mousePos.x, mousePos.y, mouseButton, heldTime));
            }
        }
    }

    @SubscribeEvent
    public void onMouseEventPost(MouseInputEvent.Post event) {

        GuiScreen gui = event.getGui();
        if (gui instanceof GuiContainer) {
            Point mousePos = GuiDraw.getMousePosition();
            if (Mouse.getEventButtonState()) {
                int mouseButton = Mouse.getEventButton();
                inputHandlers.forEach(handler -> handler.onMouseClickedPost(gui, mousePos.x, mousePos.y, mouseButton));
            }

            int i = Mouse.getDWheel();
            if (i != 0) {
                int scrolled = i > 0 ? 1 : -1;
                for (IInputHandler handler : inputHandlers) {
                    if (handler.mouseScrolled(gui, mousePos.x, mousePos.y, scrolled)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

    }

    @SubscribeEvent
    public void potionShiftEvent(PotionShiftEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void containerInitEvent(GuiScreenEvent.InitGuiEvent.Pre event) {
        if (event.getGui() instanceof GuiContainer) {
            GuiContainer container = ((GuiContainer) event.getGui());
            objectHandlers.forEach(handler -> handler.load(container));
        }
    }

    @SubscribeEvent
    public void guiInitEvent(GuiScreenEvent.InitGuiEvent.Post event) {
//        for (Triple<Class<?>, Class<? extends GuiButton>, Integer> triple : bookStripList) {
//            if (event.getGui().getClass() == triple.getLeft()){
//                event.getButtonList().removeIf(suspect -> triple.getMiddle().equals(suspect.getClass()) && triple.getRight() == suspect.id);
//            }
//        }
//        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiCrafting) {
//            event.getButtonList().removeIf(suspect -> suspect instanceof GuiButtonImage && suspect.id == 10);
//        }
    }

    @SubscribeEvent
    public void guiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiContainer) {
            if (lastGui != event.getGui()) {
                if (event.getGui() == null) {
                    instanceTooltipHandlers = null;
                } else {
                    instanceTooltipHandlers = new LinkedList<>();
                    if (event.getGui() instanceof IContainerTooltipHandler) {
                        instanceTooltipHandlers.add(((IContainerTooltipHandler) event.getGui()));
                    }
                    instanceTooltipHandlers.addAll(tooltipHandlers);
                }
                lastGui = event.getGui();
            }
        }
    }

    @SubscribeEvent
    public void clientTickEvent(TickEvent.ClientTickEvent event) {

        if (event.phase == Phase.START) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.currentScreen != null && minecraft.currentScreen instanceof GuiContainer) {
                objectHandlers.forEach(handler -> handler.guiTick(((GuiContainer) minecraft.currentScreen)));
            }
        }
    }

    @SubscribeEvent
    public void drawScreenPost(DrawScreenEvent.Post event) {
        GuiScreen screen = event.getGui();

        Point mousePos = GuiDraw.getMousePosition();
        List<String> tooltip = new LinkedList<>();
        ItemStack stack = ItemStack.EMPTY;
        if (instanceTooltipHandlers != null) {
            instanceTooltipHandlers.forEach(handler -> handler.handleTooltip(screen, mousePos.x, mousePos.y, tooltip));
        }

        if (screen instanceof GuiContainer) {
            if (tooltip.isEmpty() && GuiHelper.shouldShowTooltip(screen)) {
                GuiContainer container = ((GuiContainer) screen);
                stack = GuiHelper.getStackMouseOver(container, false);

                if (!stack.isEmpty()) {
                    tooltip.clear();
                    tooltip.addAll(GuiHelper.itemDisplayNameMultiline(stack, container, false));
                }
            }
        }

        GuiDraw.drawMultiLineTip(stack, mousePos.x + 10, mousePos.y - 12, tooltip);
    }

    @SubscribeEvent
    public void foregroundRenderEvent(GuiContainerEvent.DrawForeground event) {
        GuiContainer container = event.getGuiContainer();
        GlStateTracker.pushState();
        Point mousePos = GuiDraw.getMousePosition();

        GlStateManager.translate(-container.getGuiLeft(), -container.getGuiTop(), 100F);
        drawHandlers.forEach(handler -> handler.renderObjects(container, mousePos.x, mousePos.y));

        drawHandlers.forEach(handler -> handler.postRenderObjects(container, mousePos.x, mousePos.y));

        GlStateManager.translate(container.getGuiLeft(), container.getGuiTop(), -100F);
        GuiHelper.enable3DRender();

        GlStateManager.pushMatrix();
        for (Slot slot : container.inventorySlots.inventorySlots) {
            GlStateTracker.pushState();
            drawHandlers.forEach(handler -> handler.renderSlotOverlay(container, slot));
            GlStateTracker.popState();
        }
        GlStateManager.popMatrix();

        GlStateTracker.popState();
    }

    @SubscribeEvent
    public void tooltipPreEvent(RenderTooltipEvent.Pre event) {
        //for (IContainerObjectHandler handler : objectHandlers) {
        //    if (!handler.shouldShowTooltip(Minecraft.getMinecraft().currentScreen)) {
        //        event.setCanceled(true);
        //        return;
        //    }
        //}
        event.setY(MathHelper.clip(event.getY(), 8, event.getScreenHeight() - 8));
    }

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event) {

        if (instanceTooltipHandlers != null && Minecraft.getMinecraft().currentScreen != null) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;

            Point mousePos = GuiDraw.getMousePosition();

            instanceTooltipHandlers.forEach(handler -> handler.handleTooltip(screen, mousePos.x, mousePos.y, event.getToolTip()));

            if (screen instanceof GuiContainer) {
                GuiContainer container = ((GuiContainer) screen);
                instanceTooltipHandlers.forEach(handler -> handler.handleItemDisplayName(screen, GuiHelper.getStackMouseOver(container, true), event.getToolTip()));
            }
        }
    }

}

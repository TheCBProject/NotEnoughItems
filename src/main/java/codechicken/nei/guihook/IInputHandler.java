package codechicken.nei.guihook;

import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public interface IInputHandler {

    /**
     * This version of keyTyped is passive and will be called on every input handler before keyTyped is processed
     *
     * @param gui     An instance of the current screen
     * @param keyChar The character representing the keyPress
     * @param keyID   The KeyCode as defined in {@link Keyboard}
     */
    default void onKeyTyped(GuiScreen gui, char keyChar, int keyID) {
    }

    /**
     * Only use this for things like input boxes that have to be 'focused' first and will not conflict with others
     *
     * @param gui     An instance of the current screen
     * @param keyChar The character representing the keyPress
     * @param keyCode The KeyCode as defined in {@link Keyboard}
     * @return true to terminate further processing of this event.
     */
    default boolean keyTyped(GuiScreen gui, char keyChar, int keyCode) {
        return false;
    }

    /**
     * This version of keyTyped is called if the key event has not been handled by the first pass, use this for key bindings that work globally
     *
     * @param gui     An instance of the current screen
     * @param keyChar The character representing the keyPress
     * @param keyID   The KeyCode as defined in {@link Keyboard}
     * @return true to terminate further processing of this event.
     */
    default boolean lastKeyTyped(GuiScreen gui, char keyChar, int keyID) {
        return false;
    }

    /**
     * Called when the mouse is clicked in the gui
     *
     * @param gui    An instance of the current screen
     * @param mouseX The x position of the mouse in pixels from left
     * @param mouseY The y position of the mouse in pixels from top
     * @param button The button index being pressed, {0 = Left Click, 1 = Right Click, 2 = Middle Click}
     * @return true to terminate further processing of this event.
     */
    default boolean mouseClicked(GuiScreen gui, int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * This version of mouseClicked is passive and will be called on every input handler before mouseClicked is processed
     *
     * @param gui    An instance of the current screen
     * @param mouseX The x position of the mouse in pixels from left
     * @param mouseY The y position of the mouse in pixels from top
     * @param button The button index being pressed, {0 = Left Click, 1 = Right Click, 2 = Middle Click}
     */
    default void onMouseClicked(GuiScreen gui, int mouseX, int mouseY, int button) {
    }

    /**
     * @param gui    An instance of the current screen
     * @param mouseX The x position of the mouse in pixels from left
     * @param mouseY The y position of the mouse in pixels from top
     * @param button The button index being released, {0 = Left Click, 1 = Right Click, 2 = Middle Click}
     */
    default void onMouseUp(GuiScreen gui, int mouseX, int mouseY, int button) {
    }

    /**
     * Called on the Post event of MouseInputEvent
     *
     * @param gui    An instance of the current screen
     * @param mouseX The x position of the mouse in pixels from left
     * @param mouseY The y position of the mouse in pixels from top
     * @param button The button index being released, {0 = Left Click, 1 = Right Click, 2 = Middle Click}
     */
    default void onMouseClickedPost(GuiScreen gui, int mouseX, int mouseY, int button) {
    }

    /**
     * @param gui      An instance of the current screen
     * @param mouseX   The x position of the mouse in pixels from left
     * @param mouseY   The y position of the mouse in pixels from top
     * @param scrolled The number of notches scrolled. Positive for up.
     * @return true to terminate further processing of this event.
     */
    default boolean mouseScrolled(GuiScreen gui, int mouseX, int mouseY, int scrolled) {
        return false;
    }

    /**
     * This version of mouseClicked is passive and will be called on every input handler before mouseClicked is processed
     *
     * @param gui      An instance of the current screen
     * @param mouseX   The x position of the mouse in pixels from left
     * @param mouseY   The y position of the mouse in pixels from top
     * @param button   The button index being pressed, {0 = Left Click, 1 = Right Click, 2 = Middle Click}
     * @param heldTime The number of milliseconds since the button was first pressed
     */
    default void onMouseDragged(GuiScreen gui, int mouseX, int mouseY, int button, long heldTime) {
    }

}

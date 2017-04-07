package codechicken.nei.layout;

import codechicken.nei.VisibilityData;
import codechicken.nei.widget.Button;
import net.minecraft.client.gui.inventory.GuiContainer;

public abstract class LayoutStyle {

    public abstract void init();

    public abstract void reset();

    public abstract void layout(GuiContainer gui, VisibilityData visibility);

    public abstract String getName();

    public void drawBackground(GuiContainer gui) {
    }

    public abstract void drawButton(Button button, int mouseX, int mouseY);

    public abstract void drawSubsetTag(String text, int x, int y, int w, int h, int state, boolean mouseover);
}

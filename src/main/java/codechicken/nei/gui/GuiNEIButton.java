package codechicken.nei.gui;

import codechicken.lib.texture.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiNEIButton extends GuiButton {

    protected static final ResourceLocation GUI_TEX = new ResourceLocation("textures/gui/widgets.png");

    public GuiNEIButton(int buttonId, int x, int y, int width, int height, String text) {

        super(buttonId, x, y, width, height, text);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {

        if (!visible) {
            return;
        }

        FontRenderer fontrenderer = minecraft.fontRenderer;
        TextureUtils.changeTexture(GUI_TEX);
        GlStateManager.color(1, 1, 1, 1);

        boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        int k = getHoverState(flag);

        drawTexturedModalRect(x, y, 0, 46 + k * 20, width / 2, height / 2);//top left
        drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + k * 20, width / 2, height / 2);//top right
        drawTexturedModalRect(x, y + height / 2, 0, 46 + k * 20 + 20 - height / 2, width / 2, height / 2);//bottom left
        drawTexturedModalRect(x + width / 2, y + height / 2, 200 - width / 2, 46 + k * 20 + 20 - height / 2, width / 2, height / 2);//bottom right

        mouseDragged(minecraft, mouseX, mouseY);

        if (!enabled) {
            drawCenteredString(fontrenderer, displayString, x + width / 2, y + (height - 8) / 2, 0xffa0a0a0);
        } else if (flag) {
            drawCenteredString(fontrenderer, displayString, x + width / 2, y + (height - 8) / 2, 0xffffa0);
        } else {
            drawCenteredString(fontrenderer, displayString, x + width / 2, y + (height - 8) / 2, 0xe0e0e0);
        }
    }
}

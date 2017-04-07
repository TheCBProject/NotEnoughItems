package codechicken.nei.layout;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.LayoutManager;
import codechicken.nei.widget.Button;
import net.minecraft.client.gui.inventory.GuiContainer;

import static codechicken.lib.gui.GuiDraw.*;
import static codechicken.nei.LayoutManager.*;

public class LayoutStyleTMIOld extends LayoutStyleDefault {

    public static final Rectangle4i stateOff = new Rectangle4i(48, 0, 8, 12);
    public static final Rectangle4i stateOn = new Rectangle4i(56, 0, 8, 12);
    public static final Rectangle4i stateDisabled = new Rectangle4i(64, 0, 8, 12);

    int stateButtonCount;
    int clickButtonCount;

    @Override
    public String getName() {
        return "oldtmi";
    }

    @Override
    public void init() {
        delete.icon = new Rectangle4i(24, 12, 12, 12);
        gamemode.icons[0] = new Rectangle4i(12, 12, 12, 12);
        gamemode.icons[1] = new Rectangle4i(36, 12, 12, 12);
        gamemode.icons[2] = new Rectangle4i(48, 12, 12, 12);
        rain.icon = new Rectangle4i(0, 12, 12, 12);
        magnet.icon = new Rectangle4i(60, 24, 12, 12);
        timeButtons[0].icon = new Rectangle4i(12, 24, 12, 12);
        timeButtons[1].icon = new Rectangle4i(0, 24, 12, 12);
        timeButtons[2].icon = new Rectangle4i(24, 24, 12, 12);
        timeButtons[3].icon = new Rectangle4i(36, 24, 12, 12);
        heal.icon = new Rectangle4i(48, 24, 12, 12);
        dropDown.x = 93;
    }

    @Override
    public void reset() {
        stateButtonCount = clickButtonCount = 0;
    }

    @Override
    public void layoutButton(Button button) {
        int offsetx = 2;
        int offsety = 2;

        if ((button.state & 0x4) != 0) {
            button.x = offsetx + stateButtonCount * 22;
            button.y = offsety;
            stateButtonCount++;
        } else {
            button.x = offsetx + (clickButtonCount % 4) * 22;
            button.y = offsety + (1 + clickButtonCount / 4) * 17;
            clickButtonCount++;
        }

        button.h = 14;
        button.w = button.contentWidth() + 2;
        if ((button.state & 0x4) != 0) {
            button.w += stateOff.w;
        }
    }

    @Override
    public void drawBackground(GuiContainer gui) {
        if (clickButtonCount == 0 && stateButtonCount == 0) {
            return;
        }

        int maxx = Math.max(stateButtonCount, clickButtonCount);
        if (maxx > 4) {
            maxx = 4;
        }
        int maxy = clickButtonCount == 0 ? 1 : (clickButtonCount / 4 + 2);

        drawRect(0, 0, 2 + 22 * maxx, 1 + maxy * 17, 0xFF000000);
    }

    @Override
    public void drawButton(Button b, int mousex, int mousey) {
        int cwidth = b.contentWidth();
        if ((b.state & 0x4) != 0) {
            cwidth += stateOff.w;
        }
        int textx = b.x + (b.w - cwidth) / 2;
        int texty = b.y + (b.h - 8) / 2;

        drawRect(b.x, b.y, b.w, b.h, b.contains(mousex, mousey) ? 0xee401008 : 0xee000000);

        Rectangle4i icon = b.getRenderIcon();
        if (icon == null) {
            drawString(b.getRenderLabel(), textx, texty, -1);
        } else {
            int icony = b.y + (b.h - icon.h) / 2;
            LayoutManager.drawIcon(textx, icony, icon);
            if ((b.state & 0x3) == 2) {
                drawRect(textx, icony, icon.w, icon.h, 0x80000000);
            }

            if ((b.state & 0x4) != 0) {
                Rectangle4i stateimage;
                if ((b.state & 0x3) == 1) {
                    stateimage = stateOn;
                } else if ((b.state & 0x3) == 2) {
                    stateimage = stateDisabled;
                } else {
                    stateimage = stateOff;
                }
                LayoutManager.drawIcon(textx + icon.w, icony, stateimage);
            }
        }
    }

    @Override
    public void drawSubsetTag(String text, int x, int y, int w, int h, int state, boolean mouseover) {
        drawRect(x, y, w, h, mouseover ? 0xFF401008 : 0xFF000000);
        if (text != null) {
            int colour = -1;
            if (state == 0) {
                colour = 0xFF601010;
            } else if (state == 1) {
                colour = 0xFF807070;
            }
            drawStringC(text, x, y, w, h, colour, state == 0);
        }
    }
}

package codechicken.nei.widget;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.LayoutManager;
import codechicken.nei.util.NEIClientUtils;

import java.util.List;

import static codechicken.lib.gui.GuiDraw.getStringWidth;

public abstract class Button extends Widget {

    public Button(String s) {
        label = s;
    }

    public Button() {
        label = "";
    }

    public int contentWidth() {
        return getRenderIcon() == null ? getStringWidth(label) : getRenderIcon().w;
    }

    @Override
    public void draw(int mousex, int mousey) {
        LayoutManager.getLayoutStyle().drawButton(this, mousex, mousey);
    }

    @Override
    public boolean handleClick(int mx, int my, int button) {
        if (button == 1 || button == 0) {
            if (onButtonPress(button == 1)) {
                NEIClientUtils.playClickSound();
            }
        }
        return true;
    }

    public abstract boolean onButtonPress(boolean rightclick);

    public Rectangle4i getRenderIcon() {
        return icon;
    }

    @Override
    public void handleTooltip(int mx, int my, List<String> tooltip) {
        if (!contains(mx, my)) {
            return;
        }

        String tip = getButtonTip();
        if (tip != null) {
            tooltip.add(tip);
        }
    }

    public String getButtonTip() {
        return null;
    }

    public String getRenderLabel() {
        return label;
    }

    public String label;
    public Rectangle4i icon;

    /**
     * 0x4 = state flag, as opposed to 1 click
     * 0 = normal
     * 1 = on
     * 2 = disabled
     */
    public int state;
}

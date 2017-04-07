package codechicken.nei.widget;

import codechicken.lib.vec.Rectangle4i;

public abstract class ButtonCycled extends Button {

    @Override
    public Rectangle4i getRenderIcon() {
        return icons[index];
    }

    public int index;
    public Rectangle4i[] icons;
}

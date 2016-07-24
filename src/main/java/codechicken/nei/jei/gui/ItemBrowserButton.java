package codechicken.nei.jei.gui;

import codechicken.nei.config.OptionCycled;
import codechicken.nei.jei.EnumItemBrowser;

/**
 * Created by covers1624 on 7/15/2016.
 */
public abstract class ItemBrowserButton extends OptionCycled {

    public ItemBrowserButton(String name) {
        super(name, EnumItemBrowser.values().length);
    }

    public boolean cycle() {
        int next = value();
        do
            next = (next + 1) % count; while (!optionValid(next));

        if (next == value()) {
            return false;
        }

        getTag().setIntValue(next);
        setValue(EnumItemBrowser.values()[next]);
        return true;
    }

    protected abstract void setValue(EnumItemBrowser itemBrowser);

    @Override
    public boolean optionValid(int index) {
        return EnumItemBrowser.values()[index].isValid();
    }
}
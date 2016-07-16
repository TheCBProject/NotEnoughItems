package codechicken.nei.jei;

import codechicken.nei.config.OptionCycled;

/**
 * Created by covers1624 on 7/15/2016.
 */
public class ItemPanelButton extends OptionCycled {

    public ItemPanelButton(String name) {
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
        JEIIntegrationManager.panelOwner = EnumItemBrowser.values()[next];
        return true;
    }

    @Override
    public boolean optionValid(int index) {
        return EnumItemBrowser.values()[index].isValid();
    }
}

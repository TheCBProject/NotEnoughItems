package codechicken.nei.config;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.LayoutManager;

public class OptionGamemodes extends OptionStringSet {

    public OptionGamemodes(String name) {
        super(name);

        options.add("creative");
        options.add("creative+");
        options.add("adventure");
    }

    @Override
    public void drawIcons() {
        int x = buttonX();
        LayoutManager.drawIcon(x + 4, 4, new Rectangle4i(132, 12, 12, 12));
        x += 24;
        LayoutManager.drawIcon(x + 4, 4, new Rectangle4i(156, 12, 12, 12));
        x += 24;
        LayoutManager.drawIcon(x + 4, 4, new Rectangle4i(168, 12, 12, 12));
        x += 24;
    }
}

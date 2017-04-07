package codechicken.nei.jei;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by covers1624 on 7/14/2016.
 */
public enum EnumItemBrowser {
    NEI {
        @Override
        public boolean isValid() {
            return true;
        }
    },
    JEI {
        @Override
        public boolean isValid() {
            return Loader.isModLoaded("jei");
        }
    };

    public abstract boolean isValid();

    public static LinkedList<EnumItemBrowser> getValidBrowsers() {
        LinkedList<EnumItemBrowser> validBrowsers = new LinkedList<>();
        for (EnumItemBrowser browser : values()) {
            if (browser.isValid()) {
                validBrowsers.add(browser);
            }
        }
        return validBrowsers;
    }

    public static ArrayList<EnumItemBrowser> toArray() {
        return Lists.newArrayList(values());
    }

    public static EnumItemBrowser getNext(EnumItemBrowser current) {
        LinkedList<EnumItemBrowser> validBrowsers = getValidBrowsers();
        EnumItemBrowser nextBrowser = validBrowsers.getFirst();
        int index = validBrowsers.indexOf(current);
        if (index != -1) {
            try {
                nextBrowser = validBrowsers.get(index + 1);
            } catch (IndexOutOfBoundsException e) {
                nextBrowser = validBrowsers.getFirst();
            }
        }

        return nextBrowser;
    }

}

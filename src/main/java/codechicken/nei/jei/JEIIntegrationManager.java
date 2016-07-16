package codechicken.nei.jei;

import codechicken.lib.config.ConfigTagParent;
import codechicken.nei.VisibilityData;
import codechicken.nei.jei.proxy.IJEIProxy;
import net.minecraftforge.fml.common.Loader;

/**
 * Created by covers1624 on 7/14/2016.
 * Handles hiding and showing things from jei and nei. Basically an interface between the two.
 */
public class JEIIntegrationManager {

    public static final IJEIProxy proxy = createProxy();

    public static EnumItemBrowser panelOwner = EnumItemBrowser.JEI;
    public static EnumItemBrowser recipeOwner = EnumItemBrowser.NEI;

    public static void pushChanges(VisibilityData data) {

        if (panelOwner == EnumItemBrowser.JEI && isJEIInstalled()) {
            proxy.setJEIItemPanelState(true);
            data.showItemPanel = false;
        } else {
            proxy.setJEIItemPanelState(false);
            data.showItemPanel = true;
        }
    }

    public static boolean isJEIInstalled() {
        return Loader.isModLoaded("JEI");
    }

    private static IJEIProxy createProxy() {
        IJEIProxy proxy;
        try {
            Class<?> proxyClass;
            if (isJEIInstalled()) {
                proxyClass = Class.forName("codechicken.nei.jei.proxy.JEIProxy", true, Loader.instance().getModClassLoader());
            } else {
                proxyClass = Class.forName("codechicken.nei.jei.proxy.JEIDummyProxy", true, Loader.instance().getModClassLoader());
            }
            proxy = (IJEIProxy) proxyClass.newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Unable to create NEI's JEI proxy interface!", e);
        }
        return proxy;
    }

    public static void initConfig(ConfigTagParent tag) {
        int panelOwnerOrdinal = tag.getTag("jei.panelOwner").getIntValue(0);
        try {
            panelOwner = EnumItemBrowser.values()[panelOwnerOrdinal];
        } catch (IndexOutOfBoundsException e) {
            panelOwner = EnumItemBrowser.NEI;
        }
    }
}

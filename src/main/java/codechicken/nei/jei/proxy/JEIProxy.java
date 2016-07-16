package codechicken.nei.jei.proxy;

import codechicken.nei.util.LogHelper;
import mezz.jei.config.Config;

/**
 * Created by covers1624 on 7/14/2016.
 */
public class JEIProxy implements IJEIProxy {
    @Override
    public void setJEIItemPanelState(boolean enable) {
        if (enable) {
            if (!Config.isOverlayEnabled()) {
                LogHelper.info("Setting JEI to: " + enable);
                Config.toggleOverlayEnabled();
            }
        } else {
            if (Config.isOverlayEnabled()) {
                LogHelper.info("Setting JEI to: " + enable);
                Config.toggleOverlayEnabled();
            }
        }
    }
}

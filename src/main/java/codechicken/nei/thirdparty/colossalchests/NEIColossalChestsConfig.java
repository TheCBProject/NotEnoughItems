package codechicken.nei.thirdparty.colossalchests;

import codechicken.lib.reflect.ReflectionManager;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.api.NEIPlugin;
import codechicken.nei.util.LogHelper;
import net.minecraft.client.gui.inventory.GuiContainer;

/**
 * Created by covers1624 on 9/02/2017.
 */
@NEIPlugin
public class NEIColossalChestsConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        try {
            Class<?> clazz = null;
            try {
                clazz = ReflectionManager.findClass("org.cyclops.colossalchests.client.gui.container.GuiColossalChest");
            } catch (Exception ignored) {
            }
            if (clazz != null) {
                API.addFastTransferExemptContainer((Class<? extends GuiContainer>) clazz);
            }
        } catch (Exception e) {
            LogHelper.fatalError("Something went wring trying to enable ColossalChests ingegration.", e);
        }
    }

    @Override
    public String getName() {
        return "Colossal Chests Gui blacklisting";
    }

    @Override
    public String getVersion() {
        return "1";
    }
}

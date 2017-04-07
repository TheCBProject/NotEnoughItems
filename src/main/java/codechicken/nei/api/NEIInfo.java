package codechicken.nei.api;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.OptionCycled;
import net.minecraft.world.World;

public class NEIInfo {

    public static void load(World world) {
        OptionCycled modeOption = (OptionCycled) NEIClientConfig.getOptionList().getOption("inventory.cheatmode");
        modeOption.parent.synthesizeEnvironment(false);
        if (!modeOption.optionValid(modeOption.value())) {
            modeOption.copyGlobals();
            modeOption.cycle();
        }
    }
}

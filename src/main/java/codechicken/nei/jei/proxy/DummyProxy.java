package codechicken.nei.jei.proxy;

import net.minecraft.item.ItemStack;

/**
 * Created by covers1624 on 7/04/2017.
 */
public class DummyProxy implements IJEIProxy {

    @Override
    public boolean isJEIEnabled() {
        return false;
    }
}

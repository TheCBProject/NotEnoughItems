package codechicken.nei;

import codechicken.nei.api.INEIGuiHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;

public class NEICreativeGuiHandler implements INEIGuiHandler {

    @Override
    public VisibilityData modifyVisibility(GuiContainer gui, VisibilityData currentVisibility) {
        if (!(gui instanceof GuiContainerCreative)) {
            return currentVisibility;
        }

        if (((GuiContainerCreative) gui).getSelectedTabIndex() != CreativeTabs.INVENTORY.getTabIndex()) {
            currentVisibility.showItemPanel = currentVisibility.enableDeleteMode = false;
        }

        return currentVisibility;
    }

}

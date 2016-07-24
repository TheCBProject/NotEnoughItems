package codechicken.nei.jei.gui;

import codechicken.nei.jei.EnumItemBrowser;
import codechicken.nei.jei.JEIIntegrationManager;

/**
 * Created by covers1624 on 7/24/2016.
 * A gui button to define what mod handles the search box.
 */
public class SearchBoxButton extends ItemBrowserButton {

    public SearchBoxButton(String name) {
        super(name);
    }

    @Override
    protected void setValue(EnumItemBrowser itemBrowser) {
        JEIIntegrationManager.setSearchBoxOwner(itemBrowser);
    }
}

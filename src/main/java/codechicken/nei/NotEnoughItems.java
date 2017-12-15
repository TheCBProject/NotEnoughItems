package codechicken.nei;

import codechicken.lib.CodeChickenLib;
import codechicken.lib.internal.ModDescriptionEnhancer;
import codechicken.nei.init.NEIInitialization;
import codechicken.nei.proxy.Proxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static codechicken.lib.CodeChickenLib.MC_VERSION;
import static codechicken.lib.CodeChickenLib.MC_VERSION_DEP;
import static codechicken.nei.NotEnoughItems.*;

/**
 * Created by covers1624 on 29/03/2017.
 */
@Mod (modid = MOD_ID, name = MOD_NAME, version = MOD_VERSION, dependencies =  DEPENDENCIES, acceptedMinecraftVersions = MC_VERSION_DEP, updateJSON = UPDATE_URL, certificateFingerprint = "f1850c39b2516232a2108a7bd84d1cb5df93b261")
public class NotEnoughItems {

    public static final String MOD_ID = "nei";
    public static final String MOD_NAME = "Not Enough Items";
    public static final String MOD_VERSION = "${mod_version}";
    public static final String MOD_VERSION_DEP = "required-after:nei@[" + MOD_VERSION + ",);";
    public static final String DEPENDENCIES = CodeChickenLib.MOD_VERSION_DEP + ";required-after:jei@[4.8.0.,);required-after:forge@[14.21.1.2443,)";
    static final String UPDATE_URL = "http://chickenbones.net/Files/notification/version.php?query=forge&version=" + MC_VERSION + "&file=NotEnoughItems";

    @SidedProxy (clientSide = "codechicken.nei.proxy.ProxyClient", serverSide = "codechicken.nei.proxy.Proxy")
    public static Proxy proxy;

    @Instance
	public static NotEnoughItems instance;

    @Metadata
    public static ModMetadata metadata;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        NEIInitialization.scrapeData(event.getAsmData());
        ModDescriptionEnhancer.registerEnhancement(MOD_ID, "NotEnoughItems");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }
}

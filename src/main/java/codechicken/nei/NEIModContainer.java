package codechicken.nei;

import codechicken.lib.CodeChickenLib;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.asm.NEICorePlugin;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class NEIModContainer extends DummyModContainer {

    public static LinkedList<IConfigureNEI> plugins = new LinkedList<>();
    private static URL UPDATE_URL;

    public NEIModContainer() {
        super(MetadataCollection.from(MetadataCollection.class.getResourceAsStream("/neimod.info"), "NotEnoughItems").getMetadataForId("nei", null));
        loadMetadata();
        try {
            UPDATE_URL = new URL("http://chickenbones.net/Files/notification/version.php?query=forge&version=" + CodeChickenLib.MC_VERSION_DEP + "&file=NotEnoughItems");
        } catch (MalformedURLException ignored) {
        }
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        Set<ArtifactVersion> deps = new HashSet<>();
        if (!super.getMetadata().version.contains("$")) {
            deps.add(VersionParser.parseVersionReference("codechickenlib@[" + CodeChickenLib.MOD_VERSION + ",)"));
            deps.add(VersionParser.parseVersionReference("JEI@[3.13.2,)"));
        }
        return deps;
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return new LinkedList<>(getRequirements());
    }

    private String description;

    private void loadMetadata() {
        description = super.getMetadata().description.replace("Supporters:", TextFormatting.AQUA + "Supporters:");
    }

    @Override
    public ModMetadata getMetadata() {
        String s_plugins = "";
        if (plugins.size() == 0) {
            s_plugins += TextFormatting.RED + "No installed plugins.";
        } else {
            s_plugins += TextFormatting.GREEN + "Installed plugins: ";
            for (int i = 0; i < plugins.size(); i++) {
                if (i > 0) {
                    s_plugins += ", ";
                }
                IConfigureNEI plugin = plugins.get(i);
                s_plugins += plugin.getName() + " " + plugin.getVersion();
            }
            s_plugins += ".";
        }

        ModMetadata meta = super.getMetadata();
        meta.description = description.replace("<plugins>", s_plugins);
        return meta;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent event) {
        FingerprintChecker.runFingerprintChecks();
        if (event.getSide().isClient()) {
            ClientHandler.preInit();
        }
    }

    @Subscribe
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            ClientHandler.init();
        }

        ServerHandler.init();
    }

    @Override
    public VersionRange acceptableMinecraftVersionRange() {
        return VersionParser.parseRange(CodeChickenLib.MC_VERSION_DEP);
    }

    @Override
    public File getSource() {
        return NEICorePlugin.location;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        return getSource().isFile() ? FMLFileResourcePack.class : FMLFolderResourcePack.class;
    }

    @Override
    public Object getMod() {
        return this;
    }

    @Override
    public URL getUpdateUrl() {
        return UPDATE_URL;
    }
}

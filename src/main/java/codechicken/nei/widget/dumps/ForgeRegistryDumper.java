package codechicken.nei.widget.dumps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.LinkedList;

/**
 * Created by covers1624 on 8/01/2017.
 */
public abstract class ForgeRegistryDumper<V extends IForgeRegistryEntry<V>> extends DataDumper {

    public ForgeRegistryDumper(String name) {
        super(name);
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        LinkedList<String[]> list = new LinkedList<>();
        IForgeRegistry<V> registry = registry();

        for (V obj : registry) {
            if (obj.getRegistryName() == null) {
                continue;
            }
            list.add(dump(obj, obj.getRegistryName()));
        }

        return list;
    }

    public abstract IForgeRegistry<V> registry();

    public abstract String[] dump(V obj, ResourceLocation registryName);

    @Override
    public int modeCount() {
        return 1;
    }
}

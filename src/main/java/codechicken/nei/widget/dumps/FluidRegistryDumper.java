package codechicken.nei.widget.dumps;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by covers1624 on 9/01/2017.
 */
public class FluidRegistryDumper extends DataDumper {

    public FluidRegistryDumper() {
        super("tools.dump.fluid");
    }

    @Override
    public String[] header() {
        //@formatter:off
        return new String[] {
                "Name",
                "ID",
                "Unlocalized Name",
                "Luminosity",
                "Density",
                "Temperature",
                "Viscosity",
                "Is Gas",
                "Rarity",
                "Block",
                "Still Texture",
                "Flowing Texture",
                "Fill Sound",
                "Empty Sound",
                "Class"
        };
        //@formatter:on
    }

    @Override
    public Iterable<String[]> dump(int mode) {
        Map<String, Fluid> registeredFluids = FluidRegistry.getRegisteredFluids();
        Map<Fluid, Integer> fluidIDMap = FluidRegistry.getRegisteredFluidIDs();
        List<String[]> dumps = new LinkedList<>();
        for (Entry<String, Fluid> fluidEntry : registeredFluids.entrySet()) {
            Fluid fluid = fluidEntry.getValue();
            int id = fluidIDMap.get(fluid);
            //@formatter:off
            dumps.add(new String[] {
                    fluidEntry.getKey(),
                    Integer.toString(id),
                    fluid.getUnlocalizedName(),
                    String.valueOf(fluid.getLuminosity()),
                    String.valueOf(fluid.getDensity()),
                    String.valueOf(fluid.getTemperature()),
                    String.valueOf(fluid.isGaseous()),
                    fluid.getRarity().toString(),
                    fluid.canBePlacedInWorld() ? fluid.getBlock().getRegistryName().toString() : "No Block",
                    fluid.getStill().toString(),
                    fluid.getFlowing().toString(),
                    fluid.getFillSound().getRegistryName().toString(),
                    fluid.getEmptySound().getRegistryName().toString(),
                    fluid.getClass().getCanonicalName()
            });
            //@formatter:on
        }
        return dumps;
    }

    @Override
    public int modeCount() {
        return 1;
    }
}

package codechicken.nei.asm;

import codechicken.lib.asm.ASMBlock;
import codechicken.lib.asm.ASMReader;
import codechicken.lib.asm.ModularASMTransformer;
import codechicken.lib.asm.ModularASMTransformer.MethodInjector;
import codechicken.lib.asm.ObfMapping;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Map;

public class NEITransformer implements IClassTransformer {

    private ModularASMTransformer transformer = new ModularASMTransformer();
    private Map<String, ASMBlock> blocks = ASMReader.loadResource("/assets/nei/asm/blocks.asm");

    public NEITransformer() {
        ObfMapping mapping;

        mapping = new ObfMapping("net/minecraft/client/gui/inventory/GuiContainerCreative", "func_147053_i", "()V");
        transformer.add(new MethodInjector(mapping, blocks.get("i_creativeTabSearch"), true));

        mapping = new ObfMapping("net/minecraft/client/gui/inventory/GuiContainer", "func_73863_a", "(IIF)V");
        transformer.add(new MethodInjector(mapping, blocks.get("n_foregroundHook"), blocks.get("i_foregroundHook"), false));
    }

    @Override
    public byte[] transform(String name, String tname, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            bytes = transformer.transform(name, bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bytes;
    }
}

# Optimizes Creative search.
list i_creativeTabSearch
ALOAD 0
INVOKESTATIC codechicken/nei/featurehack/VanillaCreativeTabSearchHooks.updateSearchListThreaded (Lnet/minecraft/client/gui/inventory/GuiContainerCreative;)V
RETURN

#Only hook used for NEI now, Just a render Hook.
list n_foregroundHook
INVOKESTATIC net/minecraft/client/renderer/RenderHelper.func_74518_a ()V
ALOAD 0
ILOAD 1
ILOAD 2
INVOKEVIRTUAL net/minecraft/client/gui/inventory/GuiContainer.func_146979_b (II)V
INVOKESTATIC net/minecraft/client/renderer/RenderHelper.func_74520_c ()V

list i_foregroundHook
ALOAD 0
INVOKESTATIC codechicken/nei/asm/ASMHooks.handleForegroundRender (Lnet/minecraft/client/gui/inventory/GuiContainer;)V

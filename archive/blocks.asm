list spawnerPlaced
ALOAD 1
ALOAD 2
ALOAD 5
INVOKESTATIC codechicken/nei/ItemMobSpawner.onBlockPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/item/ItemStack;)V
RETURN

#begin GuiContainer patches

list m_getManager
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ARETURN

list m_setWorldAndResolution
ALOAD 1
GETFIELD net/minecraft/client/Minecraft.field_71462_r : Lnet/minecraft/client/gui/GuiScreen;
ALOAD 0
IF_ACMPNE LSKIP
ALOAD 0
NEW codechicken/nei/guihook/GuiContainerManager
DUP
ALOAD 0
INVOKESPECIAL codechicken/nei/guihook/GuiContainerManager.<init> (Lnet/minecraft/client/gui/inventory/GuiContainer;)V
PUTFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
LSKIP
ALOAD 0
ALOAD 1
ILOAD 2
ILOAD 3
INVOKESPECIAL net/minecraft/client/gui/GuiScreen.func_146280_a (Lnet/minecraft/client/Minecraft;II)V
ALOAD 1
GETFIELD net/minecraft/client/Minecraft.field_71462_r : Lnet/minecraft/client/gui/GuiScreen;
ALOAD 0
IF_ACMPNE LEND
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.load ()V
LEND
RETURN

list preDraw
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.preDraw ()V

list n_objectUnderMouse
INVOKEVIRTUAL net/minecraft/inventory/Slot.func_111238_b ()Z
IFEQ LSKIP

list objectUnderMouse
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.objectUnderMouse (II)Z
IFNE LSKIP

list n_renderObjects
INVOKEVIRTUAL net/minecraft/client/gui/inventory/GuiContainer.func_146979_b (II)V
INVOKESTATIC net/minecraft/client/renderer/RenderHelper.func_74520_c ()V

list renderObjects
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.renderObjects (II)V

list d_renderToolTip
ALOAD 8
INVOKEVIRTUAL net/minecraft/entity/player/InventoryPlayer.func_70445_o ()Lnet/minecraft/item/ItemStack;
IFNONNULL LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147006_u : Lnet/minecraft/inventory/Slot;
IFNULL LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147006_u : Lnet/minecraft/inventory/Slot;
INVOKEVIRTUAL net/minecraft/inventory/Slot.func_75216_d ()Z
IFEQ LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147006_u : Lnet/minecraft/inventory/Slot;
INVOKEVIRTUAL net/minecraft/inventory/Slot.func_75211_c ()Lnet/minecraft/item/ItemStack;
ASTORE 10
ALOAD 0
ALOAD 10
ILOAD 1
ILOAD 2
INVOKEVIRTUAL net/minecraft/client/gui/inventory/GuiContainer.func_146285_a (Lnet/minecraft/item/ItemStack;II)V
LSKIP

list d_renderToolTipIntellijEclipseCompilerFix
ALOAD 6
INVOKEVIRTUAL net/minecraft/entity/player/InventoryPlayer.func_70445_o ()Lnet/minecraft/item/ItemStack;
IFNONNULL LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147006_u : Lnet/minecraft/inventory/Slot;
IFNULL LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147006_u : Lnet/minecraft/inventory/Slot;
INVOKEVIRTUAL net/minecraft/inventory/Slot.func_75216_d ()Z
IFEQ LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147006_u : Lnet/minecraft/inventory/Slot;
INVOKEVIRTUAL net/minecraft/inventory/Slot.func_75211_c ()Lnet/minecraft/item/ItemStack;
ASTORE 8
ALOAD 0
ALOAD 8
ILOAD 1
ILOAD 2
INVOKEVIRTUAL net/minecraft/client/gui/inventory/GuiContainer.func_146285_a (Lnet/minecraft/item/ItemStack;II)V
LSKIP

list renderTooltips
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.renderToolTips (II)V

list d_drawSlot
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146296_j : Lnet/minecraft/client/renderer/RenderItem;
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146297_k : Lnet/minecraft/client/Minecraft;
GETFIELD net/minecraft/client/Minecraft.field_71439_g : Lnet/minecraft/client/entity/EntityPlayerSP;
ALOAD 4
ILOAD 2
ILOAD 3
INVOKEVIRTUAL net/minecraft/client/renderer/RenderItem.func_184391_a (Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146296_j : Lnet/minecraft/client/renderer/RenderItem;
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146289_q : Lnet/minecraft/client/gui/FontRenderer;
ALOAD 4
ILOAD 2
ILOAD 3
ALOAD 8
INVOKEVIRTUAL net/minecraft/client/renderer/RenderItem.func_180453_a (Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V

list drawSlot
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ALOAD 1
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.renderSlotUnderlay (Lnet/minecraft/inventory/Slot;)V
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ALOAD 1
ALOAD 4
ILOAD 2
ILOAD 3
ALOAD 8
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.drawSlotItem (Lnet/minecraft/inventory/Slot;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ALOAD 1
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.renderSlotOverlay (Lnet/minecraft/inventory/Slot;)V

list mouseClicked
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
ILOAD 3
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.mouseClicked (III)Z
IFEQ LCONT
RETURN
LCONT

list d_handleMouseClick
INVOKEVIRTUAL net/minecraft/client/gui/inventory/GuiContainer.func_184098_a (Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V

list handleMouseClick
INVOKEVIRTUAL net/minecraft/client/gui/inventory/GuiContainer.managerHandleMouseClick (Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V

list m_managerHandleMouseClick
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ALOAD 1
ILOAD 2
ILOAD 3
ALOAD 4
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.handleMouseClick (Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V
RETURN

list n_mouseDragged
ASTORE 7

list mouseDragged
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
ILOAD 3
LLOAD 4
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.mouseDragged (IIIJ)V

list overrideMouseUp
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
ILOAD 3
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.overrideMouseUp (III)Z
IFEQ LCONT
RETURN
LCONT

list n_mouseUpGoto
IFNULL LSTART
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146297_k : Lnet/minecraft/client/Minecraft;
GETFIELD net/minecraft/client/Minecraft.field_71474_y : Lnet/minecraft/client/settings/GameSettings;
GETFIELD net/minecraft/client/settings/GameSettings.field_74322_I : Lnet/minecraft/client/settings/KeyBinding;

list n_mouseUp
LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146297_k : Lnet/minecraft/client/Minecraft;
GETFIELD net/minecraft/client/Minecraft.field_71439_g : Lnet/minecraft/client/entity/EntityPlayerSP;
GETFIELD net/minecraft/client/entity/EntityPlayerSP.field_71071_by : Lnet/minecraft/entity/player/InventoryPlayer;
INVOKEVIRTUAL net/minecraft/entity/player/InventoryPlayer.func_70445_o ()Lnet/minecraft/item/ItemStack;
IFNONNULL L30
ALOAD 0
LCONST_0
PUTFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146997_J : J

list mouseUp
GOTO LSKIP
LSTART
ILOAD 3
IFLT LSKIP
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 1
ILOAD 2
ILOAD 3
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.mouseUp (III)V

list d_handleSlotClick
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146297_k : Lnet/minecraft/client/Minecraft;
GETFIELD net/minecraft/client/Minecraft.field_71442_b : Lnet/minecraft/client/multiplayer/PlayerControllerMP;
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147002_h : Lnet/minecraft/inventory/Container;
GETFIELD net/minecraft/inventory/Container.field_75152_c : I
ILOAD 2
ILOAD 3
ALOAD 4
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_146297_k : Lnet/minecraft/client/Minecraft;
GETFIELD net/minecraft/client/Minecraft.field_71439_g : Lnet/minecraft/client/entity/EntityPlayerSP;
INVOKEVIRTUAL net/minecraft/client/multiplayer/PlayerControllerMP.func_187098_a (IIILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;
POP

list handleSlotClick
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 2
ILOAD 3
ALOAD 4
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.handleSlotClick (IILnet/minecraft/inventory/ClickType;)V

list n_updateScreen
ALOAD 0
INVOKESPECIAL net/minecraft/client/gui/GuiScreen.func_73876_c ()V

list updateScreen
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.updateScreen ()V


#Other inventory modifications

list renderTabTooltip
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 2
ILOAD 3
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.objectUnderMouse (II)Z
IFEQ LCONT
ICONST_0
IRETURN
LCONT

list handleTabClick
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
ILOAD 2
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147003_i : I
IADD
ILOAD 3
ALOAD 0
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147009_r : I
IADD
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.objectUnderMouse (II)Z
IFEQ LCONT
ICONST_0
IRETURN
LCONT

list beaconButtonObscured
ALOAD 0
# NEI will replace the owner with the appropriate inner class
GETFIELD net/minecraft/client/gui/inventory/GuiBeacon.this$0 : Lnet/minecraft/client/gui/inventory/GuiBeacon;
DUP
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.manager : Lcodechicken/nei/guihook/GuiContainerManager;
SWAP
DUP
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147003_i : I
ILOAD 1
IADD
SWAP
GETFIELD net/minecraft/client/gui/inventory/GuiContainer.field_147009_r : I
ILOAD 2
IADD
INVOKEVIRTUAL codechicken/nei/guihook/GuiContainerManager.objectUnderMouse (II)Z
IFEQ LCONT
RETURN
LCONT

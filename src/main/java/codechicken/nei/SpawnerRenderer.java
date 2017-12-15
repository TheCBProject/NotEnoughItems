package codechicken.nei;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.util.TransformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.model.IModelState;

//import net.minecraft.entity.boss.BossStatus;

public class SpawnerRenderer implements IItemRenderer {

    public static void load(ItemMobSpawner item) {
        ModelRegistryHelper.registerItemRenderer(item, new SpawnerRenderer());
    }

    public void renderItem(ItemStack stack, TransformType transformType) {
        int meta = stack.getItemDamage();

        if (meta == 0) {
            meta = ItemMobSpawner.idPig;
        }

        //String bossName = BossStatus.bossName;
        //int bossTimeout = BossStatus.statusBarTime;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;

        IBakedModel baseModel = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("mob_spawner"));
        GlStateManager.pushMatrix();
        GlStateManager.translate(.5, .5, .5);
        GlStateManager.scale(2, 2, 2);
        mc.getRenderItem().renderItem(stack, baseModel);
        GlStateManager.popMatrix();

        try {
            Entity entity = ItemMobSpawner.getEntity(meta);
            entity.setWorld(world);
            float scale = 0.6F / Math.max(entity.height, entity.width);

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0.4, 0.5);
            GlStateManager.rotate((float) (ClientUtils.getRenderTime() * 10), 0, 1, 0);
            GlStateManager.rotate(-20, 1, 0, 0);
            GlStateManager.translate(0, -0.4, 0);
            GlStateManager.scale(scale, scale, scale);
            entity.setLocationAndAngles(0, 0, 0, 0, 0);
            mc.getRenderManager().renderEntity(entity, 0, 0, 0, 0, 0, false);
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();

            GlStateManager.enableRescaleNormal();
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        } catch (Exception e) {
            if (Tessellator.getInstance().getBuffer().isDrawing) {
                Tessellator.getInstance().draw();
            }
        }
        //BossStatus.bossName = bossName;
        //BossStatus.statusBarTime = bossTimeout;
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }
}

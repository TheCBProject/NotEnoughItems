package codechicken.nei;

import codechicken.core.ClientUtils;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.IItemRenderer;
import codechicken.lib.render.ModelRegistryHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
//import net.minecraft.entity.boss.BossStatus;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class SpawnerRenderer implements IItemRenderer {
    public static void load(ItemMobSpawner item) {
        ModelRegistryHelper.registerItemRenderer(item, new SpawnerRenderer());
    }

    public void renderItem(ItemStack stack) {
        int meta = stack.getItemDamage();

        if (meta == 0) {
            meta = ItemMobSpawner.idPig;
        }

        //String bossName = BossStatus.bossName;
        //int bossTimeout = BossStatus.statusBarTime;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;

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
            mc.getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, 0, false);
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
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
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/mob_spawner");
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return BlockRenderer.blockCameraTransform;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}

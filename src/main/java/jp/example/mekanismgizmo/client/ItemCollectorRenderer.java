package jp.example.mekanismgizmo.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import jp.example.mekanismgizmo.block.ItemCollectorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Draws the configured collection box of the item collector in the world - similar to Just Dire Things' "show area"
 * preview. The box is toggled from the machine screen and its state is stored in (and synced by) the block entity.
 */
public class ItemCollectorRenderer implements BlockEntityRenderer<ItemCollectorBlockEntity> {

    private static final float RED = 0.25F;
    private static final float GREEN = 1.0F;
    private static final float BLUE = 0.35F;

    @Override
    public void render(ItemCollectorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
          int packedLight, int packedOverlay) {
        if (!be.isRenderArea() || be.getLevel() == null) {
            return;
        }
        // The block entity renderer is translated to the machine position, so make the box relative
        AABB area = be.getCollectionArea().move(-be.getBlockPos().getX(), -be.getBlockPos().getY(), -be.getBlockPos().getZ())
              .inflate(0.002);
        LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), area, RED, GREEN, BLUE, 0.9F);
        renderFill(poseStack, bufferSource.getBuffer(ItemCollectorRenderTypes.AREA_FILL), area, RED, GREEN, BLUE, 0.12F);
    }

    private static void renderFill(PoseStack poseStack, VertexConsumer consumer, AABB box, float red, float green, float blue, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        // Down
        quad(consumer, matrix, red, green, blue, alpha, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ);
        // Up
        quad(consumer, matrix, red, green, blue, alpha, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
        // North
        quad(consumer, matrix, red, green, blue, alpha, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ);
        // South
        quad(consumer, matrix, red, green, blue, alpha, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ);
        // West
        quad(consumer, matrix, red, green, blue, alpha, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ);
        // East
        quad(consumer, matrix, red, green, blue, alpha, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, float red, float green, float blue, float alpha,
          float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, x3, y3, z3).color(red, green, blue, alpha).endVertex();
        consumer.vertex(matrix, x4, y4, z4).color(red, green, blue, alpha).endVertex();
    }

    /** Render type extended so we can use the protected {@link RenderType#create} factory. */
    private static final class ItemCollectorRenderTypes extends RenderType {
        private static final RenderType AREA_FILL = create("mekanismgizmo_item_collector_area",
              DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
              RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true));

        private ItemCollectorRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
              boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }
    }
}
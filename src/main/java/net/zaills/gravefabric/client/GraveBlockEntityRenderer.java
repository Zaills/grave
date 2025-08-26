package net.zaills.gravefabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.zaills.gravefabric.block.entity.GraveBlockEntity;

@Environment(EnvType.CLIENT)
public class GraveBlockEntityRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	public GraveBlockEntityRenderer(BlockEntityRendererFactory.Context context){}

	@Override
	public void render(GraveBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
		matrices.push();
		matrices.translate(0.5, 1.25, 0.5);

		if (entity.getWorld() != null && entity.getOwner() != null) {
			int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
			PlayerEntity player = entity.getWorld().getPlayerByUuid(entity.getOwner().getId());
			EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
			if (player != null) {
				player.limbAnimator.setSpeed(0.0f);
				player.lastBodyYaw = player.bodyYaw = 0.0f;
				player.lastHeadYaw = player.headYaw = 0.0f;
				dispatcher.render(player, 0,0,0, tickProgress, matrices, vertexConsumers, lightAbove);
			}
			else {
				SkeletonEntity skeleton = new SkeletonEntity(EntityType.SKELETON, entity.getWorld());
				skeleton.limbAnimator.setSpeed(0.0f);
				skeleton.lastBodyYaw = skeleton.bodyYaw = 0.0f;
				skeleton.lastHeadYaw = skeleton.headYaw = 0.0f;
				dispatcher.render(skeleton, 0,0,0, tickProgress, matrices, vertexConsumers, lightAbove);
			}
		}
		matrices.pop();
	}
}

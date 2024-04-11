package net.zaills.grave.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import net.zaills.grave.block.entity.GraveBlockEntity;

import java.util.Map;
import java.util.Objects;

public class GraveRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	private final SkullEntityModel ShM;

	public static final Identifier SkT = new Identifier("textures/entity/skeleton/skeleton.png");
	public GraveRenderer(BlockEntityRendererFactory.Context ctx){
		this.ShM = new SkullEntityModel(ctx.getLayerRenderDispatcher().getModelPart(EntityModelLayers.SKELETON_SKULL));
	}

	@Override
	public void render(GraveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

		if (Objects.equals(entity.getCachedState().getBlock().getName().getString(), "block.grave.grave")){
			matrices.push();
			matrices.translate(.5, 0, .5);
			matrices.scale(-.8f, -.8f, -.8f);
			switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)){
				case NORTH:
					this.ShM.setHeadRotation(0, 180, 0);
					matrices.multiply(new Quaternion(0, 0, (float) Math.sin((double) 25 /2), (float) Math.cos((double) 25 /2)));
					break;
				case SOUTH:
					this.ShM.setHeadRotation(0, 0, 0);
					matrices.multiply(new Quaternion(0, 0, (float) Math.sin((double) 25 /2), (float) Math.cos((double) 25 /2)));
					break;
				case EAST:
					this.ShM.setHeadRotation(0, 90, 0);
					matrices.multiply(new Quaternion((float) Math.sin((double) 25 /2), 0, 0, (float) Math.cos((double) 25 /2)));
					break;
				case WEST:
					this.ShM.setHeadRotation(0, 270, 0);
					matrices.multiply(new Quaternion((float) Math.sin((double) 25 /2), 0, 0, (float) Math.cos((double) 25 /2)));
					break;
			}
			this.ShM.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCullZOffset(SkT)), light, overlay, 1, 1, 1, 1);
			matrices.pop();
		}
	}

}

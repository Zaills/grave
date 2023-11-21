package net.zaills.grave.client;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.zaills.grave.block.entity.GraveBlockEntity;

public class GraveRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	@Override
	public void render(GraveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		Direction direction = entity.getCachedState().get(Properties.HORIZONTAL_FACING);

		matrices.push();

		matrices.scale(1, 1, 1);

		switch (direction){
			case NORTH:
				SkullBlockEntityRenderer.renderSkull(null, 0f, 0f, matrices, vertexConsumers, light, SkullBlockEntityRenderer.getModels(MinecraftClient.getInstance().getEntityModelLoader()).get(SkullBlock.Type.PLAYER), SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, entity.getOwner()));
				break;
			case SOUTH:
				SkullBlockEntityRenderer.renderSkull(null, 180f, 0f, matrices, vertexConsumers, light, SkullBlockEntityRenderer.getModels(MinecraftClient.getInstance().getEntityModelLoader()).get(SkullBlock.Type.PLAYER), SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, entity.getOwner()));
				break;
			case EAST:
				SkullBlockEntityRenderer.renderSkull(null, 90f, 0f, matrices, vertexConsumers, light, SkullBlockEntityRenderer.getModels(MinecraftClient.getInstance().getEntityModelLoader()).get(SkullBlock.Type.PLAYER), SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, entity.getOwner()));
				break;
			case WEST:
				SkullBlockEntityRenderer.renderSkull(null, -90f, 0f, matrices, vertexConsumers, light, SkullBlockEntityRenderer.getModels(MinecraftClient.getInstance().getEntityModelLoader()).get(SkullBlock.Type.PLAYER), SkullBlockEntityRenderer.getRenderLayer(SkullBlock.Type.PLAYER, entity.getOwner()));
				break;
		}

		matrices.pop();

		String name = "NoName";
		if (entity.getOwner() != null) {
			name = entity.getOwner().getName();
		}

		matrices.push();

		int wd = MinecraftClient.getInstance().textRenderer.getWidth(name);

		float scl = .7f / wd;

		/*switch (direction){
			case NORTH:
				break;
			case SOUTH:
				break;
			case EAST:
				break;
			case WEST:
				break;
		}*/

		matrices.translate(.5, 0, .5);
		matrices.translate(0, .6, .42);
		matrices.scale(-1, -1, 0);

		matrices.scale(scl, scl, scl);
		matrices.translate(-wd / 2., -4.5, 0);

		MinecraftClient.getInstance().textRenderer.draw(name, 0, 0, 0xFFFFFF, true, matrices.peek().getModel(), vertexConsumers, true, 0, light);
		matrices.pop();

	}

	public GraveRenderer(BlockEntityRenderDispatcher dispatch){
		this.dispatch = dispatch;
	}
	public BlockEntityRenderDispatcher dispatch;

	public GraveRenderer(BlockEntityRendererFactory.Context context){
		this.dispatch = context.getRenderDispatcher();
	}
}

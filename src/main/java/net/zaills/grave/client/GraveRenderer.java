package net.zaills.grave.client;

import net.minecraft.client.MinecraftClient;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Quaternion;
import net.zaills.grave.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class GraveRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	private final SkullEntityModel PhM;
	private final SkullEntityModel ShM;

	public static final Identifier SkT = new Identifier("textures/entity/skeleton/skeleton.png");
	public GraveRenderer(BlockEntityRendererFactory.Context ctx){
		this.PhM = new SkullEntityModel(ctx.getLayerRenderDispatcher().getModelPart(EntityModelLayers.PLAYER_HEAD));
		this.ShM = new SkullEntityModel(ctx.getLayerRenderDispatcher().getModelPart(EntityModelLayers.SKELETON_SKULL));
	}

	@Override
	public void render(GraveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

		if (Objects.equals(entity.getCachedState().getBlock().getName().getString(), "block.grave.grave")){
			matrices.push();
			matrices.translate(.5, 0, .5);
			matrices.scale(-.8f, -.8f, -.8f);
			var model = entity.getOwner() == null ? this.ShM : this.PhM;
			switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)){
				case NORTH:
					model.setHeadRotation(0, 180, 0);
					matrices.multiply(new Quaternion(0, 0, (float) Math.sin((double) 25 /2), (float) Math.cos((double) 25 /2)));
					break;
				case SOUTH:
					model.setHeadRotation(0, 0, 0);
					matrices.multiply(new Quaternion(0, 0, (float) Math.sin((double) 25 /2), (float) Math.cos((double) 25 /2)));
					break;
				case EAST:
					model.setHeadRotation(0, 90, 0);
					matrices.multiply(new Quaternion((float) Math.sin((double) 25 /2), 0, 0, (float) Math.cos((double) 25 /2)));
					break;
				case WEST:
					model.setHeadRotation(0, 270, 0);
					matrices.multiply(new Quaternion((float) Math.sin((double) 25 /2), 0, 0, (float) Math.cos((double) 25 /2)));
					break;
			}
			model.render(matrices, vertexConsumers.getBuffer(getRL(entity.getOwner())), light, overlay, 1, 1, 1, 1);
			matrices.pop();
		}

		int wd = MinecraftClient.getInstance().textRenderer.getWidth("Grave");

		matrices.push();
		matrices.translate(.5, 1, .5);

		assert MinecraftClient.getInstance().player != null;
		matrices.multiply(getQuad(entity.getPos(), MinecraftClient.getInstance().player.getPos()));

		matrices.scale(-1, -1, 0);
		matrices.scale(.7F / wd, .7F / wd, .7F / wd);
		matrices.translate(-wd / 2.0, -4.5, 0);

		MinecraftClient.getInstance().textRenderer.draw("Grave", .5f, 0, 0xFFFFFF, true, matrices.peek().getModel(), vertexConsumers, true, 0, light);
		matrices.pop();
	}

	public static RenderLayer getRL(@Nullable GameProfile Pf){
		if (Pf != null){
			MinecraftClient client = MinecraftClient.getInstance();
			Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = client.getSkinProvider().getTextures(Pf);
			return map.containsKey(MinecraftProfileTexture.Type.SKIN) ? RenderLayer.getEntityTranslucent(client.getSkinProvider().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN)) : RenderLayer.getEntityTranslucent(DefaultSkinHelper.getTexture(Pf.getId()));
		}
		return RenderLayer.getEntityCutoutNoCullZOffset(SkT);
	}

	public static Quaternion getQuad(BlockPos pos1, Position pos2){
		double dx = pos1.getX() +.5 - pos2.getX();
		double dz = pos1.getZ() +.5 - pos2.getZ();
		double heading = Math.atan2(dx, dz);
		return new Quaternion( 0, (float) Math.sin(heading/2), 0, (float) Math.cos(heading/2));
	}
}

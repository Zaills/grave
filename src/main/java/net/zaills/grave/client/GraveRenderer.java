package net.zaills.grave.client;

import net.minecraft.client.MinecraftClient;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.font.TextRenderer;
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
import net.zaills.grave.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GraveRenderer implements BlockEntityRenderer<GraveBlockEntity> {
	//public static final Identifier GRAVE = new Identifier("grave", "grave");

	private final TextRenderer Tr;
	private final SkullEntityModel PhM;
	private final SkullEntityModel ShM;


	public static final Identifier SkT = new Identifier("textures/entity/skeleton/skeleton.png");
	public GraveRenderer(BlockEntityRendererFactory.Context ctx){
		this.Tr = ctx.getTextRenderer();
		this.PhM = new SkullEntityModel(ctx.getLayerRenderDispatcher().getModelPart(EntityModelLayers.PLAYER_HEAD));
		this.ShM = new SkullEntityModel(ctx.getLayerRenderDispatcher().getModelPart(EntityModelLayers.SKELETON_SKULL));
	}

	@Override
	public void render(GraveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		matrices.translate(.5, 0, .5);
		matrices.scale(-1, -1, -1);
		var model = entity.getOwner() == null ? this.ShM : this.PhM;
		switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)){
			case NORTH:
				model.setHeadRotation(0, 180, 0);
				break;
			case SOUTH:
				model.setHeadRotation(0, 0, 0);
				break;
			case EAST:
				model.setHeadRotation(0, 90, 0);
				break;
			case WEST:
				model.setHeadRotation(0, 270, 0);
				break;
		}
		model.render(matrices, vertexConsumers.getBuffer(getRL(entity.getOwner())), light, overlay, 1, 1, 1, 1);
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

}
